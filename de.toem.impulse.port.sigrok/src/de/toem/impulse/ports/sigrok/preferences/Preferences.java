package de.toem.impulse.ports.sigrok.preferences;

import java.io.File;

import de.toem.impulse.ports.sigrok.SigrokPort;


public class Preferences {

    public static final String COMMAND = "sigrok-cli";
    
	public static final String P_PATH = "pathPreference";
	
	static public String getCliPath(){
	    return SigrokPort.getDefault().getPreferenceStore().getString(P_PATH);
	}
	   static public String getCliCommand(){
	        String path = Preferences.getCliPath().trim();
	        if (!path.isEmpty() && !path.endsWith(File.separator))
	            path+= File.separator;
        return path += COMMAND;
	    }
}
