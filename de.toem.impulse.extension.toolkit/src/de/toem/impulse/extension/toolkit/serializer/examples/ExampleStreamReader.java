//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.toolkit.serializer.examples;

import java.io.InputStream;

import de.toem.impulse.cells.ports.IPortProgress;
import de.toem.impulse.cells.record.Scope;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.domain.TimeBase;
import de.toem.impulse.samples.IFloatSamplesWriter;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.serializer.AbstractSingleDomainRecordReader;
import de.toem.impulse.serializer.ParseException;
import de.toem.pattern.element.ICover;
import de.toem.pattern.threading.IProgress;

public class ExampleStreamReader extends AbstractSingleDomainRecordReader {

    long t;
    int changed;    
    Signal float1;
    Signal float2;
    Signal float3;
            
    public ExampleStreamReader() {
        super();
    }

    public ExampleStreamReader(String id, InputStream in) {
        super(id, in);
    }

    @Override
    protected int isApplicable(String name, String contentType) {
        if (name != null && !name.equals("toem.float.example"))
            return NOT_APPLICABLE;
        return 8; // need 8 bytes to check
    }

    @Override
    protected int isApplicable(byte[] buffer) {
        if (buffer[0] == '#' && buffer[1] == 'E' && buffer[2] == 'X' && buffer[3] == 'A')
            return APPLICABLE;
        return NOT_APPLICABLE;
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    synchronized public ICover flush() {
        changed = CHANGED_NONE;
        return super.doFlush(t);
    }

    @Override
    public int hasChanged() {
        return changed;
    }
    
    @Override
    protected void parse(IProgress progress, InputStream in) throws ParseException {

        // Init the record
        initRecord("Example Record", TimeBase.ns);

        // We forget the input and create a record with scopes and 3 signals
        Scope system = addScope(null, "System");  // null means at root
        float1 = addSignal(system, "Signal1", "My first float Signal", ProcessType.Discrete, SignalType.Float, SignalDescriptor.Float64);
        float2 = addSignal(system, "Signal2", "My second float Signal", ProcessType.Discrete, SignalType.Float, SignalDescriptor.Float64);
        Scope another = addScope(system, "Another");
        float3 = addSignal(another, "Signal3", "Another float", ProcessType.Discrete, SignalType.Float, SignalDescriptor.Float64);

        // mark what has been changed
        changed = CHANGED_RECORD;

    // wait after header parsing
    if (progress instanceof IPortProgress)
        synchronized (progress) {
            while (!((IPortProgress) progress).isStreaming() && !progress.isCanceled())
                try {
                    progress.wait(250);
                } catch (InterruptedException e) {
                }
        }

              
        // if you find a problem, throw an exception
        if (false)
            throw new ParseException("Not Valid");

        // We start at 0 ns
        t = 0; // 0 ns
        open(t);

        // And continue until 1000 ns
        for (; t < 100000; t += 10) {
            
            // but now the input takes some time
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }

            // call a synchronized method, so feed and sync do not conflict
            parse();
        }
       

        // And close finally
        close(t);
    }
    
    synchronized private void parse() throws ParseException {
        
        // write the sin and cos data
        ((IFloatSamplesWriter) getWriter(float1)).write(t, Math.sin((t + 100) / 3000.0) * 10.0);
        ((IFloatSamplesWriter) getWriter(float2)).write(t, Math.cos((t + 200) / 1000.0) * 10.0);
        ((IFloatSamplesWriter) getWriter(float3)).write(t, Math.sin((t + 300) / 6000.0) * 10.0 + 10.00001);
        
        // mark what has changed
        changed = CHANGED_SIGNALS;
    }
}
