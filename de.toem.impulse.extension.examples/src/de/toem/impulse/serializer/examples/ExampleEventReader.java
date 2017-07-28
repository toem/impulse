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
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.serializer.AbstractSingleDomainRecordReader;
import de.toem.impulse.serializer.ParseException;
import de.toem.pattern.threading.IProgress;

public class ExampleEventReader extends AbstractSingleDomainRecordReader {

    public ExampleEventReader() {
        super();
    }

    public ExampleEventReader(String id, InputStream in) {
        super(id, in);
    }

    @Override
    protected int isApplicable(String name, String contentType) {
        if (name != null && !name.equals("toem.event.example"))
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
 
        Signal event1 = addSignal(signals, "Event1", "Simple events", 
                ProcessType.Discrete, SignalType.Event, SignalDescriptor.DEFAULT);
        Signal event2 = addSignal(signals, "Event2", "Enum events with ints", 
                ProcessType.Discrete, SignalType.Event, SignalDescriptor.DEFAULT);
        Signal event3 = addSignal(signals, "Event3", "Enum events with strings", 
                ProcessType.Discrete, SignalType.Event, SignalDescriptor.DEFAULT);
        Signal event4 = addSignal(signals, "Event4", "2 Enums", 
                ProcessType.Discrete, SignalType.EventArray, new SignalDescriptor(SignalDescriptor.STRUCT_CONTENT_GANTT,2,ISample.ACCURACY_DEFAULT,ISample.FORMAT_DEFAULT));
 
        // We start at 0 ns
        open(0);


        // simple events
        IEventSamplesWriter eventWriter = (IEventSamplesWriter) getWriter(event1);
        eventWriter.write(12000, false);
        eventWriter.write(22000, false);
        
        // enum events with strings
        eventWriter = (IEventSamplesWriter) getWriter(event2);
        eventWriter.write(13000, false,"Start");
        eventWriter.write(14000, false,"Continue");
        eventWriter.write(15000, false,"Stop");
        eventWriter.write(25000, false,"Destroy");     
        eventWriter.write(30000, false,"Start");  

        // enum events with ints
        eventWriter = (IEventSamplesWriter) getWriter(event3);
        for (int n=0;n<32;n++)
        eventWriter.write(0+n*2000, false,n);
  
        
        // enum events with arrays
        eventWriter = (IEventSamplesWriter) getWriter(event4);
        eventWriter.setMember(0,"State", "STATE", ISample.FORMAT_TEXT);
        eventWriter.setMember(1,"Event", "EVENT", ISample.FORMAT_TEXT);
        eventWriter.write(10000, false,new String[]{null,"X1"});
        eventWriter.write(30000, false,new String[]{"Starting","X2"});
        eventWriter.write(50000, false,new String[]{"Started","Off"});
        eventWriter.write(60000, false,new String[]{null,"X1"});
         eventWriter.write(70000, false,new String[]{"Running","Of2"}); 
        eventWriter.write(80000, false,new String[]{"Stopping","Of3"});    
        eventWriter.write(90000, false,new String[]{"Setopped","Of4"});    
        eventWriter.write(95000, false,new String[]{"Down","X1"});
        
 
        
        // And close finally
        close(100000);
       
            
    }


}
