package de.toem.impulse.charts.nebula;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.toem.pattern.information.InformationGroup;

public class ImpulseNebulaCharts implements BundleActivator {

    private static BundleContext context;
    
    // The plug-in ID
    public static final String PLUGIN_ID = "de.toem.impulse.charts.nebula"; //$NON-NLS-1$

    public final static InformationGroup EXTENSION_GROUP = new InformationGroup("de.toem.impulse.charts.nebula", "Nebula Charts", ""){public String getImage(){
        return "port.record.example.16";}};
        
    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        ImpulseNebulaCharts.context = bundleContext;
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        ImpulseNebulaCharts.context = null;
    }

}
