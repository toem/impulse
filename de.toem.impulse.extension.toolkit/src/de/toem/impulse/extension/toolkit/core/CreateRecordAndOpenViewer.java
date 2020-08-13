package de.toem.impulse.extension.toolkit.core;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.utils.EditorUtils;
import de.toem.impulse.cells.record.Record;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.domain.TimeBase;
import de.toem.impulse.samples.IIntegerSamplesWriter;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.samples.ISingleDomainRecordGenerator;
import de.toem.impulse.serializer.SingleDomainRecordGenerator;
import de.toem.pattern.element.IElement;
import de.toem.pattern.element.constant.ConstantCellElement;
import de.toem.pattern.threading.Actives;
import de.toem.pattern.threading.IExecutable;
import de.toem.pattern.threading.IProgress;

public class CreateRecordAndOpenViewer {

    public static void createRecordAndOpenViewer() {
        Actives.runInMain(new IExecutable(){

            @Override
            public void execute(IProgress p) {

                // create record
                Record record = new Record();
                final ISingleDomainRecordGenerator g = new SingleDomainRecordGenerator(record);
                g.initRecord("my record", TimeBase.ms);
                
                // add signal
                final Signal s0 = g.addSignal(null, "my signal", null, ProcessType.Discrete, SignalType.Integer, SignalDescriptor.DEFAULT);
                g.open(0);
                ((IIntegerSamplesWriter) g.getWriter(s0)).write(0, false, 1);
                ((IIntegerSamplesWriter) g.getWriter(s0)).write(1000, false, 10);
                ((IIntegerSamplesWriter) g.getWriter(s0)).write(2000, false, 20);
                g.close(10000);
                g.apply();
                
                // create element
                final ConstantCellElement element = new ConstantCellElement(IElement.DOCUMENT,record){
                    @Override
                    public void setHint(String key, String value) {
                        super.setHint(key, value);
                        
                        // log viewer changes of the user
//                        Utils.log(key,value);
                    }
                };
                
                // set viewer hints
                element.setHint("de.toem.impulse.viewer.samples.showCursorDetails", "true");
                
                // open editor
                EditorUtils.openEditor("de.toem.impulse.editor.records",element,false);
                
                // modify and update the signal after 10s
                Actives.runInMain(new IExecutable(){

                    @Override
                    public void execute(IProgress p) {
                        g.open(0);
                        ((IIntegerSamplesWriter) g.getWriter(s0)).write(0, false, 100);
                        ((IIntegerSamplesWriter) g.getWriter(s0)).write(1000, false, 50);
                        ((IIntegerSamplesWriter) g.getWriter(s0)).write(2000, false, 10);
                        g.close(10000);
                        g.apply();
                        
                        element.fireElementResetted();
                        
                    }},10000);
            }});              
    }
}
