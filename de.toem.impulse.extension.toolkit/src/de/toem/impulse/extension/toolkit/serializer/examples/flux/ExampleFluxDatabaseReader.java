package de.toem.impulse.extension.toolkit.serializer.examples.flux;

import java.io.InputStream;

import de.toem.impulse.extension.toolkit.ImpulseToolkitExtension;
import de.toem.impulse.serializer.AbstractFluxDatabaseRecordReader;

public class ExampleFluxDatabaseReader extends AbstractFluxDatabaseRecordReader{

    public ExampleFluxDatabaseReader() {
        super();
    }

    public ExampleFluxDatabaseReader(String id, InputStream in) {
        super(id, in);
    }
    
    @Override
    protected int isApplicable(String name, String contentType) {
        if (name != null && !name.equals("toem.fluxdb.example"))
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
        return "flux/example22/";
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
