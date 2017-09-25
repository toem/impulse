package de.toem.impulse.serializer.wizard;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginModelFactory;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.templates.AbstractTemplateSection;
import org.eclipse.pde.ui.templates.OptionTemplateSection;
import org.eclipse.pde.ui.templates.PluginReference;
import org.osgi.framework.Bundle;

import de.toem.impulse.extension.examples.ExtensionToolkit;

public class NewReaderTemplate extends OptionTemplateSection {

	/**
	 * Constructor for HelloWorldTemplate.
	 */
	public NewReaderTemplate() {
		setPageCount(1);
		createOptions();
	}

	@Override
	public String getSectionId() {
		return "reader"; //$NON-NLS-1$
	}

	protected ResourceBundle getPluginResourceBundle() {
		return new ResourceBundle(){

			@Override
			protected Object handleGetObject(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Enumeration<String> getKeys() {
				// TODO Auto-generated method stub
				return null;
			}};
	}

	protected URL getInstallURL() {
		return ExtensionToolkit.getDefault().getInstallURL();
	}

	protected String getTemplateDirectory() {
		return "templates";
	}

	/*
	 * @see ITemplateSection#getNumberOfWorkUnits()
	 */
	@Override
	public int getNumberOfWorkUnits() {
		return super.getNumberOfWorkUnits() + 1;
	}

	private void createOptions() {
		// first page
		addOption(KEY_PACKAGE_NAME, "Java Package Name:", (String) null, 0);
		addOption("className", "Reader Class Name:", "MyReader", 0);
		addOption("readerId", "Reader Id:", (String) null, 0);
		addOption("readerLabel", "Reader Label:", "My Reader", 0);
		addOption("readerDescription", "Reader Label:", "This is my first reader", 0);
		addOption("readerTemplate", "Template:", new String[][] { { "text", "Text Line Reader" },
				{ "binary", "Binary Data Reader" }, { "empty", "Empty Reader" } }, "text", 0);
		addOption("createStreamReader", "Create stream reader (Handles continues input stream when using ports)", false,
				0);
		addOption("readerConfiguration",
				"Reader Configuration:", new String[][] { { "none", "No Reader Configuration" },
						{ "default", "Default Reader Configuration" }/*, { "custom", "Custom Reader Configuration" } */},
				"none", 0);
		addOption("contentTypeId", "Content Type Id:",(String) null, 0);
		addOption("createContentType", "Create Content Type:", true, 0);
		addOption("contentLabel", "     Content Type Label:", "My Content", 0);
		addOption("fileExtensions", "     File extensions (e.g. txt,xls):", "myExt", 0);
	}

	@Override
	protected void initializeFields(IFieldData data) {
		// In a new project wizard, we don't know this yet - the
		// model has not been created
		initializeFields(data.getId());

	}

	@Override
	public void initializeFields(IPluginModelBase model) {
		// In the new extension wizard, the model exists so
		// we can initialize directly from it
		initializeFields(model.getPluginBase().getId());
	}

	public void initializeFields(String id) {
		initializeOption(KEY_PACKAGE_NAME, getFormattedPackageName(id));
		initializeOption("readerId", getFormattedPackageName(id)+".impulse.serializer.my");
		initializeOption("contentTypeId", getFormattedPackageName(id)+".contentType.my");
	}

	@Override
	public boolean isDependentOnParentWizard() {
		return true;
	}

	@Override
	public void addPages(Wizard wizard) {
		WizardPage page0 = createPage(0, "New impulse Reader olug-in project");
		page0.setTitle("impulse Reader");
		page0.setDescription("This template will generate a new impulse reader for text or binary input.");
		wizard.addPage(page0);

		markPagesAdded();
	}

	/**
	 * @see AbstractTemplateSection#isOkToCreateFile(File)
	 */
	@Override
	protected boolean isOkToCreateFile(File sourceFile) {
		boolean isOk = true;
		String fileName = sourceFile.getName();
		if (fileName.equals("contexts.xml")) {
			isOk = true;
		}
		return isOk;
	}

	@Override
	public String[] getNewFiles() {
		return new String[] { "icons/" };
	}
	
	@Override
	public String getUsedExtensionPoint() {
		return "org.eclipse.ui.views";
	}

	@Override
	protected void updateModel(IProgressMonitor monitor) throws CoreException {

		IPluginBase plugin = model.getPluginBase();
		IPluginModelFactory factory = model.getPluginFactory();

		IPluginExtension extension = createExtension("de.toem.pattern.general.serializer", true);

		String fullClassName = getStringOption(KEY_PACKAGE_NAME) + "." + getStringOption("className");
		IPluginElement readerElement = factory.createElement(extension);
		readerElement.setName("serializer");
		readerElement.setAttribute("id", getStringOption("readerId"));
		readerElement.setAttribute("label", getStringOption("readerLabel"));
		readerElement.setAttribute("description", getStringOption("readerDescription"));
		readerElement.setAttribute("reader", fullClassName);
		readerElement.setAttribute("contentType", getStringOption("contentTypeId"));
		readerElement.setAttribute("defaultConfiguration", "default".equals(getStringOption("readerConfiguration"))?"true":"false");
		extension.add(readerElement);

		if (!extension.isInTheModel())
			plugin.add(extension);

		if (getBooleanOption("createContentType")) {
			extension = createExtension("org.eclipse.core.contenttype.contentTypes", true);
			IPluginElement contentTypeElement = factory.createElement(extension);
			contentTypeElement.setName("content-type");
			contentTypeElement.setAttribute("base-type", "de.toem.impulse.contentType.record");
			contentTypeElement.setAttribute("file-extensions", getStringOption("fileExtensions"));
			contentTypeElement.setAttribute("id", getStringOption("contentTypeId"));
			contentTypeElement.setAttribute("name", getStringOption("contentLabel"));
			contentTypeElement.setAttribute("priority", "normal");
			extension.add(contentTypeElement);
			IPluginElement describerElement = factory.createElement(contentTypeElement);
			describerElement.setName("describer");
			describerElement.setAttribute("class", "de.toem.impulse.serializer.RecordContentDescriber");
			describerElement.setAttribute("plugin", "de.toem.impulse.base");
			contentTypeElement.add(describerElement);
			IPluginElement parameterElement = factory.createElement(describerElement);
			parameterElement.setName("parameter");
			parameterElement.setAttribute("name", "contentType");
			parameterElement.setAttribute("value", getStringOption("contentTypeId"));
			describerElement.add(parameterElement);

			if (!extension.isInTheModel())
				plugin.add(extension);
		}
	}

	@Override
	public IPluginReference[] getDependencies(String schemaVersion) {
		ArrayList<PluginReference> result = new ArrayList<PluginReference>();
		if (schemaVersion != null)
			result.add(new PluginReference("org.eclipse.core.runtime", null, 0));
		result.add(new PluginReference("org.eclipse.ui", null, 0));
		result.add(new PluginReference("org.eclipse.jface", null, 0));
		result.add(new PluginReference("de.toem.impulse.base", null, 0));
		result.add(new PluginReference("de.toem.eclipse.toolkits.editor", null, 0));
		result.add(new PluginReference("de.toem.eclipse.toolkits.general", null, 0));
		result.add(new PluginReference("de.toem.basics.general", null, 0));
		result.add(new PluginReference("de.toem.pattern.general", null, 0));
		result.add(new PluginReference("de.toem.eclipse.hooks", null, 0));
		return result.toArray(new IPluginReference[result.size()]);
	}

	protected String getFormattedPackageName(String id) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < id.length(); ++i) {
			char ch = id.charAt(i);
			if (buffer.length() == 0) {
				if (Character.isJavaIdentifierStart(ch))
					buffer.append(Character.toLowerCase(ch));
			} else if ((Character.isJavaIdentifierPart(ch)) || (ch == '.')) {
				buffer.append(ch);
			}
		}
		String packageName = buffer.toString().toLowerCase(Locale.ENGLISH);
		if (packageName.length() != 0)
			return packageName;
		return "reader"; 
	}
}
