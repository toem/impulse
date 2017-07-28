//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.ports.examples.scope;

import java.io.Closeable;
import java.io.IOException;

import de.toem.basics.core.Utils;
import de.toem.impulse.cells.ports.AbstractPortAdapterBaseCell;
import de.toem.impulse.cells.ports.IPortAdapter;
import de.toem.impulse.cells.ports.IPortProgress;
import de.toem.impulse.cells.ports.IPortProviderFactory;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.MultiAdapterPort;
import de.toem.impulse.cells.preferences.ImpulsePorts;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.domain.TimeBase;
import de.toem.impulse.paint.IActiveValueProvider;
import de.toem.impulse.samples.IFloatSamplesWriter;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.serializer.AbstractPortAdapterRecordReader;
import de.toem.impulse.serializer.IRecordReader;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.ICover;
import de.toem.pattern.threading.Actives;
import de.toem.pattern.threading.IProgress;

@CellAnnotation(type = ExampleScopeAdapter.TYPE, dynamicChildOf = { MultiAdapterPort.TYPE, ImpulsePorts.TYPE })
public class ExampleScopeAdapter extends AbstractPortAdapterBaseCell implements IRecordPort, IPortAdapter {
    public static final String TYPE = "port.record.example.scope";

    // parameters
    public int signalCount = 4;
    public int signalRate = 50; // in us
    public int samples = 1000; // no of samples

    @Override
    public int getNature() {
        return IRecordPort.NATURE_REFRESH_REQUEST | IRecordPort.NATURE_ITERATE_ONCE | IRecordPort.NATURE_ITERATE_MULTIPLE;
    }

    /**
     * This class implements both the input (Closable) as well as the reader. IPortProviderFactory adds visual output of current values in the viewer.
     */
    class ExampleInput extends AbstractPortAdapterRecordReader implements Closeable, IPortProviderFactory {

        // parameters of this input
        public int signalCount = ExampleScopeAdapter.this.signalCount;
        public int signalRate = ExampleScopeAdapter.this.signalRate;
        public int samples = ExampleScopeAdapter.this.samples;

        // status
        long current;
        boolean closed;
        int changed;
        final int NUM = signalCount >= 0 ? signalCount : 8;

        // signals and writers
        Signal[] signals = new Signal[NUM];
        IFloatSamplesWriter[] writers = new IFloatSamplesWriter[NUM];
        IActiveValueProvider[] painters = new IActiveValueProvider[NUM];
        IPortProgress progress;

        // the value model
        class Model {
            double getCurrent(int index, long t) {
                switch (index % 8) {
                case 0:
                    return Math.sin(t / 1000.0) * 5.0;
                case 1:
                    return Math.sin(t / 1300.0) * 5.0;
                case 2:
                    return Math.sin(t / 1500.0) * 5.0;
                case 3:
                    return Math.sin(t / 1900000.0) * 5.0;
                case 4:
                    return Math.sin(t / 80000.0) * 5.0;
                case 5:
                    return Math.sin(t / 500.0) * 5.0;
                case 6:
                    return Math.sin(t / 300.0) * 5.0;
                case 7:
                    return Math.sin(t / 100.0) * 5.0;
                }
                return 0.0;
            }

            double getMin(int index) {
                return -5.0;
            }

            double getMax(int index) {
                return 5.0;
            }
        }

        Model model = new Model();

        @Override
        protected void process(IPortProgress progress) {

            // progress
            if (progress == null)
                return;
            this.progress = progress;

            // Init the record and signals
            initRecord("Example Scope Adapter", TimeBase.us);
            for (int n = 0; n < writers.length; n++) {
                signals[n] = addSignal(base, "in" + n, "Example Signal " + n, ProcessType.Continuous, SignalType.Float, SignalDescriptor.Float32);
                writers[n] = (IFloatSamplesWriter) getWriter(signals[n]);
            }
            changed = CHANGED_RECORD;
            progress.doUpdate();

            // wait after header parsing
            synchronized (progress) {
                while (!progress.isStreaming() && !progress.isCanceled())
                    try {
                        progress.wait(250);
                        changed = CHANGED_CURRENT;
                    } catch (InterruptedException e) {
                    }
            }

            // start at t=0
            current = 0;
            final long start = Utils.millies() * 1000;
            int bursts = 0;

            // operate until canceled
            while (!closed && (progress == null || !progress.isCanceled())) {
                Actives.sleep(/* (int) ((signalRate * samples) * (bursts + 1) - (Utils.millies() - start)) */100);
                process(current + start);
                current += samples * signalRate;
                bursts++;
                progress.doUpdate();
                if (!progress.isIterating())
                    break;
            }

            // close
            close(current);
            progress = null;
        }

        /**
         * Process at time current
         * 
         * @param current
         * @param count
         */
        synchronized private void process(long current) {
            long t = 0;

            for (int n = 0; n < writers.length; n++)
                writers[n].open(t, signalRate, 0, 0, null);

            for (int i = 0; i < samples; i++) {
                t = i * signalRate;
                for (int n = 0; n < writers.length; n++)
                    writers[n].write(t, model.getCurrent(n, t + current));
            }
            for (int n = 0; n < writers.length; n++)
                writers[n].close(t);
            changed = CHANGED_SIGNALS > changed ? CHANGED_SIGNALS : changed;
        }

        @Override
        public boolean supportsStreaming() {
            return true;
        }

        @Override
        synchronized public ICover flush() {
            changed = CHANGED_NONE;
            return super.doFlush(current);
        }

        @Override
        public int hasChanged() {
            return changed;
        }

        @Override
        synchronized public void close() throws IOException {
            closed = true;
        }

        @Override
        public Object getProvider(Class clazz, Object subject) {
            return null;
        }
    }

    @Override
    public boolean validate(ICell insertPoint) {
        return true;
    }

    @Override
    public Closeable getInput(IProgress iProgress) {
        return new ExampleInput();
    }

    @Override
    public IRecordReader newReader(Closeable input) {
        if (input instanceof ExampleInput)
            return (ExampleInput) input;
        return null;
    }

    @Override
    public Object getProvider(Class clazz, Object subject) {
        return null;
    }

    @Override
    public boolean isPort() {
        return getParent() instanceof ImpulsePorts;
    }
}
