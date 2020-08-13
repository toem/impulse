/*
 * $RCSfile: JSEditor.java,v $
 *
 * Copyright 2002
 * CH-1700 Fribourg, Switzerland
 * All rights reserved.
 *
 *========================================================================
 * Modifications history
 *========================================================================
 * $Log: JSEditor.java,v $
 * Revision 1.5  2003/08/14 15:14:15  agfitzp
 * Removed thread hack from automatic update
 *
 * Revision 1.4  2003/07/04 17:26:56  agfitzp
 * New hack, update in a new thread only if we're not already in the middle of updating
 *
 * Revision 1.3  2003/06/21 03:48:51  agfitzp
 * fixed global variables as functions bug
 * fixed length calculation of instance variables
 * Automatic outlining is now a preference
 *
 * Revision 1.2  2003/05/28 20:47:58  agfitzp
 * Outline the document, not the file.
 *
 * Revision 1.1  2003/05/28 15:17:12  agfitzp
 * net.sourceforge.jseditor 0.0.1 code base
 *
 *========================================================================
 */

package net.sourceforge.jseditor.editors;

import java.util.ResourceBundle;

import javax.script.ScriptException;

import net.sourceforge.jseditor.views.JSOutlinePage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.5 $
 * @author $Author: agfitzp $, $Date: 2003/08/14 15:14:15 $
 */
public class JSEditor extends TextEditor implements ISelectionChangedListener {

    protected JSColorManager colorManager = new JSColorManager();
    protected JSOutlinePage outlinePage;
    protected JSConfiguration configuration;
    private IEditorInput input;
    protected boolean updating = false;

    /**
     * Constructor for SampleEditor.
     */
    public JSEditor() {
        super();

        setDocumentProvider(createDocumentProvider());

        configuration = createViewerConfiguration();
        setSourceViewerConfiguration(configuration);
        

    }

    protected IDocumentProvider createDocumentProvider() {
        return new JSDocumentProvider();
    }

    protected JSConfiguration createViewerConfiguration() {
        return new JSConfiguration(colorManager,this.getPreferenceStore());
    }

    protected void doSetInput(IEditorInput newInput) throws CoreException {
        super.doSetInput(newInput);
        this.input = newInput;

        IDocument document = getDocumentProvider().getDocument(input);
        if (document != null)
            document.addDocumentListener(new IDocumentListener() {

                @Override
                public void documentAboutToBeChanged(DocumentEvent event) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void documentChanged(DocumentEvent event) {
                    validateAndMark();

                }
            });

    }

    /**
     * Method declared on IEditorPart
     * 
     * @param monitor
     */
    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);

        if (outlinePage != null) {
            outlinePage.update();
        }
    }

    /**
	 *
	 */
    public void dispose() {
        colorManager.dispose();
        super.dispose();
    }

    /**
     * Method declared on IAdaptable
     * 
     * @param key
     * 
     * @return
     */
    public Object getAdapter(Class key) {
        if (key.equals(IContentOutlinePage.class)) {
            IDocument document = getDocumentProvider().getDocument(getEditorInput());

            outlinePage = new JSOutlinePage(document);
            outlinePage.addSelectionChangedListener(this);
            return outlinePage;
        }

        return super.getAdapter(key);
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        if (null != event) {
            if (event.getSelection() instanceof IStructuredSelection) {
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                if (null != sel) {
                    JSElement fe = (JSElement) sel.getFirstElement();
                    if (null != fe) {
                        selectAndReveal(fe.getStart(), fe.getLength());
                    }
                }
            }
        }
    }

    /**
     * Updates all content dependent actions.
     * 
     * This might be a hack: We're trapping this update to ensure that the outline is always up to date.
     */
    protected void updateContentDependentActions() {
        super.updateContentDependentActions();

        if (!updating) {
            if (configuration.getAutomaticOutliningPreference()) {
                if (outlinePage != null) {
                    updating = true;

                    outlinePage.update();
                    updating = false;
                }
            }
        }
    }
    @Override
    protected void createActions() {
        super.createActions();
        ResourceBundle bundle = JSEditorContributor.bundle;
        IAction a=  new TextOperationAction(bundle, "de.toem.actions.format.", this, ISourceViewer.FORMAT);
        a.setActionDefinitionId("de.toem.actions.format");
        setAction("de.toem.actions.format",a );
        

        // setAction("ContentAssistProposal", new TextOperationAction(bundle, "ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS));
        // setAction("ContentAssistTip", new TextOperationAction(bundle, "ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION));
    }

    @Override
    protected void editorContextMenuAboutToShow(IMenuManager menu) {
        super.editorContextMenuAboutToShow(menu);
        addAction(menu, "de.toem.actions.format");
    }

    Annotation errorAnnotation = new Annotation("org.eclipse.ui.workbench.texteditor.error", false, "an error");

    protected void validateAndMark() {
        try {
            IDocument document = getDocumentProvider().getDocument(input);
            IAnnotationModel annotationModel = getDocumentProvider().getAnnotationModel(input);
            if (annotationModel != null) {
                annotationModel.removeAnnotation(errorAnnotation);

                String text = document.get();
                ScriptException e = JSSyntaxErrorParser.parse(text);
                if (e != null) {
                    String message = e.getLocalizedMessage();
                    if (message.indexOf(':') > 0)
                        message = message.substring(message.indexOf(':') + 1);
                    errorAnnotation.setText(message);
                    int offset = document.getLineOffset(e.getLineNumber() - 1);
                    int length = document.getLineLength(e.getLineNumber() - 1);
                    annotationModel.addAnnotation(errorAnnotation, new Position(offset, length));

                }
            }
        } catch (Throwable e) {
        }
    }
}