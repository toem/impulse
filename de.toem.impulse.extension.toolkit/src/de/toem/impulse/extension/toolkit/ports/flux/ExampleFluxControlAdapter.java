//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.toolkit.ports.flux;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import de.toem.eclipse.toolkits.util.EclipseUtils;
import de.toem.impulse.cells.ports.AbstractPortAdapterBaseCell;
import de.toem.impulse.cells.ports.IPortAdapter;
import de.toem.impulse.cells.ports.IPortProgress;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.MultiAdapterPort;
import de.toem.impulse.cells.preferences.ImpulsePorts;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.extension.toolkit.ImpulseToolkitExtension;
import de.toem.impulse.flux.AbstractFluxHandler;
import de.toem.impulse.flux.FluxParser;
import de.toem.impulse.flux.FluxParser.Trace;
import de.toem.impulse.flux.IFluxHandler;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.serializer.BinaryParseBuffer;
import de.toem.impulse.serializer.IRecordReader;
import de.toem.impulse.serializer.base.FluxReader;
import de.toem.impulse.values.StructMember;
import de.toem.pattern.bundles.Bundles;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.element.ICell;
import de.toem.pattern.threading.Actives;
import de.toem.pattern.threading.IExecutable;
import de.toem.pattern.threading.IProgress;

@CellAnnotation(type = ExampleFluxControlAdapter.TYPE, dynamicChildOf = { MultiAdapterPort.TYPE, ImpulsePorts.TYPE })
public class ExampleFluxControlAdapter extends AbstractPortAdapterBaseCell implements IRecordPort, IPortAdapter {
    public static final String TYPE = "port.record.example.flux.control";

    // parameters

    @Override
    public int getNature() {
        return IRecordPort.NATURE_CONNECT | IRecordPort.NATURE_FLOATING | IRecordPort.NATURE_REFRESH_CONTINUOUS | IRecordPort.NATURE_CURRENT_VALUE;
    }

    /**
     * This class implements both the input (Closable) as well as the reader. IPortProviderFactory adds visual output of current values in the viewer.
     */
    class FluxInput implements Closeable {

        IProgress progress;
        Process p;
        InputStream in;
        OutputStream out;
        IFluxHandler handler;

        FluxInput(final IProgress progress) {
			// flux location
			String loc = "";
			URL fluxLocation = null;
			try {
				loc = "flux/example21/" + EclipseUtils.getExecutableSubEntryPath("example");
				fluxLocation = Bundles.getBundleEntryAsFileUrl(ImpulseToolkitExtension.PLUGIN_ID, loc);
			} catch (final Throwable e) {
			}
			if (fluxLocation == null)
				return;

			// command
			final String[] command = new String[1];
			command[0] = fluxLocation.getPath();

			// process
			this.progress = progress;
			try {

				ProcessBuilder builder = new ProcessBuilder(command);
	            p = builder.start();
				in = p.getInputStream();
				out = p.getOutputStream();
			} catch (Throwable e) {
				e.printStackTrace();
			}
            handler = new AbstractFluxHandler() {

                @Override
                public boolean requiresData() {
                    return false;
                }

                @Override
                public boolean handleControl(IProgress p,Trace trace, boolean request, int controlId, int messageId, StructMember[] members,BinaryParseBuffer b) {
                    if (!request) {
                        if (controlId == 1 && messageId == 1) {
                            members = new StructMember[] { new StructMember(0, ISample.STRUCT_TYPE_INTEGER, 1000),
                                    new StructMember(1, ISample.STRUCT_TYPE_TEXT, "ns") };
                            FluxParser.writeControlEntry(out, true, 2, 1, members);
                        }
                        else if (controlId == 2 && messageId == 1) {
                            byte[] value = { 12, 32 };
                            members = new StructMember[] { new StructMember(0, ISample.STRUCT_TYPE_ENUM, 1), new StructMember(1, ISample.STRUCT_TYPE_ENUM, 2),
                                    new StructMember(2, ISample.STRUCT_TYPE_INTEGER, 2000), new StructMember(3, ISample.STRUCT_TYPE_BINARY, value) };
                            FluxParser.writeControlEntry(out, true, 3, 1, members);

                        }
                        else if (controlId == 3 && messageId == 1) {
                            members = new StructMember[] { new StructMember(0, ISample.STRUCT_TYPE_INTEGER, 5000) };
                            FluxParser.writeControlEntry(out, true, 4, 1, members);

                        }
                        else if (controlId == 4 && messageId == 1) {

                        }
                    }
                    return true;
                }
             };

            Actives.run(new IExecutable() {

                @Override
                public void execute(IProgress p) {
                    if (progress instanceof IPortProgress)
                        while (!((IPortProgress) progress).isStreaming() && !progress.isCanceled())
                            Actives.sleep(100);

                    // start
                    Actives.sleep(1000);
                    StructMember[] members = new StructMember[] { new StructMember(0, ISample.STRUCT_TYPE_ENUM, 1),
                            new StructMember(1, ISample.STRUCT_TYPE_TEXT, "signalName"), new StructMember(2, ISample.STRUCT_TYPE_ENUM, 2) };
                    FluxParser.writeControlEntry(out, true, 1, 1, members);

                }
            });
        }

        @Override
        public void close() throws IOException {
            try {
                in.close();
                out.close();
                p.destroy();
            } catch (Throwable e) {

            }
        }

        public boolean isOpen() {
            return in != null;
        }
    }

    @Override
    public boolean validate(ICell insertPoint) {
        return true;
    }

    @Override
    public Closeable getInput(IProgress iProgress) {
        return new FluxInput(iProgress);
    }

    @Override
    public IRecordReader newReader(Closeable input) {
        if (input instanceof FluxInput && ((FluxInput) input).isOpen())
            return new FluxReader(null, ((FluxInput) input).in, ((FluxInput) input).handler);
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
