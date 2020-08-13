
package de.toem.impulse.extension.yakindu.producer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.yakindu.base.types.Type;
import org.yakindu.base.types.typesystem.ITypeSystem;
import org.yakindu.sct.domain.extension.DomainRegistry;
import org.yakindu.sct.domain.extension.IDomain;
import org.yakindu.sct.model.sexec.ExecutionFlow;
import org.yakindu.sct.model.sexec.naming.INamingService;
import org.yakindu.sct.model.sexec.transformation.IModelSequencer;
import org.yakindu.sct.model.sgraph.RegularState;
import org.yakindu.sct.model.sgraph.Statechart;
import org.yakindu.sct.model.sruntime.ExecutionContext;
import org.yakindu.sct.model.sruntime.ExecutionEvent;
import org.yakindu.sct.model.sruntime.ExecutionVariable;
import org.yakindu.sct.simulation.core.engine.scheduling.ITimeTaskScheduler;
import org.yakindu.sct.simulation.core.sexec.container.IExecutionContextInitializer;
import org.yakindu.sct.simulation.core.sexec.interpreter.IExecutionFlowInterpreter;
import org.yakindu.sct.simulation.core.util.ExecutionContextExtensions;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

import de.toem.basics.core.Utils;
import de.toem.impulse.extension.yakindu.chart.StateChart;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.convert.SampleConverter;

public class Engine {

    @Inject
    private IModelSequencer sequencer;
    @Inject
    public IExecutionFlowInterpreter interpreter;
    @Inject
    public ExecutionContext context;
    @Inject
    public IExecutionContextInitializer initializer;

    public ITimeTaskScheduler scheduler;
    public OperationExecutor executor;

    public ExecutionFlow flow;

    @Inject
    private ITypeSystem typeSystem;
//    @Inject
//    private INamingService namingService;
    
    public Engine(Statechart statechart, boolean eventDriven, long start) {

        // producer module
        scheduler = new Scheduler(eventDriven, start);
        executor = new OperationExecutor();
        ProducerModule producerModule = new ProducerModule(scheduler, executor);

        // domain module
        IDomain domain = DomainRegistry.getDomain(statechart);
        Module domainModule = domain.getModule(IDomain.FEATURE_SIMULATION);

        // inject
        Injector injector = Guice.createInjector(producerModule, domainModule);
        injector.injectMembers(this);
       
        // initialize
        flow = sequencer.transform(statechart);
        initializer.initialize(context, flow);
        interpreter.initialize(flow, context);
    }

    public ExecutionContext context() {
        return context;
    }

    public ExecutionFlow flow() {
        return flow;
    }

    public void enter() {
        interpreter.enter();
        printVars();
        printEvents();
        printActiveStates();
    }

    public void runCycle() {
        interpreter.runCycle();
    }

    public void exit() {
        interpreter.exit();
    }

    public boolean isActive() {
        return interpreter.isActive();
    }

    public boolean isFinal() {
        return interpreter.isFinal();
    }

    public long timeLeap(long duration) {
        scheduler.timeLeap(duration);
        return scheduler.getCurrentTime();
    }

    public long current() {
        return scheduler.getCurrentTime();
    }

    public void print() {
        // Utils.log("----------------------------------------------");
        // Utils.log("Time", current());
        // Utils.log("Engine", interpreter.isActive(), interpreter.isFinal());
        printActiveStates();
        printVars();
        // printEvents();
    }

    // public List<String> getAllVariableNames() {
    // List<String> list = new ArrayList<>();
    // for (ExecutionVariable v : context().getAllVariables())
    // list.add(v.getName());
    // return list;
    // }

    public void printVars() {
        for (ExecutionVariable v : context().getAllVariables())
            Utils.log("Vars:", v.getFqName(), v.getType(), v.getValue(), v.getClass().getSimpleName());
    }

    public void printEvents() {
        for (ExecutionEvent e : context().getAllEvents())
            Utils.log("Event:", e.getName(), e.getValue(), e.isRaised(), e.getType());
    }

    public void printActiveStates() {
        for (RegularState s : new ExecutionContextExtensions().getAllActiveStates(context))
            Utils.log("Active:", s.getName());
    }

//    public long getInteger(String varName) {
//        ExecutionVariable variable = context().getVariable(varName);
//        return (Long) variable.getValue();
//    }
//
//    public boolean getBoolean(String varName) {
//        ExecutionVariable variable = context().getVariable(varName);
//        return (Boolean) variable.getValue();
//    }
//
//    public Double getReal(String varName) {
//        ExecutionVariable variable = context().getVariable(varName);
//        return (Double) variable.getValue();
//    }
//
//    public String getString(String varName) {
//        ExecutionVariable variable = context().getVariable(varName);
//        return (String) variable.getValue();
//    }
//
//    
//    public long setInteger(String varName, long v) {
//        context().getVariable(varName).setValue((Long) v);
//        return v;
//    }
//
//    public boolean setBoolean(String varName, boolean v) {
//        context().getVariable(varName).setValue((Boolean) v);
//        return v;
//    }
//
//    public double setReal(String varName, double v) {
//        context().getVariable(varName).setValue((Double) v);
//        return v;
//    }
//
//    public String setString(String varName, String v) {
//        context().getVariable(varName).setValue((String) v);
//        return v;
//    }

