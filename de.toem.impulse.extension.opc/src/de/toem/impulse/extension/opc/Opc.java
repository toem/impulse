//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.opc;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.toem.pattern.information.InformationGroup;

public class Opc implements BundleActivator {

	private static BundleContext context;

    public final static InformationGroup EXTENSION_GROUP = new InformationGroup("de.toem.impulse.extension.opc", "OPC", "");

    
	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Opc.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Opc.context = null;
	}

}
