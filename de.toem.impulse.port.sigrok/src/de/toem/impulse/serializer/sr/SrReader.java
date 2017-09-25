//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.serializer.sr;

import java.io.InputStream;

import de.toem.impulse.ports.sigrok.preferences.Preferences;
import de.toem.impulse.serializer.AbstractConverterRecordReader;
import de.toem.impulse.serializer.vcd.VcdReader;
import de.toem.pattern.element.serializer.ICellReader;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.properties.PropertyModel;

public class SrReader extends AbstractConverterRecordReader {

    public SrReader() {
        super();
    }

    public SrReader(String id, InputStream in) {
        super(id, in);
    }

    static public IPropertyModel getPropertyModel() {
        return new PropertyModel().add("path", "", "Path").add("command", "sigrok-cli -O vcd -i %f ", "Command");
    }
    

    @Override
    protected String getPath() {
        return Preferences.getCliPath();
    }

    @Override
    protected int isApplicable(String name, String contentType) {
        if (name != null && !name.endsWith(".sr"))
            return NOT_APPLICABLE;
        return 0x2;
    }

    @Override
    protected int isApplicable(byte[] buffer) {
        return buffer[0] == 0x50 && buffer[1] == 0x4b ? APPLICABLE : NOT_APPLICABLE;

    }

    protected ICellReader createReader(String id, InputStream in) {
        return new VcdReader(id, in);
    }

    protected ICellReader createReader() {
        return new VcdReader();
    }
}
