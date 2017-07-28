//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.ports.jdt.watchpoint;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.core.model.IWatchpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;

import de.toem.impulse.cells.ports.AbstractPortAdapterBaseCell;
import de.toem.impulse.cells.ports.IPortAdapter;
import de.toem.impulse.cells.ports.IPortProgress;
import de.toem.impulse.cells.ports.IPortProviderFactory;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.MultiAdapterPort;
import de.toem.impulse.cells.preferences.ImpulsePorts;
import de.toem.impulse.cells.record.Scope;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.domain.TimeBase;
import de.toem.impulse.ports.jdt.IBreakpointListener;
import de.toem.impulse.ports.jdt.JavaBreakpointNotifier;
import de.toem.impulse.samples.IEventSamplesWriter;
import de.toem.impulse.samples.IFloatSamplesWriter;
import de.toem.impulse.samples.IIntegerSamplesWriter;
import de.toem.impulse.samples.ILogicSamplesWriter;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISamples;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.samples.ISamplesWriter;
import de.toem.impulse.samples.IStructSamplesWriter;
import de.toem.impulse.samples.ITextSamplesWriter;
import de.toem.impulse.serializer.AbstractPortAdapterRecordReader;
import de.toem.impulse.serializer.IRecordReader;
import de.toem.impulse.values.StructMember;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.ICover;
import de.toem.pattern.threading.Actives;
import de.toem.pattern.threading.IProgress;

@CellAnnotation(type = WatchpointTracer.TYPE, dynamicChildOf = { MultiAdapterPort.TYPE, ImpulsePorts.TYPE })
public class WatchpointTracer extends AbstractPortAdapterBaseCell implements IRecordPort, IPortAdapter {
	public static final String TYPE = "port.record.jdt.watchpoint";

	// parameters
	public boolean enableWatchpoints = true;
	public static final int SIGNAL_VALUE = 0;
	public static final int SIGNAL_STRUCT = 1; // value / thread / read&write
	public static final String[] WATCHPOINT_SIGNAL_LABELS = { "Value only", "Struct of Value/Thread/Access" };
	public static final Object[] WATCHPOINT_SIGNAL_VALUES = { SIGNAL_VALUE, SIGNAL_STRUCT };
	public int watchpointSignal;
	public static final String[] ACTION_LABELS = { "Don't suspend", "Suspend" };
	public static final int ACTION_DONT_SUSPEND = 0;
	public static final int ACTION_SUSPEND = 1;
	public int watchpointAction;
	public boolean enableWatchpointExpressions = false;
	public String watchpointExpressionFilter;

	public boolean enableBreakpoints;
	public static final String[] BREAKPOINT_SIGNAL_LABELS = { "Event only", "Struct of Thread/Breakpoint" };
	public static final Object[] BREAKPOINT_SIGNAL_VALUES = { SIGNAL_VALUE, SIGNAL_STRUCT };
	public int breakpointSignal;
	public int breakpointAction;
	public boolean enableBreakpointExpressions = false;
	public String breakpointExpressionFilter;

	@Override
	public int getNature() {
		return IRecordPort.NATURE_FLOATING | IRecordPort.NATURE_REFRESH_CONTINUOUS | IRecordPort.NATURE_CURRENT_VALUE;
	}

	/**
	 * This class implements both the input (Closable) as well as the reader.
	 * IPortProviderFactory adds visual output of current values in the viewer.
	 */
	class Tracer extends AbstractPortAdapterRecordReader implements Closeable, IPortProviderFactory, IBreakpointListener {

		// parameters of this input

		// status
		long start;
		long current;
		boolean closed;
		int changed;
		IPortProgress progress;

		class Target {

			Target(IDebugTarget target) {
				this.target = target;
				try {
					Scope scope = Tracer.this.addScope(null, target.getLaunch().getLaunchConfiguration().getName());
					watchPoints = Tracer.this.addScope(scope, "wp");
					breakPoints = Tracer.this.addScope(scope, "bp");
					// watchExpressions = breakExpressions =
					// Tracer.this.addScope(scope, "expressions");
					watchExpressions = Tracer.this.addScope(watchPoints, "expressions");
					breakExpressions = Tracer.this.addScope(breakPoints, "expressions");
				} catch (Throwable e) {
				}

			}