    // -> Assertion methods...
//    public void assertVarValue(String variableName, Object value) {
//        ExecutionVariable variable = context().getVariable(variableName);
//        // assertNotNull("Variable '" + variableName + "' is not defined", variable);
//        // assertEquals(value, variable.getValue());
//    }

    public boolean isStateActive(String stateName) {
        Assert.isNotNull(stateName);
        List<RegularState> allActiveStates = new ExecutionContextExtensions().getAllActiveStates(context);
        for (RegularState regularState : allActiveStates) {
            if (stateName.equals(regularState.getName())) {
                return true;
            }
        }
        return false;
    }

    public String getActiveState() {
        String states = "";
        List<RegularState> allActiveStates = new ExecutionContextExtensions().getAllActiveStates(context);
        for (RegularState regularState : allActiveStates) {
            states = (states.isEmpty() ? "" : states + "; ") + StateChart.getElementName(regularState);
        }
        return states;
    }

    public List<String> getActiveStates() {
        String states = "";
        List<RegularState> allActiveStates = new ExecutionContextExtensions().getAllActiveStates(context);
        List<String> list = new ArrayList<>();
        for (RegularState regularState : allActiveStates) {
            list.add(0, StateChart.getElementName(regularState));
        }
        return list;
    }

    public List<String> getAllEventNames() {
        List<String> list = new ArrayList<>();
        for (ExecutionEvent v : context().getAllEvents())
            list.add(v.getFqName());
        return list;
    }
    
    public boolean hasEvent(String name) {
        return context().getEvent(name) != null;
    }
    
    public boolean isEventReadOnly(String name) {
        ExecutionEvent event = context().getEvent(name);
        return event != null && !event.isWritable();
    }
    
    public void raiseEvent(String eventName) {
        ExecutionEvent event = context().getEvent(eventName);
        if (event != null)
            event.setRaised(true);
    }

    public void raiseEvent(String eventName, Object value) {
        ExecutionEvent event = context().getEvent(eventName);
        
        if (event != null) {
            event.setRaised(true);
            if (event.getType() != null) {
                switch (javaType(event.getType())) {
                case "Void":
                    break;
                case "Boolean":
                    value = SampleConverter.instance.booleanValue(value);
                    break;
                case "Long":
                    value = SampleConverter.instance.intValue(value);
                    break;
                case "Double":
                    value = SampleConverter.instance.floatValue(value);
                    break;
                case "String":
                    value = SampleConverter.instance.stringValue(value);
                    break;
                default:
                    value = SampleConverter.instance.stringValue(value);
                    break;
                }
                event.setValue(value);
            }
        }
    }

    public boolean isRaised(String eventName) {
        ExecutionEvent event = context().getEvent(eventName);
        if (event != null) {
            return event.isRaised();
        }
        return false;
    }


    public int getEventStructType(String name) {
        ExecutionEvent event = context().getEvent(name);

        if (event != null && event.getType() != null) {
            switch (javaType(event.getType())) {
            case "Void":
                return ISample.STRUCT_TYPE_UNKNOWN;
            case "Boolean":
                return ISample.STRUCT_TYPE_INTEGER;
            case "Long":
                return ISample.STRUCT_TYPE_INTEGER;
            case "Double":
                return ISample.STRUCT_TYPE_FLOAT;
            case "String":
                return ISample.STRUCT_TYPE_TEXT;
            default:
                return ISample.STRUCT_TYPE_TEXT;
            }
        }
        return ISample.STRUCT_TYPE_LOCAL_ENUM;
    }

    public int getEventStructFormat(String name) {
        ExecutionEvent event = context().getEvent(name);
        if (event != null && event.getType() != null) {
            switch (javaType(event.getType())) {
            case "Void":
                return ISample.FORMAT_DEFAULT;
            case "Boolean":
                return ISample.FORMAT_BOOLEAN;
            case "Long":
                return ISample.FORMAT_DEFAULT;
            case "Double":
                return ISample.FORMAT_DEFAULT;
            case "String":
                return ISample.FORMAT_DEFAULT;
            default:
                return ISample.FORMAT_DEFAULT;
            }
        }
        return ISample.FORMAT_TEXT;
    }

