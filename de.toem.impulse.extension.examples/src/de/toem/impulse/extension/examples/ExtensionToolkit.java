package de.toem.impulse.extension.examples;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.toem.pattern.information.InformationGroup;

public class ExtensionToolkit extends AbstractUIPlugin{
    public final static InformationGroup EXTENSION_GROUP = new InformationGroup("de.toem.impulse.extension.examples", "Extension Examples", ""){public String getImage(){
            return "port.record.example.16";}};
    public static final String PLUGIN_ID = "de.toem.impulse.extension.examples";    

	// The shared instance
	private static ExtensionToolkit plugin;
	
	public URL getInstallURL() {
		return getDefault().getBundle().getEntry("/");
	}
	
	/**
	 * The constructor
	 */
	public ExtensionToolkit() {
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
	public static ExtensionToolkit getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

}