			IDebugTarget target;
			Map<Object, Signal> signals = new HashMap<Object, Signal>();
			Scope watchPoints;
			Scope breakPoints;
			Scope watchExpressions;
			Scope breakExpressions;
		}

		Map<IDebugTarget, Target> targets = new HashMap<IDebugTarget, Target>();

		@Override
		protected void process(IPortProgress progress) {

			// progress
			if (progress == null)
				return;
			this.progress = progress;

			// Init the record and signals
			initRecord("JDT Trace", TimeBase.ms);

			changed = CHANGED_RECORD;

			// wait after header parsing
			synchronized (progress) {
				while (!progress.isStreaming() && !progress.isCanceled())
					try {
						progress.wait(250);
						changed = CHANGED_CURRENT;
					} catch (InterruptedException e) {
					}
			}

			boolean listenerAdded = false;
			try {

				// start at first event
				start = Long.MIN_VALUE;
				current = 0;

				// operate until canceled
				while (!closed && (progress == null || !progress.isCanceled())) {
					if (!listenerAdded && JavaBreakpointNotifier.instance != null) {
						listenerAdded = true;
						JavaBreakpointNotifier.instance.add(this);
					}
					Actives.sleep(100);
				}

				// close
				if (start != Long.MIN_VALUE)
					close(current + 1000);
				progress = null;
			} finally {
				if (listenerAdded)
					JavaBreakpointNotifier.instance.remove(this);
			}
		}

		@Override
		public int getMode(IBreakpoint bp) {
			int mode = 0;
			if (bp instanceof IWatchpoint) {
				mode |= enableWatchpoints ? IBreakpointListener.MODE_ENABLED : 0;
				mode |= enableWatchpointExpressions ? IBreakpointListener.MODE_EXPRESSIONS : 0;
				mode |= watchpointAction > 0 ? IBreakpointListener.MODE_SUSPEND : 0;
			} else {
				mode |= enableBreakpoints ? IBreakpointListener.MODE_ENABLED : 0;
				mode |= enableBreakpointExpressions ? IBreakpointListener.MODE_EXPRESSIONS : 0;
				mode |= breakpointAction > 0 ? IBreakpointListener.MODE_SUSPEND : 0;
			}
			return mode;
		}

		@Override
		public String getExpressionFilter(IBreakpoint bp) {
			if (bp instanceof IWatchpoint)
				return watchpointExpressionFilter;
			else
				return breakpointExpressionFilter;
		}

