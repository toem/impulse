package de.toem.impulse.extension.opc;

public class LoggerFactory {

	static Logger logger;
	
	synchronized public static Logger getLogger(Class<?> cs){
		if (logger == null)
			logger = new Logger();
		return logger;
	}
}