    public Object getEventStructValue(String name) {
        ExecutionEvent event = context().getEvent(name);
        if (event != null && event.getType() != null) {
            Object val = event.getValue();
            switch (javaType(event.getType())) {
            case "Void":
                return null;
            case "Boolean":
                if (val instanceof Boolean)
                    return val;
                break;
            case "Long":
                if (val instanceof Integer || val instanceof Long)
                    return val;
                if (val instanceof Number)
                    return ((Number) val).intValue();
                break;
            case "Double":
                if (val instanceof Float || val instanceof Double)
                    return val;
                break;
            case "String":
                if (val instanceof String)
                    return val;
                break;
            default:
                return val != null ? String.valueOf(val) : null;
            }

        }
        return null;
    }
    
    public List<String> getAllVariableNames() {
        List<String> list = new ArrayList<>();
        for (ExecutionVariable v : context().getAllVariables())
            list.add(v.getFqName());
        return list;
    }

    public boolean hasVariable(String name) {
        return context().getVariable(name) != null;
    }

    public boolean isVariableReadOnly(String name) {
        ExecutionVariable var = context().getVariable(name);
        return var != null && !var.isWritable();
    }

    protected boolean isType(Type type, String typeName) {
        return typeSystem.isSame(type, typeSystem.getType(typeName));
    }

    public void setValue(String name, Object value) {
        ExecutionVariable var = context().getVariable(name);
        if (var != null && var.getType() != null) {
            switch (javaType(var.getType())) {
            case "Void":
                break;
            case "Boolean":
                value = SampleConverter.instance.booleanValue(value);
                break;
            case "Long":
                value = SampleConverter.instance.intValue(value);
                break;
            case "Double":
                value = SampleConverter.instance.floatValue(value);
                break;
            case "String":
                value = SampleConverter.instance.stringValue(value);
                break;
            default:
                value = SampleConverter.instance.stringValue(value);
                break;
            }
            var.setValue(value);
        }
    }

    public int getVariableStructType(String name) {
        ExecutionVariable var = context().getVariable(name);

        if (var != null && var.getType() != null) {
            switch (javaType(var.getType())) {
            case "Void":
                return ISample.STRUCT_TYPE_UNKNOWN;
            case "Boolean":
                return ISample.STRUCT_TYPE_INTEGER;
            case "Long":
                return ISample.STRUCT_TYPE_INTEGER;
            case "Double":
                return ISample.STRUCT_TYPE_FLOAT;
            case "String":
                return ISample.STRUCT_TYPE_TEXT;
            default:
                return ISample.STRUCT_TYPE_TEXT;
            }
        }
        return ISample.STRUCT_TYPE_UNKNOWN;
    }

    public int getVariableStructFormat(String name) {
        ExecutionVariable var = context().getVariable(name);
        if (var != null && var.getType() != null) {
            switch (javaType(var.getType())) {
            case "Void":
                return ISample.FORMAT_DEFAULT;
            case "Boolean":
                return ISample.FORMAT_BOOLEAN;
            case "Long":
                return ISample.FORMAT_DEFAULT;
            case "Double":
                return ISample.FORMAT_DEFAULT;
            case "String":
                return ISample.FORMAT_DEFAULT;
            default:
                return ISample.FORMAT_DEFAULT;
            }
        }
        return ISample.FORMAT_DEFAULT;
    }

    public Object getVariableStructValue(String name) {
        ExecutionVariable var = context().getVariable(name);
        if (var != null && var.getType() != null) {
            Object val = var.getValue();
            switch (javaType(var.getType())) {
            case "Void":
                return null;
            case "Boolean":
                if (val instanceof Boolean)
                    return val;
                break;
            case "Long":
                if (val instanceof Integer || val instanceof Long)
                    return val;
                if (val instanceof Number)
                    return ((Number) val).intValue();
                break;
            case "Double":
                if (val instanceof Float || val instanceof Double)
                    return val;
                break;
            case "String":
                if (val instanceof String)
                    return val;
                break;
            default:
                return val != null ? String.valueOf(val) : null;
            }

        }
        return null;
    }

    private String javaType(Type type) {
        if (type != null)

            type = type.getOriginType();
        if (isType(type, ITypeSystem.VOID)) {
            return "Void";
        }
        if (isType(type, ITypeSystem.INTEGER)) {
            return "Long";
        }
        if (isType(type, ITypeSystem.REAL)) {
            return "Double";
        }
        if (isType(type, ITypeSystem.BOOLEAN)) {
            return "Boolean";
        }
        if (isType(type, ITypeSystem.STRING)) {
            return "String";
        }

        return "";

    }

}
