//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.ports.sigrok;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.console.MessageConsoleStream;

import de.toem.basics.core.Utils;
import de.toem.impulse.ImpulseBase;
import de.toem.impulse.cells.ports.AbstractPortAdapterBaseCell;
import de.toem.impulse.cells.ports.AbstractSyncablePortAdapterCell;
import de.toem.impulse.cells.ports.IPortAdapter;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.MultiAdapterPort;
import de.toem.impulse.cells.preferences.ImpulsePorts;
import de.toem.impulse.ports.sigrok.preferences.Preferences;
import de.toem.impulse.serializer.IRecordReader;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.element.Elements;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.serializer.ICellReader;
import de.toem.pattern.element.serializer.SerializerDescriptor;
import de.toem.pattern.threading.Actives;
import de.toem.pattern.threading.IExecutable;
import de.toem.pattern.threading.IProgress;

@CellAnnotation(type = SigrokAdapter.TYPE, dynamicChildOf = { MultiAdapterPort.TYPE, ImpulsePorts.TYPE })
public class SigrokAdapter extends AbstractSyncablePortAdapterCell implements IRecordPort, IPortAdapter {
	public static final String TYPE = "port.record.sigrok";

	public String driver;
	public String config;
	public String channels;
	public String channelGroup;
	public String trigger;
	public boolean waitTrigger;
	public String protocolDecoders;
	public String protocolStack;
	public String protocolAnnotations;
	public String protocolMeta;
	public String protocolBinary;
	public String additionalOptions;
	public static final int AQUISITION_SAMPLES = 0;
	public static final int AQUISITION_TIME = 1;
	public static final int AQUISITION_FRAMES = 2;
	public static final int AQISITION_CONTINUOUS = 3;
	public int aquisitionMode;
	public String aquisitionAmount = "1000";

	@Override
	public boolean validate(ICell insertPoint) {
		return true;
	}

	public SigrokAdapter() {
		super();
		driver = "demo";
		config = "samplerate=1MHz";

	}

	public String[] prepareCommand(String mode) {
		List<String> command = new ArrayList<String>();
		command.add(Preferences.getCliCommand());

		if (!Utils.isEmpty(driver)) {
			command.add("-d");
			command.add(driver);
		}
		if (!"get".equals(mode)) {
			if (!Utils.isEmpty(config)) {
				command.add("-c");
				command.add(config);
			}
			if (!Utils.isEmpty(channels)) {
				command.add("-C");
				command.add(channels);
			}
			if (!Utils.isEmpty(channelGroup)) {
				command.add("-g");
				command.add(channelGroup);
			}
			if (!Utils.isEmpty(trigger)) {
				command.add("-t");
				command.add(trigger);
			}
			if (waitTrigger) {
				command.add("-w");
			}
			if (!Utils.isEmpty(protocolDecoders)) {
				command.add("-P");
				command.add(protocolDecoders);
			}
			if (!Utils.isEmpty(protocolStack)) {
				command.add("-S");
				command.add(protocolStack);
			}
			if (!Utils.isEmpty(protocolAnnotations)) {
				command.add("-A");
				command.add(protocolAnnotations);
			}
			if (!Utils.isEmpty(protocolMeta)) {
				command.add("-M");
				command.add(protocolMeta);
			}
			if (!Utils.isEmpty(protocolBinary)) {
				command.add("-B");
				command.add(protocolBinary);
			}
			if (!Utils.isEmpty(additionalOptions)) {
				command.add(additionalOptions);
			}
		}
		if (!"get".equals(mode) && !"set".equals(mode)) {
			if (aquisitionMode == AQUISITION_TIME) {
				command.add("--time");
				command.add(aquisitionAmount);
			} else if (aquisitionMode == AQUISITION_SAMPLES) {
				command.add("--samples");
				command.add(aquisitionAmount);
			} else if (aquisitionMode == AQUISITION_FRAMES) {
				command.add("--frames");
				command.add(aquisitionAmount);
			} else if (aquisitionMode == AQISITION_CONTINUOUS) {
				command.add("--continuous");
			}
		}
		if ("set".equals(mode)) {
			command.add("--set");
		} else if ("get".equals(mode)) {
			command.add("--get");
		}
		if (!"get".equals(mode) && !"set".equals(mode)) {
			command.add("-O");
			command.add("vcd");
		}
		return command.toArray(new String[command.size()]);
	}

	@Override
	public Closeable getInput(IProgress iProgress) {
		Process p;
		Closeable input = null;
		final MessageConsoleStream console = ImpulseBase.console.newMessageStream();
		try {
			String[] ca = prepareCommand(null);
			String line = "";
			for (String s : ca)
				line += s + " ";
			console.println(line);
			p = Runtime.getRuntime().exec(ca);
			input = p.getInputStream();
			final BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			Actives.run(new IExecutable() {

				@Override
				public void execute(IProgress p) {
					String line = null;
					try {
						while ((line = error.readLine()) != null) {
							final String log = "Sigrok:" + line;
							Actives.runInMain(new IExecutable() {

								@Override
								public void execute(IProgress p) {
									console.println(log);

								}
							});
						}

					} catch (IOException e) {
					}

				}
			});
			Actives.sleep(100);
		} catch (IOException e) {
		} finally {
			try {
				console.close();
			} catch (IOException e) {
			}
		}
		return input;
	}

	@Override
	public IRecordReader newReader(Closeable input) {
		SerializerDescriptor descr = Elements.serializers.get("de.toem.impulse.serializer.vcd");
		if (descr != null && descr.hasReader()) {
			ICellReader reader = descr.newReader((InputStream) input);
			if (reader instanceof IRecordReader)
				return (IRecordReader) reader;
		}
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

	@Override
	public int getNature() {
		return IRecordPort.NATURE_CONNECT | IRecordPort.NATURE_FLOATING | IRecordPort.NATURE_REFRESH_CONTINUOUS;
	}
}