		@Override
		synchronized public void hit(IThread thread, IBreakpoint bp, Object source, IValue value, long time) {
			try {

				// domain
				if (start == Long.MIN_VALUE) {
					start = time;
					current = 0;
				} else
					current = time - start;

				// target
				Target target = targets.get(thread.getDebugTarget());
				if (target == null) {
					target = new Target(thread.getDebugTarget());
					targets.put(thread.getDebugTarget(), target);
				}

				// signal & writer
				Signal signal = null;
				ISamplesWriter writer = null;
				String name = null;
				if (target.signals.containsKey(source)) {
					signal = target.signals.get(source);
					writer = getWriter(signal);
				} else {

					SignalType signalType = SignalType.Unknown;
					SignalDescriptor signalDescriptor = SignalDescriptor.DEFAULT;

					Scope scope = null;

					if (bp instanceof IWatchpoint) {

						if (source instanceof IVariable) {
							signalType = (watchpointSignal == SIGNAL_STRUCT) ? SignalType.Struct : source2Type(source);
							if (watchpointSignal == SIGNAL_STRUCT)
								signalDescriptor = new SignalDescriptor(SignalDescriptor.CONTENT_DEFAULT,ISamples.FORMAT_VALUE_DEFAULT| ISamples.FORMAT_COLLECTION_MEMBER_3);
							name = ((IVariable) source).getName();
							scope = target.watchPoints;
						} else if (source instanceof IWatchExpression) {
							signalType = source2Type(value);
							name = ((IWatchExpression) source).getExpressionText();
							scope = target.watchExpressions;
						}
					} else {
						if (source instanceof IBreakpoint) {
							signalType = (breakpointSignal == SIGNAL_STRUCT) ? SignalType.Struct : SignalType.Event;
							if (breakpointSignal == SIGNAL_STRUCT)
								signalDescriptor = new SignalDescriptor(SignalDescriptor.CONTENT_DEFAULT,ISamples.FORMAT_VALUE_DEFAULT| ISamples.FORMAT_COLLECTION_MEMBER_0);

							IJavaStackFrame stackframe = (IJavaStackFrame) thread.getTopStackFrame();
							name = stackframe.getSourceName() + ":" + stackframe.getLineNumber();
							scope = target.breakPoints;

						} else if (source instanceof IWatchExpression) {
							signalType = source2Type(value);
							name = ((IWatchExpression) source).getExpressionText();
							scope = target.breakExpressions;
						}
					}
					if (signalType != SignalType.Unknown) {
						target.signals.put(source, signal = addSignal(scope, name, null, ProcessType.Discrete, signalType, signalDescriptor));
						writer = getWriter(signal);
						writer.open(0);
						changed = CHANGED_RECORD;
					}
				}
				if (signal != null) {
					Object o = value2Object(value);
					if (writer instanceof IIntegerSamplesWriter && o instanceof Number)
						((IIntegerSamplesWriter) writer).write(current, false, (Number) o);
					else if (writer instanceof IFloatSamplesWriter && value instanceof Number)
						((IFloatSamplesWriter) writer).write(current, false, (Number) o);
					else if (writer instanceof ILogicSamplesWriter && o instanceof Boolean)
						((ILogicSamplesWriter) writer).write(current, false, o == Boolean.TRUE ? STATE_1_BITS : STATE_0_BITS);
					else if (writer instanceof ITextSamplesWriter && o instanceof String)
						((ITextSamplesWriter) writer).write(current, false, (String) o);
					else if (writer instanceof IEventSamplesWriter)
						((IEventSamplesWriter) writer).write(current, false);
					else if (writer instanceof IStructSamplesWriter) {
						StructMember[] statMembers = new StructMember[5];
						statMembers[0] = new StructMember("Thread", StructMember.STRUCT_TYPE_ENUM, null, ISample.FORMAT_TEXT, thread.getName());
						IJavaStackFrame stackframe = (IJavaStackFrame) thread.getTopStackFrame();
						int sline = stackframe.getLineNumber();
						String sname = stackframe.getSourceName();
						statMembers[1] = new StructMember("Resource", StructMember.STRUCT_TYPE_ENUM, null, ISample.FORMAT_TEXT, sname);
						statMembers[2] = new StructMember("Line", StructMember.STRUCT_TYPE_INTEGER, null, -1, sline);
						if (bp instanceof IWatchpoint) {
							statMembers[3] = new StructMember("Value", StructMember.STRUCT_TYPE_TEXT, null, -1, String.valueOf(o));
							String access = ""+(((IWatchpoint) bp).isModification() ? "Modification" :"")+(((IWatchpoint) bp).isAccess()&&((IWatchpoint) bp).isModification() ? "+" :"")+(((IWatchpoint) bp).isAccess() ? "Access" :"");
							statMembers[4] = new StructMember("Type", StructMember.STRUCT_TYPE_ENUM, null, ISample.FORMAT_TEXT, access);
						}
						((IStructSamplesWriter) writer).write(current, false, statMembers);
						// Utils.log(source instanceof IWatchExpression ?
						// ((IWatchExpression)source).getExpressionText():source,value);
						// for (String k:m.getAttributes().keySet())
						// Utils.log(k,m.getAttribute(k));
					}
					changed = changed < CHANGED_SIGNALS ? CHANGED_SIGNALS : changed;
				}
			} catch (Throwable e) {

			}
		}

		@Override
		public void added(IBreakpoint bp) {
		}

