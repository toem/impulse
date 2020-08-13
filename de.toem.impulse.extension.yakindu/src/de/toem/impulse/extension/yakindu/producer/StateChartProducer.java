package de.toem.impulse.extension.yakindu.producer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.yakindu.sct.model.sgraph.Statechart;
import org.yakindu.sct.model.sruntime.ExecutionContext;
import org.yakindu.sct.simulation.core.sexec.interpreter.IExecutionFlowInterpreter;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.controller.source.PropertySource;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.domain.IDomainBase;
import de.toem.impulse.extension.yakindu.ImpulseYakinduExtension;
import de.toem.impulse.extension.yakindu.StateChartLoader;
import de.toem.impulse.extension.yakindu.chart.StateChart;
import de.toem.impulse.extension.yakindu.chart.StateChartDialog;
import de.toem.impulse.i18n.I18n;
import de.toem.impulse.samples.IEventSamplesWriter;
import de.toem.impulse.samples.IMemberDescriptor;
import de.toem.impulse.samples.IPointer;
import de.toem.impulse.samples.IReadableSamples;
import de.toem.impulse.samples.IReaderDomainBaseProvider;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISamplePointer;
import de.toem.impulse.samples.ISamples;
import de.toem.impulse.samples.ISamplesIterator;
import de.toem.impulse.samples.ISamplesWriter;
import de.toem.impulse.samples.IStructSamplesWriter;
import de.toem.impulse.samples.ITextSamplesWriter;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.samples.base.PackedSamples;
import de.toem.impulse.samples.iterator.SamplePointer;
import de.toem.impulse.samples.iterator.SamplesIterator;
import de.toem.impulse.samples.producer.AbstractUpdatableSamplesProducer;
import de.toem.impulse.scripting.DefaultScriptContextProvider;
import de.toem.impulse.scripting.IScriptContextProvider;
import de.toem.impulse.scripting.ScriptControls;
import de.toem.impulse.scripting.Scripting;
import de.toem.impulse.values.StructMember;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.Link;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.properties.PropertyModel;
import de.toem.pattern.threading.IProgress;

public class StateChartProducer extends AbstractUpdatableSamplesProducer implements IScriptContextProvider {

    // parameters
    private Link chartReference;
    private String outFilter;
    private String timeout;

    // state chart & engine
    private Statechart stateChart;
    private Engine engine;
    private int[] sourceIndex;
    private String value = null;
    private boolean changeWritten;
    private Scripting scripting;

    private Map<IReadableSamples, List<StateChartInput>> inputsBySource = new HashMap<>();
    private Map<String, StateChartInput> inputsByName = new HashMap<>();
    private List<StateChartOutput> outputs = new ArrayList<>();
    private StructMember[] members;

    long current;

    public StateChartProducer() {
    };

    public StateChartProducer(List<IReadableSamples> sources, ProcessType processType, SignalType signalType, SignalDescriptor signalDescriptor,
            IDomainBase domainBase, String start, String end, String rate, String definition, IPropertyModel parameters,
            IReaderDomainBaseProvider readerBaseProvider) {
        super(sources, processType, signalType, signalDescriptor, domainBase, start, end, rate, definition, parameters, readerBaseProvider);

    }

    static public IPropertyModel getPropertyModel() {
        return new PropertyModel() {
            public IControlProvider getControls() {
                IControlProvider provider = StateChartDialog.getStateControls(true);
                provider.setSourceType(PropertySource.class);

                return provider;
            }

        }.add("chartReference", (Link) null, "Chart Reference", null)/* .add("outFilter", "", "outFilter", null) */.add("timeout", "", I18n.General_TimeoutMs,
                null);
    }

    static public IControlProvider getDefinitionControls(Field source) {

        return new AbstractControlProvider() {

            @Override
            protected boolean fillThis() {
                try {
                    ScriptControls.fillScriptControls(tlk(), container(), editor(), source, tlk().ld(cols(), TLK.GRAB, TLK.IGNORE_CONTROL, TLK.FILL, 150));
                } catch (SecurityException e) {
                }
                return true;
            }
        };
    }

