//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.opc;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ImpulseOpcExtension implements BundleActivator {

	private static BundleContext context;

	public static final String PLUGIN_ID = "de.toem.impulse.extension.opc"; 
	  
	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		ImpulseOpcExtension.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		ImpulseOpcExtension.context = null;
	}

}
