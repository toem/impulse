//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.ports.sigrok;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.toem.pattern.information.InformationGroup;


/**
 * The activator class controls the plug-in life cycle
 */
public class SigrokPort extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "de.toem.impulse.port.sigrok"; 

    public final static InformationGroup EXTENSION_GROUP = new InformationGroup("de.toem.impulse.extension.scopes", "Scopes", ""){public String getImage(){
        return "port.record.sigrok.16";}};

        
    // The shared instance
    private static SigrokPort plugin;
    
    /**
     * The constructor
     */
    public SigrokPort() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static SigrokPort getDefault() {
        return plugin;
    }
}