    @Override
    public void init(String sstart, String send, String srate, IReaderDomainBaseProvider readerBaseProvider) {

        // parameters
        this.chartReference = (Link) parameters.getTyped("chartReference");
        this.outFilter = (String) parameters.getTyped("outFilter");
        this.timeout = (String) parameters.getTyped("timeout");
        super.init(sstart, send, srate, readerBaseProvider);
    }

    @Override
    public void modifyInit() {

        super.modifyInit();

        if ((flags & FLAG_signalType) == 0) {

            switch (signalType) {
            case Struct:
                break;
            default:
                signalType = SignalType.Event;
            }

        }
        switch (signalType) {
        case Struct:
        case Event:
            break;
        default:
            setError(I18n.Producer_InvalidParameters);
        }
    }

    @Override
    protected boolean instatiate(IProgress p) {

        try {

            // timeout
            if (p != null && timeout != null && Utils.parseInt(timeout, 0) > 0) {
                p.incTimeout(Utils.parseInt(timeout, 0));
            }

            // writer
            targetWriter = PackedSamples.createWriter(processType, signalType, signalDescriptor, productionBase);
            if (targetWriter == null)
                return false;
            targetWriter.open(start, (flags & FLAG_end) != 0 ? end : start, rate, 0, 0, null);
            current = start;

            // state chart
            // Load state chart
            StateChartLoader loader = StateChart.loadChart(this, StateChart.MODE_CHART_REFERENCE, chartReference, true);
            if (loader == null)
                return false;

            // engine
            stateChart = loader.getStateChart();
            engine = stateChart != null ? new Engine(stateChart, processType == ProcessType.Discrete, start) : null;

            // sources
            final ISamplePointer[] input = new ISamplePointer[sources.size()];
            sourceIndex = new int[sources.size()];
            for (int n = 0; n < sources.size(); n++) {
                IReadableSamples source = sources.get(n);
                if (source != null) {
                    input[n] = new SamplePointer(source);
                }
                sourceIndex[n] = -1;
            }
            iter = new SamplesIterator(targetWriter, input);

            // scripting
            scripting = new Scripting(this, "definition") {

                @Override
                public void init() {

                    // out
                    setSymbol("out", targetWriter);

                    // inputs
                    int n = 0;
                    int m = 0;
                    int size = sources.size();
                    if (size > 1 && sources.get(0) == null) {
                        size -= 1;
                    }
                    for (IReadableSamples source : sources) {
                        if (source != null) {
                            setSymbol("in" + n, input[m]);
                            m++;
                        }
                        n++;
                    }
                    setSymbol("input", input);
                    setSymbol("iter", iter);
                    setSymbol("base", targetWriter.getDomainBase());

                    setSymbol("engine", StateChartProducer.this.engine);
                    setSymbol("interpreter", StateChartProducer.this.engine.interpreter);
                    setSymbol("context", StateChartProducer.this.engine.context);

                }

                @Override
                protected Object eval(ScriptEngine engine, boolean init, String script) throws ScriptException {
                    return script != null ? engine.eval(removePsoidoCode(script)) : null;
                }

                @Override
                protected void handleThreadDeath() {
                    setError(I18n.Producer_Killed);

                }

                @Override
                protected void handleException(Throwable e) {
                    setError(e.getLocalizedMessage());
                    e.printStackTrace();
                }
            };
            engine.executor.scripting = scripting;
            scripting.run(p);

            // enter
            engine.enter();
            if (scripting.hasFunction("onEnter"))
                scripting.invoke("onEnter");

            // input
            for (int n = 0; n < input.length; n++) {
                ISamplePointer in = input[n];
                if (in != null) {

                    // simple signal
                    if (in.getSignalType().isSimple()) {

                        String name = Link.fromPath(in.getId() != null ? in.getId() : "").getName();

                        // simple variable signal
                        if (!Utils.equals(in.getContent(), ISample.CONTENT_EVENT) && engine.hasVariable(name) && !engine.isVariableReadOnly(name)
                                && !inputsByName.containsKey(name)) {
                            addInput(new StateChartInput(in, name, StateChartInput.TYPE_VARIABLE, null, null));
                        } else if (engine.hasEvent(name) && !engine.isEventReadOnly(name) && !inputsByName.containsKey(name)) {
                            addInput(new StateChartInput(in, name, StateChartInput.TYPE_EVENT, null, null));
                        } else if (Utils.equals(in.getContent(), ISample.CONTENT_EVENT) && !engine.hasEvent(name) && !engine.hasVariable(name)
                                && !inputsByName.containsKey(name)) {
                            addInput(new StateChartInput(in, name, StateChartInput.TYPE_MULTI_EVENT, null, null));
                        }

                    }

                    // struct or array signals
                    else if (in.getSignalType().isArrayOrStruct()) {

                        // total event
                        if (Utils.equals(in.getContent(), ISample.CONTENT_EVENT) && !inputsByName.containsKey(name)) {

                            // event member
                            List<Object> typeMembers = in.membersWithContent(ISample.CONTENT_EVENTTYPE);
                            List<Object> paramMembers = in.membersWithContent(ISample.CONTENT_EVENTPARM);
                            if (!typeMembers.isEmpty())
                                addInput(new StateChartInput(in, name, StateChartInput.TYPE_MULTI_EVENT, typeMembers.get(0),
                                        paramMembers.isEmpty() ? null : paramMembers.get(0)));

                        }

                        // permembers
                        if (!Utils.equals(in.getContent(), ISample.CONTENT_EVENT))

                            for (IMemberDescriptor member : in.getMemberDescriptors()) {

                                String name = member.getName();

                                if (!Utils.equals(member.getContent(), ISample.CONTENT_EVENT) && engine.hasVariable(name) && !engine.isVariableReadOnly(name)
                                        && !inputsByName.containsKey(name)) {
                                    addInput(new StateChartInput(in, name, StateChartInput.TYPE_VARIABLE, member.getId(), null));
                                } else if (engine.hasEvent(name) && !engine.isEventReadOnly(name) && !inputsByName.containsKey(name)) {
                                    addInput(new StateChartInput(in, name, StateChartInput.TYPE_EVENT, member.getId(), null));
                                } else if (Utils.equals(member.getContent(), ISample.CONTENT_EVENT) && !engine.hasEvent(name) && !engine.hasVariable(name)
                                        && !inputsByName.containsKey(name)) {
                                    addInput(new StateChartInput(in, name, StateChartInput.TYPE_MULTI_EVENT, member.getId(), null));
                                }
                            }
                    }
                }
            }

            // outputs
            if (signalType == SignalType.Struct) {
                for (String name : engine.getAllVariableNames()) {
                    if (name != null && (!inputsByName.containsKey(name))) {
                        outputs.add(new StateChartOutput((IStructSamplesWriter) targetWriter, name, StateChartOutput.TYPE_VARIABLE));
                    }
                }
                for (String name : engine.getAllEventNames()) {
                    Utils.log(name);
                    if (name != null && (!inputsByName.containsKey(name))) {
                        outputs.add(new StateChartOutput((IStructSamplesWriter) targetWriter, name, StateChartOutput.TYPE_EVENT));
                    }
                }
                members = new StructMember[outputs.size() + 2];
                ((IStructSamplesWriter) targetWriter).createMember(members, 0, "@States", STRUCT_TYPE_LOCAL_ENUM | STRUCT_MOD_VALID_UNTIL_CHANGE,
                        ISamples.CONTENT_STATE, FORMAT_DEFAULT);
                ((IStructSamplesWriter) targetWriter).createMember(members, 1, "@Calls", STRUCT_TYPE_TEXT, null, FORMAT_DEFAULT);
                for (int n = 0; n < outputs.size(); n++)
                    members[n + 2] = outputs.get(n).member;
            }

            // reader
            setReference(PackedSamples.createReader(targetWriter, readerBase));

        } catch (

        Throwable e) {
            e.printStackTrace();
            setError(e.getLocalizedMessage());
            return false;
        }

        return reference != null;
    }

