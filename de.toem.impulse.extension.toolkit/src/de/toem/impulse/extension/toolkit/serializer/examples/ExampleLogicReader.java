//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.toolkit.serializer.examples;

import java.io.InputStream;

import de.toem.impulse.cells.record.Scope;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.domain.TimeBase;
import de.toem.impulse.samples.ILogicSamplesWriter;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.serializer.AbstractSingleDomainRecordReader;
import de.toem.impulse.serializer.ParseException;
import de.toem.impulse.values.Logic;
import de.toem.pattern.threading.IProgress;

public class ExampleLogicReader extends AbstractSingleDomainRecordReader {

    final static int STATE_LEVEL_NONE = (byte) ISample.STATE_LEVEL_NONE;
    final static int STATE_LEVEL_2 = (byte) ISample.STATE_LEVEL_2;
    final static int STATE_LEVEL_4 = (byte) ISample.STATE_LEVEL_4;
    final static int STATE_LEVEL_16 = (byte) ISample.STATE_LEVEL_16;

    final static byte STATE_0_BITS = (byte) ISample.STATE_0_BITS;
    final static byte STATE_1_BITS = (byte) ISample.STATE_1_BITS;
    final static byte STATE_Z_BITS = (byte) ISample.STATE_Z_BITS;
    final static byte STATE_X_BITS = (byte) ISample.STATE_X_BITS;
    final static byte STATE_L_BITS = (byte) ISample.STATE_L_BITS;
    final static byte STATE_H_BITS = (byte) ISample.STATE_H_BITS;
    final static byte STATE_U_BITS = (byte) ISample.STATE_U_BITS;
    final static byte STATE_W_BITS = (byte) ISample.STATE_W_BITS;
    final static byte STATE_D_BITS = (byte) ISample.STATE_D_BITS;

    public ExampleLogicReader() {
        super();
    }

    public ExampleLogicReader(String id, InputStream in) {
        super(id, in);
    }

    @Override
    protected int isApplicable(String name, String contentType) {
        if (name != null && !name.equals("toem.logic.example"))
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

        // We forget the input and create a record with scopes and 3 signals
        Scope signals = addScope(null, "Signals");
        Signal logic1 = addSignal(signals, "Logic1", "A logic signal", ProcessType.Discrete, SignalType.Logic, SignalDescriptor.LogicWidth(1));
        Signal logic2 = addSignal(signals, "Logic2", "An 8bit  logic signal", ProcessType.Discrete, SignalType.Logic, SignalDescriptor.LogicWidth(8));
        Signal logic3 = addSignal(signals, "Logic3", "An 2048 bit logic signal", ProcessType.Discrete, SignalType.Logic, SignalDescriptor.LogicWidth(2048));
        Signal logic4 = addSignal(signals, "Logic4", "An 64 bit logic signal", ProcessType.Discrete, SignalType.Logic, SignalDescriptor.LogicWidth(64));

        // We start at 0 ns
        long t = 0; // 0 ns
        open(t);

        // get the writers
        ILogicSamplesWriter logicWriter = (ILogicSamplesWriter) getWriter(logic1);

        // And continue until 1000 ns
        for (; t < 100000; t += 10) {

            // write a rectangle
            logicWriter.write(t, false, STATE_LEVEL_2, t % 20 == 0 ? STATE_0_BITS : STATE_1_BITS);
        }

        logicWriter = (ILogicSamplesWriter) getWriter(logic2);
        logicWriter.write(0, false, STATE_LEVEL_16, STATE_U_BITS);
        logicWriter.write(10000, false, STATE_LEVEL_2, STATE_0_BITS);
        logicWriter.write(20000, false, STATE_LEVEL_2, STATE_0_BITS, new byte[] { STATE_1_BITS, STATE_1_BITS, STATE_0_BITS, STATE_1_BITS }, 0, 4);
        logicWriter.write(30000, false, STATE_0_BITS, "11XX");

        logicWriter = (ILogicSamplesWriter) getWriter(logic3);
        logicWriter.write(20000, false, STATE_LEVEL_2, STATE_1_BITS);
        logicWriter.write(50000, false, STATE_LEVEL_2, STATE_0_BITS);
        logicWriter.write(70000, false, STATE_U_BITS, "111100001111000011110000");
        logicWriter.write(80000, false, STATE_U_BITS, "000011110000111100001111");

        logicWriter = (ILogicSamplesWriter) getWriter(logic4);
        logicWriter.write(0, Logic.valueOf("11XXZZUU00"));
        logicWriter.write(20000, Logic.valueOf("uuXXZZUU00"));
        logicWriter.write(40000, Logic.valueOf("xxuuXXZZUU00"));
        logicWriter.write(60000, Logic.valueOf(0xff00));
        logicWriter.write(80000, Logic.valueOf(0xffffaa23L));

        // And close finally
        close(t);
    }
}
