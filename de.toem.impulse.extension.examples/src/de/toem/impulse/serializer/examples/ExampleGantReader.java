//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.serializer.examples;

import java.io.InputStream;

import de.toem.impulse.cells.record.Scope;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.domain.TimeBase;
import de.toem.impulse.samples.IEventSamplesWriter;
import de.toem.impulse.samples.IIntegerSamplesWriter;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISamples;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.samples.IStructSamplesWriter;
import de.toem.impulse.serializer.AbstractSingleDomainRecordReader;
import de.toem.impulse.serializer.ParseException;
import de.toem.impulse.values.StructMember;
import de.toem.pattern.threading.IProgress;

public class ExampleGantReader extends AbstractSingleDomainRecordReader {

    public ExampleGantReader() {
        super();
    }

    public ExampleGantReader(String id, InputStream in) {
        super(id, in);
    }

    @Override
    protected int isApplicable(String name, String contentType) {
        if (name != null && !name.equals("toem.gant.example"))
            return NOT_APPLICABLE;
        return 8; // need 8 bytes to check
        // return APPLICABLE;
    }

    @Override
    protected int isApplicable(byte[] buffer) {
        if (buffer[0] == '#' && buffer[1] == 'E' && buffer[2] == 'X' && buffer[3] == 'A')
            return APPLICABLE;
        return NOT_APPLICABLE;
    }

