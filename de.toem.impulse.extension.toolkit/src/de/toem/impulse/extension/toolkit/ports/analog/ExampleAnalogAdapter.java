//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.toolkit.ports.analog;

import java.io.Closeable;
import java.io.IOException;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.tlk.ITlkPainter;
import de.toem.eclipse.toolkits.tlk.TLK;
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

@CellAnnotation(type = ExampleAnalogAdapter.TYPE, dynamicChildOf = { MultiAdapterPort.TYPE, ImpulsePorts.TYPE })
public class ExampleAnalogAdapter extends AbstractPortAdapterBaseCell implements IRecordPort, IPortAdapter {
    public static final String TYPE = "port.record.example";

    // parameters
    public int signalCount = 8;

    @Override
    public int getNature() {
        return IRecordPort.NATURE_CONNECT | IRecordPort.NATURE_FLOATING | IRecordPort.NATURE_REFRESH_CONTINUOUS | IRecordPort.NATURE_CURRENT_VALUE;
    }

    /**
     * This class implements both the input (Closable) as well as the reader. IPortProviderFactory adds visual output of current values in the viewer.
     */
    class ExampleInput extends AbstractPortAdapterRecordReader implements Closeable, IPortProviderFactory {

        // parameters of this input
        public int signalCount = ExampleAnalogAdapter.this.signalCount;

        // status
        long t;
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
            double getCurrent(int index) {
                long t = Utils.millies();
                switch (index % 8) {
                case 0:
                    return Math.sin(t / 1000.0) * 5.0;
                case 1:
                    return Math.sin(t / 1300.0) * 5.0;
                case 2:
                    return Math.sin(t / 1500.0) * 5.0;
                case 3:
                    return Math.sin(t / 1900.0) * 5.0;
                case 4:
                    return Math.sin(t / 800.0) * 5.0;
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
            initRecord("Example Adapter Record", TimeBase.ms);
            for (int n = 0; n < writers.length; n++) {
                signals[n] = addSignal(base, "in" + n, "Example Signal " + n, ProcessType.Discrete, SignalType.Float, SignalDescriptor.Float64);
                writers[n] = (IFloatSamplesWriter) getWriter(signals[n]);
            }
            changed = CHANGED_RECORD;
            // wait after header parsing
            synchronized (progress) {
                while (!progress.isStreaming() && !progress.isCanceled())
                    try {
                        progress.wait(100);
                        changed = CHANGED_VALUE;
//                        Utils.log("CHANGED_CURRENT");
                    } catch (InterruptedException e) {
                    }
            }

            // start at t=0
            t = 0;
            final long start = Utils.millies();
            open(t, 0, 1, 0);
            // open(t);

            // operate until canceled
            while (!closed && (progress == null || !progress.isCanceled())) {
                process(t);
                Actives.sleep(100);
                t = Utils.millies() - start;
            }

            // close
            close(t);
            progress = null;
        }

        /**
         * Process at time current
         * 
         * @param current
         */
        synchronized private void process(long current) {
            for (int n = 0; n < writers.length; n++)
                writers[n].write(current, model.getCurrent(n));
            changed = CHANGED_SIGNALS > changed ? CHANGED_SIGNALS : changed;
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
        synchronized public void close() throws IOException {
            closed = true;
        }

        @Override
        public Object getProvider(Class clazz, Object subject) {
            if (clazz.equals(IActiveValueProvider.class))
                for (int n = 0; n < signals.length; n++)
                    if (signals[n] == subject) {
                        if (painters[n] == null) {
                            final int index = n;
                            painters[n] = new IActiveValueProvider() {

                                @Override
                                public boolean isActive() {
                                    // Utils.log(progress.doUpdating());
                                    return !closed && progress != null && (!progress.isStreaming() || progress.isUpdating());
                                }

                                @Override
                                public Object getValue() {
                                    return model != null ? model.getCurrent(index) : null;
                                }

                                @Override
                                public boolean paint(ITlkPainter painter, int x, int y, int width, int height, Object value) {

                                    double v = model.getCurrent(index);
                                    double min = model.getMin(index);
                                    double max = model.getMax(index);
                                    int bar = (int) (Math.abs((v - min) / (max - min)) * width);

                                    // bar
                                    painter.drawLine(x, y + 1, x + bar, y + 1);
                                    painter.drawLine(x, y + 3, x + bar, y + 3);
                                    painter.drawLine(x, y + 2, x + width, y + 1);

                                    // value
                                    return false; // let impulse paint the value

                                }
                            };
                        }
                        return painters[n];
                    }
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
