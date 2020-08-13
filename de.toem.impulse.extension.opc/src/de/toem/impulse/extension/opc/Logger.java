package de.toem.impulse.extension.opc;

import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.impulse.ImpulseBase;
import de.toem.pattern.threading.Actives;
import de.toem.pattern.threading.IExecutable;
import de.toem.pattern.threading.IProgress;

public class Logger {

	private static boolean logToConsole;
	private static IController logToController;
	private static StringBuilder logToControllerText;

	public static void logToConsole(boolean logToConsole) {
		Logger.logToConsole = logToConsole;
	}

	public static void logToController(IController controller) {
		Logger.logToController = controller;
		if (controller != null)
			logToControllerText = new StringBuilder();
	}

	public boolean isDebugEnabled() {
		return logToConsole;
	}

	public void log(String sev, String message, Object... o) {
		if (logToConsole || logToController != null)
			try {
				String formatted = message;
				for (Object on : o) {
					int idx = formatted.indexOf("{}");
					if (idx >= 0)
						formatted = formatted.replaceFirst("\\{\\}", String.valueOf(on));
					else
						formatted += " " + String.valueOf(on);
				}
				if (logToConsole)
				ImpulseBase.getDefault().defaultConsoleStream.println("opc: " + sev + " " + formatted);
				if (logToController!=null){
					logToControllerText.append("opc: " + sev + " " + formatted+"<br/>");
					Actives.runInMain(new IExecutable(){

						@Override
						public void execute(IProgress p) {
							logToController.setValue(logToControllerText.toString());
							
						}});
					
				}
					
			} catch (Throwable e) {
			}
	}

	public void log(String sev, String message, Throwable o) {
		if (logToConsole)
			ImpulseBase.getDefault().defaultConsoleStream.println("opc: " + sev + " " + message + ":" + o != null ? o.getLocalizedMessage() : "");
	}

	public void trace(String message, Object... o) {
		log("trace", message, o);
	}

	public void trace(String message, Throwable o) {
		log("trace", message, o);
	}

	public void warn(String message, Object... o) {
		log("warn", message, o);
	}

	public void warn(String message, Throwable o) {
		log("warn", message, o);
	}

	public void debug(String message, Object... o) {
		log("debug", message, o);
	}

	public void debug(String message, Throwable o) {
		log("debug", message, o);
	}

	public void error(String message, Object... o) {
		log("error", message, o);
	}

	public void error(String message, Throwable o) {
		log("error", message, o);
	}

	public void info(String message, Object... o) {
		log("info", message, o);
	}

	public void info(String message, Throwable o) {
		log("info", message, o);
	}

	public boolean isTraceEnabled() {
		return logToConsole;
	}

}
