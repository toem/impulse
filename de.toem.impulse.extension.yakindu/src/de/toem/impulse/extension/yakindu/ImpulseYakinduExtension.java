package de.toem.impulse.extension.yakindu;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ImpulseYakinduExtension extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "de.toem.impulse.extension.statecharts"; //$NON-NLS-1$

    // The shared instance
    private static ImpulseYakinduExtension plugin;

    /**
     * The constructor
     */
    public ImpulseYakinduExtension() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
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
    public static ImpulseYakinduExtension getDefault() {
        return plugin;
    }

}
