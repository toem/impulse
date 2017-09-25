package de.toem.impulse.ports.sigrok.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.toem.impulse.ports.sigrok.SigrokPort;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {


	public void initializeDefaultPreferences() {
		IPreferenceStore store = SigrokPort.getDefault().getPreferenceStore();

	}

}