    @Override
    public void provideToScriptContext(IScriptContextInterface context) {

        DefaultScriptContextProvider.provideDefaultScriptContext(context, true, false, false, false, false, true);

        int index = 0;
        for (IReadableSamples source : sources) {
            if (source != null) {
                context.addSymbol("in" + index++, false, ISamplePointer.class, IReadableSamples.class);
            }
        }
        Class<? extends ISamplesWriter> writer = PackedSamples.getWriterInterface(signalType);
        if (writer != null)
            context.addSymbol("out", false, writer);
        context.addSymbol("base", false, IDomainBase.class);
        context.addSymbol("input", false, IPointer.class, ISamplePointer.class, IReadableSamples.class); // ?

        context.addImport("de.toem.impulse.extension.yakindu.producer", null);
        context.addImport("org.yakindu.sct.simulation.core.sexec.interpreter", null);
        context.addImport("org.yakindu.sct.model.sruntime", null);

        context.addSymbol("iter", false, ISamplesIterator.class);
        context.addSymbol("engine", false, Engine.class);
        context.addSymbol("interpreter", false, IExecutionFlowInterpreter.class);
        context.addSymbol("context", false, ExecutionContext.class);
        context.setLoader(ImpulseYakinduExtension.class.getClassLoader());

        context.setScript(definition, null);
    }

