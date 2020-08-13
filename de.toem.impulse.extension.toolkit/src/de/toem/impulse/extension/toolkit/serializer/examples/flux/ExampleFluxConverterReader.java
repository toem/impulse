package de.toem.impulse.extension.toolkit.serializer.examples.flux;

import java.io.InputStream;

import de.toem.impulse.extension.toolkit.ImpulseToolkitExtension;
import de.toem.impulse.serializer.AbstractFluxConverterRecordReader;

public class ExampleFluxConverterReader extends AbstractFluxConverterRecordReader{

    public ExampleFluxConverterReader() {
        super();
    }

    public ExampleFluxConverterReader(String id, InputStream in) {
        super(id, in);
    }
    
    @Override
    protected int isApplicable(String name, String contentType) {
        if (name != null && !name.equals("toem.flux.example"))
            return NOT_APPLICABLE;
        return 8; 
    }

    @Override
    protected int isApplicable(byte[] buffer) {
        if (buffer[0] == '#' && buffer[1] == 'E' && buffer[2] == 'X' && buffer[3] == 'A')
            return APPLICABLE;
        return NOT_APPLICABLE;
    }

    @Override
    protected String getFluxName() {
        return "example";
    }

    @Override
    protected String getFluxLocation() {
        return "flux/example20/";
    }

    @Override
    protected String getPluginId() {
        return ImpulseToolkitExtension.PLUGIN_ID;
    }

    @Override
    protected String getSharedLocation() {
        return null;
    }

}
