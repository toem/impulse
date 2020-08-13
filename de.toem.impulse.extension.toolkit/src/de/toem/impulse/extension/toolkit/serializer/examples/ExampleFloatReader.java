//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.toolkit.serializer.examples;

import java.io.InputStream;
import java.math.BigDecimal;

import de.toem.impulse.cells.record.Scope;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.domain.TimeBase;
import de.toem.impulse.samples.IFloatSamplesWriter;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.serializer.AbstractSingleDomainRecordReader;
import de.toem.impulse.serializer.ParseException;
import de.toem.pattern.threading.IProgress;

public class ExampleFloatReader extends AbstractSingleDomainRecordReader {

    public ExampleFloatReader() {
        super();
    }

    public ExampleFloatReader(String id, InputStream in) {
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
    protected void parse(IProgress progress, InputStream in) throws ParseException {

// Init the record
initRecord("Example Record", TimeBase.ns);

// We forget the input and create a record with scopes and 5 signals
Scope system = addScope(null, "System");  // null means at root
Signal float1 = addSignal(system, "Signal1", "My first float Signal (limitet to 32 bits)", ProcessType.Discrete, SignalType.Float, SignalDescriptor.DEFAULT);
Signal float2 = addSignal(system, "Signal2", "My second float Signal", ProcessType.Discrete, SignalType.Float, SignalDescriptor.DEFAULT);
Scope another = addScope(system, "Another");
Signal float3 = addSignal(another, "Signal3", "Another float (limitet to 32 bits)", ProcessType.Discrete, SignalType.Float, SignalDescriptor.DEFAULT);
Signal float4 = addSignal(another, "Signal4", "Rectangle", ProcessType.Discrete, SignalType.Float, SignalDescriptor.DEFAULT);
Signal float5 = addSignal(another, "Signal5", "XY", ProcessType.Discrete, SignalType.FloatArray, new SignalDescriptor(ISample.CONTENT_DEFAULT,2,ISample.FLOAT_ACCURACY_64,ISample.FORMAT_DEFAULT));

        
        // if you find a problem, throw an exception
        if (false)
            throw new ParseException("Not Valid");

        // We start at 0 ns
        long t = 0; // 0 ns
        open(t);

        // add array member informations
        getWriter(float5).setMember(0,"X",0, null, ISample.FORMAT_DEFAULT);
        getWriter(float5).setMember(1,"Y",0, null, ISample.FORMAT_DEFAULT);
        
        // And continue until 1000 ns
        for (; t < 100000; t += 10) {
            
            // write the sin and cos data
            ((IFloatSamplesWriter) getWriter(float1)).write(t, false,Math.sin((t + 100) / 3000.0) * 10.0);
            ((IFloatSamplesWriter) getWriter(float2)).write(t, false,Math.cos((t + 200) / 1000.0) * 10.0+Math.cos((t + 400) / 100.0) * 3.0+Math.signum(Math.cos((t + 400) / 50.0)) * 1.0+Math.cos((t + 100) / 10.0) * 2.0+Math.random()*2.5);
            ((IFloatSamplesWriter) getWriter(float3)).write(t, false,Math.sin((t + 300) / 6000.0) * 10.0 + 10.00001);
            ((IFloatSamplesWriter) getWriter(float4)).write(t, false,Math.signum(Math.cos((t + 400) / 500.0)) * 1.0 > 0 ? new BigDecimal("13.234563456734562323454534355345345345345234234234234"):new BigDecimal("0.030034004340344000340043430043434000343430023423400234234"));
            double f =  Math.sin(t  / 30000.0) * 1;
            ((IFloatSamplesWriter) getWriter(float5)).write(t, false, new double[]{Math.sin((t + 100) / 3000.0) * 10.0,Math.cos((t + 100) / 3000.0) * 10.0 * f});
        }


        // And close finally
        close(t);
    }
}
