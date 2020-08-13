package de.toem.impulse.extension.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;

import de.toem.basics.core.Utils;

public class JavaBreakpointNotifier implements IJavaBreakpointListener {

    static public JavaBreakpointNotifier instance;

    List<IJavaBreakpoint> added = new ArrayList<IJavaBreakpoint>();
    List<IJavaBreakpoint> installed = new ArrayList<IJavaBreakpoint>();
    List<IBreakpointListener> listeners = new ArrayList<IBreakpointListener>();
    WeakHashMap<IDebugTarget, IAstEvaluationEngine> engines = new WeakHashMap<IDebugTarget, IAstEvaluationEngine>();

    public JavaBreakpointNotifier() {
        instance = this;
    }

    @Override
    public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint bp) {
        // Utils.log("addingBreakpoint", target, bp);
        added.add(bp);
        for (IBreakpointListener listener : listeners)
            listener.added(bp);
    }

    @Override
    public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint bp, IJavaType type) {
        // Utils.log("installingBreakpoint", target, bp);
        return 0;
    }

    @Override
    public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint bp) {
        // Utils.log("breakpointInstalled", target, bp);
        installed.add(bp);
    }

    @Override
    public synchronized int breakpointHit(final IJavaThread thread, final IJavaBreakpoint bp) {

        boolean enabled = false;
        boolean suspend = false;
        boolean expressions = false;
        List<String> filters = new ArrayList<String>();
        for (IBreakpointListener listener : listeners) {
            int mode = listener.getMode(bp);
            enabled |= (mode & IBreakpointListener.MODE_ENABLED) != 0;
            expressions |= (mode & IBreakpointListener.MODE_EXPRESSIONS) != 0;
            suspend |= (mode & IBreakpointListener.MODE_SUSPEND) != 0;
            String filter = listener.getExpressionFilter(bp);
            if (Utils.isEmpty(filter)) {
                if (!filters.contains("*"))
                    filters.add("*");
            } else
                for (String f : filter.split(",")) {
					if (!Utils.isEmpty(f) && !filters.contains(f))
                        filters.add(f);
                }
        }

        final long time = Utils.millies();
        if (enabled) {
            if (bp instanceof IJavaWatchpoint) {
                IJavaWatchpoint wp = (IJavaWatchpoint) bp;
                try {

                    IJavaVariable var = thread.findVariable(wp.getFieldName());
					IValue value = var != null ? var.getValue():null;

                    for (IBreakpointListener listener : listeners) {
                        int mode = listener.getMode(bp);
                        if ((mode & IBreakpointListener.MODE_ENABLED) != 0)
                            listener.hit(thread, bp, var, value, time);
                    }
                } catch (Throwable e) {
                }

            } else if (bp instanceof IJavaLineBreakpoint) {

                IJavaLineBreakpoint lp = (IJavaLineBreakpoint) bp;
                try {

                    for (IBreakpointListener listener : listeners) {
                        int mode = listener.getMode(bp);
                        if ((mode & IBreakpointListener.MODE_ENABLED) != 0)
                            listener.hit(thread, bp, bp, null, time);
                    }
                } catch (Throwable e) {
                }
            }
        }
        if (enabled && expressions) {

            // engine
            IAstEvaluationEngine engine = engines.get(thread.getDebugTarget());
			if (engine == null) {
            try {
                String pname = thread.getDebugTarget().getLaunch().getLaunchConfiguration().getAttribute("org.eclipse.jdt.launching.PROJECT_ATTR", "");
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                IWorkspaceRoot root = workspace.getRoot();
                IProject rproject = root.getProject(pname);
                IJavaProject project = JavaCore.create(rproject);
                engine = EvaluationManager.newAstEvaluationEngine(project, (IJavaDebugTarget) thread.getDebugTarget());
                engines.put(thread.getDebugTarget(), engine);
            } catch (Throwable e) {
            }
			}
            if (engine != null)
                for (final IExpression expression : DebugPlugin.getDefault().getExpressionManager().getExpressions()) {
                    try {
						if (expression instanceof IWatchExpression && ((IWatchExpression) expression).isEnabled()) {
                            
                            // global filter
                            boolean doIt = false;
                            for (String f : filters) {
                                if ("*".equals(f) || ((IWatchExpression) expression).getExpressionText().contains(f)) {
                                    doIt = true;
                                    break;
                                }
                            }
                            if (doIt)
                                engine.evaluate(((IWatchExpression) expression).getExpressionText(), (IJavaStackFrame) thread.getTopStackFrame(),
                                        new IEvaluationListener() {
                                            @Override
                                            public void evaluationComplete(IEvaluationResult result) {
                                                IValue value = result.getValue();
                                                String[] error = result.getErrorMessages();
                                                if (error == null || error.length == 0 && value != null) {
                                                    for (IBreakpointListener listener : listeners) {
                                                        int mode = listener.getMode(bp);
                                                        if ((mode & IBreakpointListener.MODE_EXPRESSIONS) != 0) {
                                                            // lister filter
                                                            boolean doIt = false;
                                                            String filter = listener.getExpressionFilter(bp);
                                                            if (Utils.isEmpty(filter)) {
                                                                doIt = true;
                                                            } else
                                                                for (String f : filter.split(",")) {
                                                                    if (!Utils.isEmpty(filter) && ("*".equals(f)
                                                                            || ((IWatchExpression) expression).getExpressionText().contains(f))) {
                                                                        doIt = true;
                                                                        break;
                                                                    }
                                                                }
                                                            if (doIt)
                                                                listener.hit(thread, bp, expression, value, time);
                                                        }
                                                    }
                                                }

                                            }
                                        }, DebugEvent.EVALUATION_IMPLICIT, false);

                        }
                    } catch (Throwable e) {
                    }
                }
        }
        if (enabled && !suspend)
            return DONT_SUSPEND;
        return DONT_CARE;
    }

    @Override
    public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint bp) {
        // Utils.log("breakpointRemoved", target, bp);
        added.remove(bp);
        installed.remove(bp);
        for (IBreakpointListener listener : listeners)
            listener.removed(bp);
    }

    @Override
    public void breakpointHasRuntimeException(IJavaLineBreakpoint bp, DebugException exception) {
        // Utils.log("breakpointHasRuntimeException", bp, exception);

    }

    @Override
    public void breakpointHasCompilationErrors(IJavaLineBreakpoint bp, org.eclipse.jdt.core.dom.Message[] errors) {
        // Utils.log("breakpointHasCompilationErrors", bp, errors);

    }

    public List<IJavaBreakpoint> getBreakpoints() {
        return added;
    }

    public List<IJavaWatchpoint> getWatchpoints() {
        List<IJavaWatchpoint> list = new ArrayList<IJavaWatchpoint>();
        for (IJavaBreakpoint bp : added)
            if (bp instanceof IJavaWatchpoint)
                list.add((IJavaWatchpoint) bp);
        return list;
    }

    public synchronized void add(IBreakpointListener listener) {
        listeners.add(listener);
    }

    public synchronized void remove(IBreakpointListener listener) {
        listeners.remove(listener);
    }
}
