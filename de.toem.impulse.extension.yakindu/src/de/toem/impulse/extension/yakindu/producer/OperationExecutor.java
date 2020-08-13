package de.toem.impulse.extension.yakindu.producer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.yakindu.base.expressions.expressions.ArgumentExpression;
import org.yakindu.base.expressions.interpreter.AbstractOperationExecutor;
import org.yakindu.base.expressions.interpreter.IExecutionSlotResolver;
import org.yakindu.base.expressions.interpreter.IOperationExecutor;
import org.yakindu.base.types.Operation;
import org.yakindu.sct.model.sruntime.ExecutionContext;
import org.yakindu.sct.model.sruntime.ExecutionSlot;
import org.yakindu.sct.model.sruntime.ExecutionVariable;

import com.google.inject.Inject;

import de.toem.basics.core.Utils;
import de.toem.impulse.scripting.Scripting;

public class OperationExecutor extends AbstractOperationExecutor implements IOperationExecutor {

    List<String> calls = new ArrayList<>();

    @Inject
    protected IExecutionSlotResolver resolver;

    public Scripting scripting;
    public Map<String, Boolean> scriptFunctions = new HashMap<>();

    // Yakindu 3.5
    public boolean canExecute(ArgumentExpression var1) {
        return true;
    }

    // Yakindu 4.0
    public boolean canExecute(ArgumentExpression arg0, ExecutionContext arg1) {
        return true;
    }
    
    @Override
    public Object execute(ArgumentExpression expression, ExecutionContext context) {
        Operation operation = this.getOperation(expression);
        String name = operation.getName();
        Optional<ExecutionSlot> slot = resolver.resolve(context, expression);
        ExecutionVariable var = slot.isPresent() && slot.get() instanceof ExecutionVariable ? (ExecutionVariable) slot.get() : null;
        if (var != null) {

            Object[] arguments = this.executeArguments(expression.getArguments(), context, operation);
//            if (arguments != null)
//                for (int n = 0; n < arguments.length; n++) {
//                    if (arguments[n] instanceof CompositeSlot) {
//                        ((CompositeSlot) arguments[n]).get
//                        arguments[n] = ((CompositeSlot) arguments[n]).getValue();
//                    }
//                    if (arguments[n] instanceof ExecutionSlot) {
//                        arguments[n] = ((ExecutionSlot) arguments[n]).getValue();
//                    }
//                }

            // scripting
            if (!scriptFunctions.containsKey(name))
                scriptFunctions.put(name, scripting.hasFunction(name));
            if (scriptFunctions.get(name) == Boolean.TRUE) {

                var.setValue(scripting.invoke(name, arguments));
            }

            // create call string
            String result = var != null ? String.valueOf(var.getValue()) : "";
            String argumentList = "";
            if (arguments != null)
                for (Object a : arguments)
                    argumentList += (Utils.isEmpty(argumentList) ? "" : ",") + String.valueOf(a);
            String text = name + "(" + argumentList + ")->" + result;
            calls.add(text);

            return var.getValue();
        }
        return null;
    }

    public List<String> get() {
        return calls;
    }

    public String getAll() {
        String all = null;
        for (String c : get())
            all = all == null ? c : all + ";" + c;
        return all;
    }

    public boolean isEmpty() {
        return calls.isEmpty();
    }

    public void clear() {
        calls.clear();
    }


}
