//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.toolkit.ports.highspeed;

import java.io.Closeable;
import java.io.IOException;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.tlk.ITlkPainter;
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

@CellAnnotation(type = ExampleHighspeedAdapter.TYPE, dynamicChildOf = { MultiAdapterPort.TYPE, ImpulsePorts.TYPE })
public class ExampleHighspeedAdapter extends AbstractPortAdapterBaseCell implements IRecordPort, IPortAdapter {
    public static final String TYPE = "port.record.example.highspeed";

    // adapter parameters
    public int signalCount = 4;
    public int signalRate = 50; // in us
    public int burstRate = 100; // in ms
    public int samples = 100000; // no of samples


    @Override
    public int getNature() {
        return IRecordPort.NATURE_CONNECT | IRecordPort.NATURE_FLOATING | IRecordPort.NATURE_REFRESH_REQUEST| IRecordPort.NATURE_CURRENT_VALUE;
    }
    
    /**
     * This class implements both the input (Closable) as well as the reader. IPortProviderFactory adds visual output of current values in the viewer.
     */
    class ExampleInput extends AbstractPortAdapterRecordReader implements Closeable, IPortProviderFactory {

        // parameters of this input
        public int signalCount =ExampleHighspeedAdapter.this.signalCount;
        public int signalRate = ExampleHighspeedAdapter.this.signalRate; 
        public int burstRate = ExampleHighspeedAdapter.this.burstRate; 
        public int samples = ExampleHighspeedAdapter.this.samples; 
        
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
            double getCurrent(int index, long t) {
                switch (index % 8) {
                case 0:
                    return Math.sin(t / 1000.0) * 5.0;
                case 1:
                    return Math.sin(t / 1300.0) * 5.0;
                case 2:
                    return Math.sin(t / 1500.0) * 5.0;
                case 3:
                    return Math.sin(t / 190000.0) * 5.0;
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
            if(progress == null)
                return;
            this.progress = progress;

            // Init the record and signals
            initRecord("Example Highspeed Adapter", TimeBase.us);
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
                        progress.wait(100);
                        changed = CHANGED_VALUE;
                    } catch (InterruptedException e) {
                    }
            }

            // start at t=0
            t = 0;
            final long start = Utils.millies();
            int bursts = 0;
            int count = 0;
            int countPerBurst = (int) (burstRate * 1000L / signalRate);
            open(t, signalRate);

            // operate until canceled
            while (!closed && (progress == null || !progress.isCanceled()) && count < samples) {  
                int toSleep = Math.max((int) (burstRate * (bursts + 1) - (Utils.millies() - start)), 0);
                Actives.sleep(toSleep);
                process(t, countPerBurst);
                t += countPerBurst * signalRate;
                bursts++;
                count += countPerBurst;
                progress.doUpdate();
            }

            // close
            close(t);
            progress = null;
        }

        /**
         * Process at time current
         * 
         * @param current
         * @param count
         */
        synchronized private void process(long current, int count) {
            for (int i = 0; i < count; i++) {
                long t = current + i * signalRate;
                for (int n = 0; n < writers.length; n++){
                    writers[n].write(t, model.getCurrent(n, t));
                }
            }
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
                                    return model != null ? model.getCurrent(index, Utils.millies() * 1000) : null;
                                }

                                @Override
                                public boolean paint(ITlkPainter painter, int x, int y, int width, int height, Object value) {

                                    double v = model.getCurrent(index, Utils.millies() * 1000);
                                    double min = model.getMin(index);
                                    double max = model.getMax(index);
                                    int bar = (int) (Math.abs((v - min) / (max - min)) * width);

                                    // bar
                                    painter.setClipping(x, y, width, height);
                                    painter.fillRectangle(x, y + 1, bar, 3);
                                    painter.fillRectangle(x, y + 2, width, 1);
//                                    painter.setClipping((Rectangle) null);

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
