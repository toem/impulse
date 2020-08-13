package de.toem.impulse.extension.birt.i18n;

import org.eclipse.osgi.util.NLS;


public class I18n extends de.toem.impulse.i18n.I18n {
    private static final String BASE_NAME = "de.toem.impulse.extension.birt.i18n.i18n";



    public static String General_ToDo;

    
    
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BASE_NAME, I18n.class);
    }

}
