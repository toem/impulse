/*******************************************************************************
 * Copyright (c) 2012-2014  Thomas Haber 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package de.toem.impulse.extension.toolkit.serializer.examples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.cells.record.Scope;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.cells.serializer.SerializerConfiguration;
import de.toem.impulse.domain.TimeBase;
import de.toem.impulse.extension.toolkit.ImpulseToolkitExtension;
import de.toem.impulse.samples.IBinarySamplesWriter;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.serializer.AbstractSingleDomainRecordReader;
import de.toem.impulse.serializer.ParseException;
import de.toem.pattern.bundles.Bundles;
import de.toem.pattern.element.ICell;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.properties.PropertyModel;
import de.toem.pattern.threading.IProgress;

public class ExampleBinaryReader extends AbstractSingleDomainRecordReader {

    static public IPropertyModel getPropertyModel() {
        return new PropertyModel().add("parma1", "4", "Param1", null, null).add("parma2", "true", "Param2", null, null);
    }
    static public IPropertyModel getPropertyModel(Class<? extends ICell> cs) {
        if (SerializerConfiguration.class.equals(cs))
            return new PropertyModel().add("conf_param3", "yep", "Param3", null, null);
        return null;
    }
    
    public ExampleBinaryReader() {
        super();
    }

    public ExampleBinaryReader(String id, InputStream in) {
        super(id, in);
    }

    @Override
    protected int isApplicable(String name, String contentType) {
        if (name != null && !name.equals("toem.binary.example"))
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
        initRecord("Example Record", TimeBase.ms);

        if (parameters != null){
            parameters.get("parma1");
        }
        if (configurationParameters != null){
            configurationParameters.get("parma3");
        }
        
        // We forget the input and create a record with scopes and some signals
        Scope signals = addScope(null, "Signals");
        Signal image1 = addSignal(signals, "Image", "An image signal", ProcessType.Discrete, SignalType.Binary, new SignalDescriptor(
                SignalDescriptor.BINARY_CONTENT_IMAGE, -1));

        // We start at 0 ns
        long t = 0; // 0 ns
        open(t);

        // images
        IBinarySamplesWriter imageWriter = (IBinarySamplesWriter) getWriter(image1);
        try {
            ImageData[] imageData = new ImageLoader().load(Bundles.getBundleEntryAsStream(ImpulseToolkitExtension.PLUGIN_ID, "input2.gif"));
            if (imageData != null)
                for (ImageData data : imageData) {
                    ImageLoader loader = new ImageLoader();
                    loader.data = new ImageData[] { data };
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    loader.save(out, TLK.IMAGE_PNG);
                    out.close();
                    imageWriter.write(t, false, out.toByteArray());
                    t += 20;
                }

        } catch (IOException e) {
        }

        // And close finally
        close(t);
    }
}
