//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.ports.sigrok;

import java.util.ArrayList;
import java.util.List;

import de.toem.basics.core.Utils;
import de.toem.impulse.ports.sigrok.preferences.Preferences;
import de.toem.pattern.element.ICell;
import de.toem.pattern.messages.Messages;

public class SigrokScan {

	class ScanResult {
		String id;
		String name;
		//int probes;
		//String[] probeNames;
	}

	public ScanResult[] scan() {

		String[] command = new String[] { Preferences.getCliCommand(), "-l", "2", "--scan" };
		String[] result = Utils.execute(command);
		List<ScanResult> list = new ArrayList<ScanResult>();
		if (result != null)
			for (String line : result) {
				if (line.contains(" - ") && line.contains("with") /*&& (line.contains("probe") || line.contains("channel"))*/) {
					int idx0 = line.indexOf(" - ");
					int idx1 = line.indexOf("with", idx0);
					//int idx2 = line.contains("probe") ? line.indexOf("probe", idx1) : line.indexOf("channel", idx1);
					//int idx3 = line.indexOf(":", idx2);

					ScanResult scan = new ScanResult();
					scan.id = line.substring(0, idx0).trim();
					scan.name = line.substring(idx0 + 3, idx1).trim();
					//scan.probes = Utils.parseInt(line.substring(idx1 + 4, idx2).trim(), 0);
					//scan.probeNames = line.substring(idx3 + 1).trim().split(" ");
					// Utils.log(scan.id, scan.name, scan.probes,
					// scan.probeNames);
					//if (scan.probes > 0 && scan.probes == scan.probeNames.length) {
					list.add(scan);
					//}
				}
			}
		return list.toArray(new ScanResult[list.size()]);
	}

	public String show(String driver) {

		String[] command = new String[] { Preferences.getCliCommand(), "--show", "-d", driver };
		String[] result = Utils.execute(command);
		if (result != null) {
			String text = "";
			for (String line : result)
				text += line + "\n";
			return text;
		}
		return null;
	}

	public String help() {

		String[] command = new String[] { Preferences.getCliCommand(), "--help" };
		String[] result = Utils.execute(command);
		if (result != null) {
			String text = "";
			for (String line : result)
				text += line + "\n";
			return text;
		}
		return null;
	}

	public String version() {

		String[] command = new String[] { Preferences.getCliCommand(), "-V" };
		String[] result = Utils.execute(command);
		if (result != null) {
			String text = "";
			for (String line : result)
				text += line + "\n";
			return text;
		}
		return null;
	}

	public String supported() {

		String[] command = new String[] { Preferences.getCliCommand(), "-L" };
		String[] result = Utils.execute(command);
		if (result != null) {
			String text = "";
			for (String line : result)
				text += line + "\n";
			return text;
		}
		return null;
	}

	public String getOptions(ICell cell) {

		if (cell instanceof SigrokAdapter) {
			String[] command = ((SigrokAdapter) cell).prepareCommand("get");
			String[] result = Utils.execute(command);
			if (result != null) {
				String text = "";
				for (String line : result)
					text += line + "\n";
				return text;
			}
		}
		return null;
	}

	public String setOptions(ICell cell) {
		if (cell instanceof SigrokAdapter) {
			String[] command = ((SigrokAdapter) cell).prepareCommand("set");
			String[] result = Utils.execute(command);
			if (result != null) {
				if (result.length == 0) {
					return "ok";
				} else {
					String text = "";
					for (String line : result)
						text += line + "\n";
					return text;
				}
			}
		}
		return null;
	}
}
