package de.toem.impulse.ports.jdt;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.toem.pattern.information.InformationGroup;

/**
 * The activator class controls the plug-in life cycle
 */
public class JdtPorts extends AbstractUIPlugin {

    public final static InformationGroup EXTENSION_GROUP = new InformationGroup("de.toem.impulse.ports.jdt", "Java Tooling", ""){public String getImage(){
        return "de.toem.impulse.ports.jdt";}};
    
	// The plug-in ID
	public static final String PLUGIN_ID = "de.toem.impulse.ports.jdt"; //$NON-NLS-1$

	// The shared instance
	private static JdtPorts plugin;
	
	/**
	 * The constructor
	 */
	public JdtPorts() {
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
	public static JdtPorts getDefault() {
		return plugin;
	}

}