    @Override
    protected void parse(IProgress progress, InputStream in) throws ParseException {

        // Init the record
        initRecord("Example Record", TimeBase.ns);

        // We forget the input and create a record with scopes and some signals
        Scope signals = addScope(null, "Signals");
        Signal int1 = addSignal(signals, "Integer1", "An integer", ProcessType.Discrete, SignalType.Integer, SignalDescriptor.DEFAULT);


        Signal event1 = addSignal(signals, "Event1", "Simple events", 
                ProcessType.Discrete, SignalType.Event, SignalDescriptor.DEFAULT);
        Signal event2 = addSignal(signals, "Event2", "Enum events with string", 
                ProcessType.Discrete, SignalType.Event, SignalDescriptor.EventGannt);
        Signal event3 = addSignal(signals, "Event3", "Enum events with ints", 
                ProcessType.Discrete, SignalType.Event, SignalDescriptor.EventGannt);
        Signal event4 = addSignal(signals, "Event4", "2 Enums", 
                ProcessType.Discrete, SignalType.EventArray, new SignalDescriptor(SignalDescriptor.EVENT_CONTENT_GANTT,2,ISample.ACCURACY_DEFAULT,ISample.FORMAT_DEFAULT));
        Signal event5 = addSignal(signals, "Event5", "2 Enums", 
                ProcessType.Discrete, SignalType.EventArray, new SignalDescriptor(SignalDescriptor.EVENT_CONTENT_GANTT,2,ISample.ACCURACY_DEFAULT,ISample.FORMAT_DEFAULT));
        Signal event6 = addSignal(signals, "Event6", "2 Enums", 
                ProcessType.Discrete, SignalType.EventArray, new SignalDescriptor(SignalDescriptor.EVENT_CONTENT_GANTT,2,ISample.ACCURACY_DEFAULT,ISample.FORMAT_DEFAULT));
        Signal event7 = addSignal(signals, "Event7", "2 Enums", 
                ProcessType.Discrete, SignalType.EventArray, new SignalDescriptor(SignalDescriptor.EVENT_CONTENT_GANTT,2,ISample.ACCURACY_DEFAULT,ISample.FORMAT_DEFAULT));
        Signal struct1 = addSignal(signals, "Struct1", "A struct signal", ProcessType.Discrete, 
                SignalType.Struct, SignalDescriptor.StructGannt);
        Signal struct2 = addSignal(signals, "Struct2", "Another struct signal", ProcessType.Discrete, 
                SignalType.Struct,SignalDescriptor.StructGannt);
        
        // We start at 0 ns
        long t = 0; // 0 ns
        open(t);

        // get the writers
        IIntegerSamplesWriter integerWriter = (IIntegerSamplesWriter) getWriter(int1);

        // And continue until 1000 ns
        for (; t < 100000; t ++) {

            // write time as integer
            integerWriter.write(t, false, (int) ((int) 100 * Math.sin(t/1000.0)));
        }

         // simple events
        IEventSamplesWriter eventWriter = (IEventSamplesWriter) getWriter(event1);
        eventWriter.write(12000, false);
        eventWriter.write(22000, false);
        eventWriter.write(22000, false);
        
        // enum events with strings
        eventWriter = (IEventSamplesWriter) getWriter(event2);
        eventWriter.write(13000, false,"Start");
        eventWriter.write(14000, false,"Continue");
        eventWriter.write(15000, false,"Stop");
        eventWriter.write(25000, false);     
        eventWriter.write(30000, false,"Start");  
        eventWriter.write(44000, false,"Continue");
        eventWriter.write(45000, false,"Stop");
        eventWriter.write(55000, false);     

        
        // enum events with ints
        eventWriter = (IEventSamplesWriter) getWriter(event3);
        eventWriter.write(13000, false,1);
        eventWriter.write(14000, false,2);
        eventWriter.write(16000, false);  
        eventWriter.write(18000, false,3);
        eventWriter.write(25000, false,4);    
        
        // enum events with arrays
        eventWriter = (IEventSamplesWriter) getWriter(event4);
        eventWriter.setMember(0,"State", "STATE", ISample.FORMAT_TEXT);
        eventWriter.setMember(1,"Event", "EVENT", ISample.FORMAT_TEXT);
        eventWriter.write(10000, false,new String[]{null,"X1"});
        eventWriter.write(30000, false,new String[]{"Starting","X2"});
        eventWriter.attachAssociation(event6.getPath(), "Trigger//", 0);
        eventWriter.write(50000, false,new String[]{"Started","Off"});
        eventWriter.attachAssociation(event5.getPath(), "Trigger//", 5000);
        eventWriter.write(60000, false);
        eventWriter.attachAssociation(event5.getPath(), "Trigger//", 5000);
        eventWriter.write(70000, false,new String[]{"Running","Of2"}); 
        eventWriter.write(80000, false,new String[]{"Stopping","Of3"});    
        eventWriter.write(90000, false,new String[]{"Setopped","Of4"});    
        eventWriter.write(95000, false,new String[]{"Down","X1"});
        
        eventWriter = (IEventSamplesWriter) getWriter(event5);
        eventWriter.setMember(0,"State", "STATE", ISample.FORMAT_TEXT);
        eventWriter.setMember(1,"Event", "EVENT", ISample.FORMAT_TEXT);
        eventWriter.write(10000, false,new String[]{null,"X1"});
        eventWriter.write(50000, false,new String[]{"Starting","X2"});
        eventWriter.write(55000, false,new String[]{"Started","Off"});
        eventWriter.write(65000, false,new String[]{null,"X1"});
        eventWriter.write(68000, false,new String[]{"Running","Of2"}); 
        eventWriter.write(70000, false,new String[]{"Stopping","Of3"});    
        eventWriter.write(90000, false,new String[]{"Setopped","Of4"});    
        eventWriter.write(95000, false,new String[]{"Down","X1"});
        
        eventWriter = (IEventSamplesWriter) getWriter(event6);
        eventWriter.setMember(0,"State", "STATE", ISample.FORMAT_TEXT);
        eventWriter.setMember(1,"Event", "EVENT", ISample.FORMAT_TEXT);
        eventWriter.write(10000, false,new String[]{null,"X1"});
        eventWriter.write(15000, false,new String[]{"Starting","X2"});
        eventWriter.write(18000, false,new String[]{"Started","Off"});
        eventWriter.write(22000, false,new String[]{null,"X1"});
        eventWriter.write(24000, false,new String[]{"Running","Of2"}); 
        eventWriter.write(30000, false,new String[]{"Stopping","Of3"});    
        eventWriter.write(60000, false,new String[]{"Setopped","Of4"});    
        eventWriter.write(70000, false,new String[]{"Down","X1"});
        
        eventWriter = (IEventSamplesWriter) getWriter(event7);
        eventWriter.setMember(0,"State", "STATE", ISample.FORMAT_TEXT);
        eventWriter.setMember(1,"Event", "EVENT", ISample.FORMAT_TEXT);
        eventWriter.write(20000, false,new String[]{null,"X1"});
        eventWriter.write(35000, false,new String[]{"Starting","X2"});
        eventWriter.write(3000, false,new String[]{"Started","Off"});
        eventWriter.write(42000, false,new String[]{null,"X1"});
        eventWriter.attachAssociation(event4.getPath(), "Reset//", -12000);
        eventWriter.write(44000, false,new String[]{"Running","Of2"}); 
        eventWriter.write(50000, false,new String[]{"Stopping","Of3"});    
        eventWriter.write(80000, false,new String[]{null,"Of4"});    
        eventWriter.write(95000, false,new String[]{"Down","X1"});
        
        // structs 
        StructMember members[] = new StructMember[3];
        members[0] = new StructMember("State", StructMember.STRUCT_TYPE_LOCAL_ENUM, 
                "STATE", ISamples.FORMAT_DEFAULT);
        members[1] = new StructMember("Value", StructMember.STRUCT_TYPE_ENUM, 
                null, ISamples.FORMAT_DEFAULT);
        members[2] = new StructMember("Other", StructMember.STRUCT_TYPE_INTEGER, 
                null, ISamples.FORMAT_DEFAULT);
        
        IStructSamplesWriter structWriter = (IStructSamplesWriter) getWriter(struct1);
        members[0].setValue(null);
        members[1].setValue("Inspect");
        members[2].setValue(100);
        structWriter.write(10000, false, members);
        members[0].setValue("On");
        members[1].setValue("First");
        members[2].setValue(200);
        structWriter.write(20000, false, members);
        structWriter.attachAssociation(struct2.getPath(), "Delayed", 20000);
        members[0].setValue("Running");
        members[1].setValue("First");
        members[2].setValue(700);
        structWriter.write(30000, false, members);
        members[0].setValue("Waiting");
        members[1].setValue("Second");
        members[2].setValue(750);
        structWriter.write(40000, false, members);
        members[0].setValue("Running");
        members[1].setValue("Second");
        members[2].setValue(750);
        structWriter.write(50000, false, members);
        members[0].setValue("Stopping");
        members[1].setValue("Third");
        members[2].setValue(950);
        structWriter.write(60000, false, members);
        members[0].setValue(null);
        members[1].setValue("Third");
        members[2].setValue(950);
        structWriter.write(70000, false, members);
        
        structWriter = (IStructSamplesWriter) getWriter(struct2);
        members[0].setValue(null);
        members[1].setValue("Inspect");
        members[2].setValue(100);
        structWriter.write(30000, false, members);
        members[0].setValue("On");
        members[1].setValue("First");
        members[2].setValue(200);
        structWriter.write(40000, false, members);
        members[0].setValue("Running");
        members[1].setValue("First");
        members[2].setValue(700);
        structWriter.write(50000, false, members);
        members[0].setValue("Waiting");
        members[1].setValue("Second");
        members[2].setValue(750);
        structWriter.write(60000, false, members);
        members[0].setValue("Running");
        members[1].setValue("Second");
        members[2].setValue(750);
        structWriter.write(70000, false, members);
        members[0].setValue("Stopping");
        members[1].setValue("Third");
        members[2].setValue(950);
        structWriter.write(80000, false, members);
        members[0].setValue(null);
        members[1].setValue("Third");
        members[2].setValue(950);
        structWriter.write(90000, false, members);
        
        // And close finally
        close(100000);  
    }


}
