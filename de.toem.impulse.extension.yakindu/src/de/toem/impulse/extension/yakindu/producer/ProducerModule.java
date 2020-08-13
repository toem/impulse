package de.toem.impulse.extension.yakindu.producer;

import org.yakindu.base.expressions.interpreter.IOperationExecutor;
import org.yakindu.sct.simulation.core.engine.scheduling.ITimeTaskScheduler;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

public class ProducerModule implements Module {

    ITimeTaskScheduler scheduler;
    IOperationExecutor executor;

    public ProducerModule(ITimeTaskScheduler scheduler,IOperationExecutor executor) {
        super();
        this.scheduler = scheduler;
        this.executor = executor;
    }
    
    @Override
    public void configure(Binder binder) {
        binder.bind(ITimeTaskScheduler.class).toInstance(scheduler);
        
        Multibinder<IOperationExecutor> mockupBinder = Multibinder.newSetBinder(binder, IOperationExecutor.class);
        mockupBinder.addBinding().toInstance(executor);
    }

}
