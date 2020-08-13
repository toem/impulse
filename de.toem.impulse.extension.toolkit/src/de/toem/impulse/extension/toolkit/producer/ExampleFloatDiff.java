package de.toem.impulse.extension.toolkit.producer;

import java.util.ArrayList;
import java.util.List;

import de.toem.basics.core.Utils;
import de.toem.impulse.domain.IDomainBase;
import de.toem.impulse.samples.IFloatSamplesWriter;
import de.toem.impulse.samples.IReadableSamples;
import de.toem.impulse.samples.IReaderDomainBaseProvider;
import de.toem.impulse.samples.ISamplePointer;
import de.toem.impulse.samples.base.PackedSamples;
import de.toem.impulse.samples.iterator.SamplePointer;
import de.toem.impulse.samples.iterator.SamplesIterator;
import de.toem.impulse.samples.producer.AbstractSamplesProducer;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.properties.PropertyModel;
import de.toem.pattern.threading.IProgress;

public class ExampleFloatDiff extends AbstractSamplesProducer {

    public ExampleFloatDiff() {
    };

    public ExampleFloatDiff(List<IReadableSamples> sources, ProcessType processType, SignalType signalType, SignalDescriptor signalDescriptor,
            IDomainBase domainBase, String start, String end, String rate, String definition, IPropertyModel parameters,IReaderDomainBaseProvider readerBaseProvider) {
        super(sources, processType, signalType, signalDescriptor, domainBase, start, end, rate, definition, parameters,readerBaseProvider);

    }

    static public IPropertyModel getPropertyModel() {
        return new PropertyModel().add("multiplier", "1.0", "Multiplier", null, null);
    }

    @Override
    public void produce(IProgress p,List<IReadableSamples> sources) {

        // get inputs
        List<ISamplePointer> inputs = new ArrayList<ISamplePointer>();
        for (IReadableSamples source : sources)
            if (source != null)
                inputs.add(new SamplePointer(source));

        // parameters
        double multiplier = Utils.parseDouble(parameters.get("multiplier"),1.0);

        // check
        if (inputs.size() == 2 && signalType == SignalType.Float && processType == ProcessType.Discrete) {

            // prepare in's and out
            ISamplePointer in0 = inputs.get(0);
            ISamplePointer in1 = inputs.get(1);
            IFloatSamplesWriter out = (IFloatSamplesWriter) PackedSamples.createWriter(processType, signalType, signalDescriptor, productionBase);
            SamplesIterator iter = new SamplesIterator(inputs);

            // open
            out.open(iter.getStart());

            // iterate
            for (; iter.hasNext();) {
                long current = iter.next();

                out.write(current, false, (in0.doubleValue() - in1.doubleValue())*multiplier);
            }

            // close
            out.close(iter.getEnd());

            setReference(PackedSamples.createReader(out, readerBase));
        }
    }

    @Override
    public boolean isVolatile() {
        return false;
    }
    
}
