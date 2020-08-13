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

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.util.EclipseUtils;
import de.toem.impulse.cells.ports.AbstractPortAdapterBaseCell;
import de.toem.impulse.cells.ports.IPortAdapter;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.MultiAdapterPort;
import de.toem.impulse.cells.preferences.ImpulsePorts;
import de.toem.impulse.extension.toolkit.ImpulseToolkitExtension;
import de.toem.impulse.serializer.IRecordReader;
import de.toem.impulse.serializer.ParseException;
import de.toem.impulse.serializer.base.FluxReader;
import de.toem.pattern.bundles.Bundles;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.element.ICell;
import de.toem.pattern.threading.IProgress;

@CellAnnotation(type = ExampleFluxAdapter.TYPE, dynamicChildOf = { MultiAdapterPort.TYPE, ImpulsePorts.TYPE })
public class ExampleFluxAdapter extends AbstractPortAdapterBaseCell implements IRecordPort, IPortAdapter {
	public static final String TYPE = "port.record.example.flux";

	// parameters

	@Override
	public int getNature() {
		return IRecordPort.NATURE_CONNECT | IRecordPort.NATURE_FLOATING | IRecordPort.NATURE_REFRESH_CONTINUOUS | IRecordPort.NATURE_CURRENT_VALUE;
	}

	/**
	 * This class implements both the input (Closable) as well as the reader.
	 * IPortProviderFactory adds visual output of current values in the viewer.
	 */
	class FluxInput implements Closeable {

		IProgress progress;
		Process p;
		InputStream in;
		OutputStream out;

		FluxInput(final IProgress progress) {

			// flux location
			String loc = "";
			URL fluxLocation = null;
			try {
				loc = "flux/example20/" + EclipseUtils.getExecutableSubEntryPath("example");
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
		if (input instanceof FluxInput)
			return new FluxReader(null, ((FluxInput) input).in);
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
