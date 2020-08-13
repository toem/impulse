//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.toolkit.serializer.examples;

import java.io.InputStream;

import de.toem.impulse.cells.record.Scope;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.domain.TimeBase;
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

public class ExampleStructReader extends AbstractSingleDomainRecordReader {

    public ExampleStructReader() {
        super();
    }

    public ExampleStructReader(String id, InputStream in) {
        super(id, in);
    }

    @Override
    protected int isApplicable(String name, String contentType) {
        if (name != null && !name.equals("toem.struct.example"))
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
        Signal log1 = addSignal(signals, "Log", "A log signal", ProcessType.Discrete, 
                SignalType.Struct, SignalDescriptor.DEFAULT);
        Signal transaction1 = addSignal(signals, "Transaction", "A transaction signal", 
                ProcessType.Discrete, SignalType.Struct, new SignalDescriptor(
                SignalDescriptor.STRUCT_CONTENT_TRANSACTION,  ISample.FORMAT_DEFAULT));

        // We start at 0 ns
        long t = 0; // 0 ns
        open(t);

        // log
        StructMember members[] = new StructMember[2];
        members[0] = new StructMember("Message", StructMember.STRUCT_TYPE_TEXT, 
                null, ISamples.FORMAT_DEFAULT);
        members[1] = new StructMember("Data", StructMember.STRUCT_TYPE_INTEGER, 
                null, ISamples.FORMAT_HEXADECIMAL);

        IStructSamplesWriter logWriter = (IStructSamplesWriter) getWriter(log1);

        members[0].setValue("Start");
        members[1].setValue(100);
        logWriter.write(100, false, members);

        members[0].setValue("Continue");
        members[1].setValue(500);
        logWriter.write(500, false, members);

        // transaction
        StructMember members0[] = new StructMember[2];
        members0[0] = new StructMember("Message", StructMember.STRUCT_TYPE_TEXT, 
                null, ISamples.FORMAT_DEFAULT);
        members0[1] = new StructMember("Data", StructMember.STRUCT_TYPE_INTEGER, 
                null, ISamples.FORMAT_HEXADECIMAL);
        StructMember members1[] = new StructMember[2];
        members1[0] = new StructMember("Message", StructMember.STRUCT_TYPE_TEXT, 
                null, ISamples.FORMAT_DEFAULT);
        members1[1] = new StructMember("Address", StructMember.STRUCT_TYPE_INTEGER, 
                null, ISamples.FORMAT_HEXADECIMAL);

        IStructSamplesWriter transWriter = (IStructSamplesWriter) getWriter(transaction1);

        members0[0].setValue("Do 1");
        members0[1].setValue(0);
        transWriter.write(100, false, /* id */0,/* order */IStructSamplesWriter.GO_FIRST,
                /* layer */0, members0);

        members1[0].setValue("Do 2");
        members1[1].setValue(0);
        transWriter.write(150, false, 1, IStructSamplesWriter.GO_FIRST, 1, members1);

        members0[1].setValue(90);
        transWriter.write(300, false, 0, IStructSamplesWriter.GO_LAST, 0, members0);

        members1[1].setValue(90);
        transWriter.write(350, false, 1, IStructSamplesWriter.GO_LAST, 1, members1);

        members0[0].setValue("Do 3");
        members0[1].setValue(0);
        transWriter.write(350, false, 2, IStructSamplesWriter.GO_FIRST, 0, members0);
        members0[1].setValue(90);
        transWriter.write(600, false, 2, IStructSamplesWriter.GO_LAST, 0, members0);

        // And close finally
        close(1000);
    }
}
