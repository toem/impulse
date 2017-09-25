package de.toem.impulse.serializer.wizard;

import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.templates.ITemplateSection;
import org.eclipse.pde.ui.templates.NewPluginTemplateWizard;

public class NewReaderWizard extends NewPluginTemplateWizard {
	/**
	 * Constructor for ViewNewWizard.
	 */
	public NewReaderWizard() {
		super();
	}

	@Override
	public void init(IFieldData data) {
		super.init(data);
		setWindowTitle("new view templ");
	}

	/*
	 * @see NewExtensionTemplateWizard#createTemplateSections()
	 */
	@Override
	public ITemplateSection[] createTemplateSections() {
		return new ITemplateSection[] {new NewReaderTemplate()};
	} 
}