		@Override
		public void removed(IBreakpoint bp) {
		}

		@Override
		public boolean supportsStreaming() {
			return true;
		}

		@Override
		synchronized public ICover flush() {
			changed = CHANGED_NONE;
			return super.doFlush(current);
		}

		@Override
		public int hasChanged() {
			return changed;
		}

		@Override
		synchronized public void close() throws IOException {
			closed = true;
		}

		@Override
		public Object getProvider(Class clazz, Object subject) {
			return null;
		}

		private Object value2Object(IValue value) {
			Object o = null;
			String refType;
			try {
				refType = value != null ? value.getReferenceTypeName() : null;
				if (value != null && refType != null) {

					if (refType.equals(Byte.TYPE.getName()) || refType.equals(Byte.class.getName())) {
						o = (byte) Integer.parseInt(value.toString());
					} else if (refType.equals(Short.TYPE.getName()) || refType.equals(Short.class.getName())) {
						o = (short) Integer.parseInt(value.toString());
					} else if (refType.equals(Integer.TYPE.getName()) || refType.equals(Integer.class.getName())) {
						o = Integer.parseInt(value.toString());
					} else if (refType.equals(Long.TYPE.getName()) || refType.equals(Long.class.getName())) {
						o = Long.parseLong(value.toString());
					} else if (refType.equals(Float.TYPE.getName()) || refType.equals(Float.class.getName())) {
						o = Float.parseFloat(value.toString());
					} else if (refType.equals(Double.TYPE.getName()) || refType.equals(Double.class.getName())) {
						o = Double.parseDouble(value.toString());
					} else if (refType.equals(Boolean.TYPE.getName()) || refType.equals(Boolean.class.getName())) {
						o = Boolean.parseBoolean(value.toString());
					} else if (refType.equals(String.class.getName())) {
						o = value.toString().replaceAll("\"", "");
					} else if (value.toString().equals("null")) {
						o = null;
					} else
						o = String.valueOf(value);
				}
			} catch (DebugException e) {
			}
			return o;

		}

		private SignalType source2Type(Object source) {
			Object o = null;
			String refType;
			SignalType type = SignalType.Unknown;
			try {
				refType = source instanceof IVariable ? ((IVariable) source).getReferenceTypeName()
						: source instanceof IValue ? ((IValue) source).getReferenceTypeName() : null;
				if (source != null && refType != null) {

					if (refType.equals(Byte.TYPE.getName()) || refType.equals(Byte.class.getName())) {
						type = SignalType.Integer;
					} else if (refType.equals(Short.TYPE.getName()) || refType.equals(Short.class.getName())) {
						type = SignalType.Integer;
					} else if (refType.equals(Integer.TYPE.getName()) || refType.equals(Integer.class.getName())) {
						type = SignalType.Integer;
					} else if (refType.equals(Long.TYPE.getName()) || refType.equals(Long.class.getName())) {
						type = SignalType.Integer;
					} else if (refType.equals(Float.TYPE.getName()) || refType.equals(Float.class.getName())) {
						type = SignalType.Float;
					} else if (refType.equals(Double.TYPE.getName()) || refType.equals(Double.class.getName())) {
						type = SignalType.Float;
					} else if (refType.equals(Boolean.TYPE.getName()) || refType.equals(Boolean.class.getName())) {
						type = SignalType.Logic;
					} else if (refType.equals(String.class.getName())) {
						type = SignalType.Text;
					} else
						type = SignalType.Text;
				}
			} catch (DebugException e) {
			}
			return type;
		}
	}

	@Override
	public boolean validate(ICell insertPoint) {
		return true;
	}

	@Override
	public Closeable getInput(IProgress iProgress) {
		return new Tracer();
	}

	@Override
	public IRecordReader newReader(Closeable input) {
		if (input instanceof Tracer)
			return (Tracer) input;
		return null;
	}

	@Override
	public Object getProvider(Class clazz, Object subject) {
		return null;
	}

	@Override
	public boolean isPort() {
		return getParent() instanceof ImpulsePorts;
	}

}