    private void addInput(StateChartInput input) {
        List<StateChartInput> list;
        if (!inputsBySource.containsKey(input.getPointer())) {
            list = new ArrayList<>();
            inputsBySource.put(input.getPointer(), list);
        } else
            list = inputsBySource.get(input.getPointer());
        list.add(input);
        inputsByName.put(input.getName(), input);
    }

    @Override
    protected boolean execute(IProgress p) {
        try {

            while (iter.hasNext() && !p.isCanceled()) {
                Long next = iter.next(targetWriter);

                // Utils.log("start", current, next);

                // run engine
                while (next > current && !p.isCanceled()) {
                    current = engine.timeLeap(processType == ProcessType.Continuous ? rate : next - current);
                    if (next > current) {
                        runEngine(current);
                    }
                }

                ISamplePointer[] input = iter.pointers();
                for (int n = 0; n < input.length; n++) {
                    ISamplePointer pointer = input[n];
                    if (pointer != null) {
                        if (pointer.getIndex() > sourceIndex[n]) {
                            sourceIndex[n] = pointer.getIndex();

                            // variable
                            List<StateChartInput> list = inputsBySource.get(pointer);
                            if (list != null)
                                for (StateChartInput in : list)
                                    in.write();
                        }
                    }
                }
                // Utils.log("runEngine", current);
                runEngine(current);

            }
            // run engine to the end ???
            if (!continueProducing()) {
                Utils.log("continueProducing", end, current);
                while (end > current && !p.isCanceled()) {
                    current = engine.timeLeap(processType == ProcessType.Continuous ? rate : end - current);
                    runEngine(current);
                }

                // exit
                engine.exit();
                if (scripting.hasFunction("onExit"))
                    scripting.invoke("onExit");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            setError(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    private void runEngine(long current) {
        // engine.printEvents();
        engine.runCycle();
        // engine.printVars();

        // engine.print();
        String activeState = engine.getActiveState();

        if (targetWriter instanceof ITextSamplesWriter) {
            if (!Utils.equals(activeState, value))
                ((ITextSamplesWriter) targetWriter).write(current, false, activeState);
        } else if (targetWriter instanceof IEventSamplesWriter) {
            if (!Utils.equals(activeState, value))
                ((IEventSamplesWriter) targetWriter).write(current, false, activeState);
        } else if (targetWriter instanceof IStructSamplesWriter) {
            boolean change = false;

            // state
            if (!Utils.equals(activeState, value)) {
                change = true;
                members[0].setValue(activeState);
                members[0].setValid(true);
            } else
                members[0].setValid(false);

            // calls
            if (!engine.executor.isEmpty()) {
                change = true;
                members[1].setValue(engine.executor.getAll());
                members[1].setValid(true);
                engine.executor.clear();
            } else {
                members[1].setValid(false);
            }

            // outputs
            for (StateChartOutput o : outputs) {
                change |= o.write();
            }

            // write
            if (change) {
                ((IStructSamplesWriter) targetWriter).write(current, false, members);
                changeWritten = true;
            } else {
                if (changeWritten && processType == ProcessType.Continuous)
                    ((IStructSamplesWriter) targetWriter).write(current, false, members);
                changeWritten = false;
            }
        }
        value = activeState;

    }

    // input / output
    class StateChartInput {

        ISamplePointer pointer;
        String name;
        int type;
        Object member;
        Object param;

        public static final int TYPE_VARIABLE = 0;
        public static final int TYPE_EVENT = 1;
        public static final int TYPE_MULTI_EVENT = 2;

        StateChartInput(ISamplePointer pointer, String name, int type, Object member, Object param) {
            this.pointer = pointer;
            this.name = name;
            this.type = type;
            this.member = member;
            this.param = param;

        }

        IReadableSamples getPointer() {
            return pointer;
        }

        String getName() {
            return name;
        }

        public void write() {

            if (type == TYPE_EVENT) {

                // struct or array
                if (member != null) {
                    if (!pointer.isNone() && pointer.hasMember(member)) {

                        // event & param value
                        Object value = pointer.valueOf(member);

                        // raise
                        engine.raiseEvent(name, value);
                    }
                } else {
                    if (!pointer.isNone()) {

                        Object value = pointer.val();

                        // raise
                        engine.raiseEvent(name, value);
                    }
                }
            } else if (type == TYPE_MULTI_EVENT) {

                // struct or array
                if (member != null) {
                    if (!pointer.isNone() && pointer.hasMember(member)) {

                        // event & param value
                        String event = pointer.formatOf(member, ISample.FORMAT_TEXT);
                        Object value = param != null ? pointer.valueOf(param) : null;

                        // raise
                        engine.raiseEvent(event, value);
                    }
                } else {
                    if (!pointer.isNone()) {
                        // event
                        String event = pointer.format(ISample.FORMAT_TEXT);

                        // raise
                        engine.raiseEvent(event);
                    }
                }
            } else if (type == TYPE_VARIABLE) {

                // struct or array
                if (member != null) {
                    if (!pointer.isNone() && pointer.hasMember(member)) {

                        // value
                        Object value = pointer.valueOf(getName());

                        // set
                        engine.setValue(name, value);
                    }
                }
                // primitive
                else {
                    if (!pointer.isNone()) {

                        // value
                        Object value = pointer.val();

                        // set
                        engine.setValue(name, value);
                    }
                }
            }
        }
    }

    class StateChartOutput {
        String name;
        int type;
        StructMember member;
        Object value;
        boolean validUntilChange;

        public static final int TYPE_VARIABLE = 0;
        public static final int TYPE_EVENT = 1;

        StateChartOutput(IStructSamplesWriter targetWriter, String name, int type) {
            this.name = name;
            this.type = type;

            if (type == TYPE_VARIABLE) {
                int mtype = engine.getVariableStructType(name);
                int mformat = engine.getVariableStructFormat(name);
                validUntilChange = true;
                member = targetWriter.createMember(null, -1, name, mtype | (validUntilChange ? STRUCT_MOD_VALID_UNTIL_CHANGE : 0), null, mformat);
            } else if (type == TYPE_EVENT) {
                int mtype = engine.getEventStructType(name);
                int mformat = engine.getEventStructFormat(name);
                member = targetWriter.createMember(null, -1, name, mtype, null, mformat);
            }
        }

        public boolean write() {

            if (type == TYPE_VARIABLE) {
                Object value = engine.getVariableStructValue(name);

                if (validUntilChange) {
                    if (!Utils.equals(this.value, value)) {
                        member.setValue(value);
                        this.value = value;
                        member.setValid(true);
                        return true;
                    } else {
                        member.setValid(false);
                        return false;
                    }
                } else {
                    if (value != null) {
                        member.setValue(value);
                        member.setValid(true);
                        return true;
                    } else {
                        member.setValid(false);
                        return false;
                    }
                }
            } else if (type == TYPE_EVENT) {
                if (engine.isRaised(this.name)) {
                    Object value = engine.getEventStructValue(name);
                    if (value != null) {
                        member.setValue(value);
                        member.setValid(true);
                        return true;
                    } else {
                        member.setValue("\u270B");
                        member.setValid(true);
                    }
                } else {
                    member.setValid(false);
                    return false;
                }
            }
            return false;
        }
    }

}
