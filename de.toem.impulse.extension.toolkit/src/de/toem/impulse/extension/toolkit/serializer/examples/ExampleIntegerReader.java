//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.toolkit.serializer.examples;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import de.toem.impulse.cells.record.Scope;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.cells.view.FolderConfiguration;
import de.toem.impulse.cells.view.PlotConfiguration;
import de.toem.impulse.cells.view.ViewConfiguration;
import de.toem.impulse.domain.TimeBase;
import de.toem.impulse.extension.toolkit.other.CreateConfiguration;
import de.toem.impulse.samples.IFloatSamplesWriter;
import de.toem.impulse.samples.IIntegerSamplesWriter;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.serializer.AbstractSingleDomainRecordReader;
import de.toem.impulse.serializer.ParseException;
import de.toem.pattern.bundles.Bundles;
import de.toem.pattern.threading.IProgress;

public class ExampleIntegerReader extends AbstractSingleDomainRecordReader {

    public ExampleIntegerReader() {
        super();
    }

    public ExampleIntegerReader(String id, InputStream in) {
        super(id, in);
    }

    @Override
    protected int isApplicable(String name, String contentType) {
        if (name != null && !name.equals("toem.integer.example"))
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
        Signal int2 = addSignal(signals, "Integer2", "A big integer", ProcessType.Discrete, SignalType.Integer, SignalDescriptor.DEFAULT);
        Signal int3 = addSignal(signals, "Integer3", "XY", ProcessType.Discrete, SignalType.IntegerArray, new SignalDescriptor(SignalDescriptor.CONTENT_DEFAULT,2,ISample.INTEGER_ACCURACY_DEFAULT,ISample.FORMAT_DEFAULT));
        
        // We start at 0 ns
        long t = 0; // 0 ns
        open(t);

        // add array member informations
        getWriter(int3).setMember(0,"X",0, null, ISample.FORMAT_DEFAULT);
        getWriter(int3).setMember(1,"Y",0, null, ISample.FORMAT_DEFAULT);
        
        // get the writers
        IIntegerSamplesWriter integerWriter = (IIntegerSamplesWriter) getWriter(int1);

        // And continue until 1000 ns
        for (; t < 100000; t += 2) {

            // write time as integer
            integerWriter.write(t, false, (int) t - 10000);
            integerWriter.write(t + 1, false, (long) t + 1 - 10000);
            
            ((IIntegerSamplesWriter) getWriter(int3)).write(t, false, new long[]{(long) (Math.sin((t + 100) / 3000.0) * 10.0),(long)(Math.cos((t + 100) / 3000.0) * 10.0)});

        }

        integerWriter = (IIntegerSamplesWriter) getWriter(int2);
        integerWriter.write(10000, false, BigInteger.valueOf(0));
        integerWriter.write(11000, false, BigInteger.valueOf(1));
        integerWriter.write(12000, false, BigInteger.valueOf(-1));
        integerWriter.write(13000, false, BigInteger.valueOf(-255));
        integerWriter.write(14000, false, BigInteger.valueOf(255));
        integerWriter.write(15000, false, BigInteger.valueOf(256));
        integerWriter.write(16000, false, BigInteger.valueOf(-256));
        integerWriter.write(20000, false, BigInteger.valueOf(-5));
        BigInteger integer = new BigInteger("C00000012345678C00000012345678C00000012345678C00000012345678", 16);
        integerWriter.write(30000, false, integer);
        integer = new BigInteger("-123456789012345678901234567890", 10);
        integerWriter.write(40000, false, integer);
        integer = new BigInteger("123456789012345678901234567890", 10);
        integerWriter.write(50000, false, integer);

        // And close finally
        close(t);

        // append a configuration
        ViewConfiguration config = addViewConfiguration("myconfig1", null);
        FolderConfiguration folder = addFolderConfiguration(config, "eventFolder", null);
        PlotConfiguration samples = addPlotConfiguration(folder, int1);
        samples = addPlotConfiguration(folder, int2);
        samples.columnValueFormat = ISample.FORMAT_HEXADECIMAL;

        // a second one
        config = addViewConfiguration("myconfig2", null);
        folder = addFolderConfiguration(config, signals);

        // all from a wallet
        byte[] wallet;
        try {
            wallet = Bundles.getBundleEntryAsByteArray("de.toem.impulse.extension.examples", "integer.walML");
            if (wallet != null)
                config = addViewConfigurations(wallet);
        } catch (IOException e) {
        }

    }

}
