package de.toem.impulse.ports.sigrok.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import de.toem.impulse.ports.sigrok.SigrokPort;


public class PreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(SigrokPort.getDefault().getPreferenceStore());
		setDescription("Select the path of the cli command (e.g. /usr/bin/ or C:\\sigrok\\). If left empty it will try to execute the command 'sigrok-cli' from any system executable path");
	}
	
	@Override
    protected Control createContents(Composite parent) {
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, SigrokPort.PLUGIN_ID+ "." + "sigrok_preferences");
        return super.createContents(parent);
    }
    @Override
	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(Preferences.P_PATH, 
				"&Sigrok-CLI Path:", getFieldEditorParent()));
			
	}
    @Override
	public void init(IWorkbench workbench) {
	}
	
}