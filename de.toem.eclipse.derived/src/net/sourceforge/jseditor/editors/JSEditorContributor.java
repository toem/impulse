package net.sourceforge.jseditor.editors;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

public class JSEditorContributor extends BasicTextEditorActionContributor
    {

        protected RetargetTextEditorAction formatProposal;
        static protected ResourceBundle bundle =new ResourceBundle(){

            @Override
            protected Object handleGetObject(String key) {
                if ("de.toem.actions.format.label".equals(key))
                    return "Format";

                return null;
            }

            @Override
            public Enumeration<String> getKeys() {
                return null;
            }};

        public JSEditorContributor()
        {
            super();

            formatProposal = new RetargetTextEditorAction(bundle, "de.toem.actions.format");

        }

        public void contributeToMenu(IMenuManager mm)
        {
            super.contributeToMenu(mm);
            IMenuManager editMenu = mm.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
            if (editMenu != null)
            {
                editMenu.add(new Separator());
                editMenu.add(formatProposal);
            }
        }

        /**
         * Sets the active editor to this contributor. This updates the actions to
         * reflect the editor.
         * 
         * @see EditorActionBarContributor#editorChanged
         */
        public void setActiveEditor(IEditorPart part)
        {

            super.setActiveEditor(part);

            ITextEditor editor = null;
            if (part instanceof ITextEditor)
                editor = (ITextEditor) part;

            formatProposal.setAction(getAction(editor, "de.toem.actions.format"));

        }

        /**
         * 
         * Contributes to the toolbar.
         * 
         * @see EditorActionBarContributor#contributeToToolBar
         */
        public void contributeToToolBar(IToolBarManager tbm)
        {
            super.contributeToToolBar(tbm);
//            tbm.add(new Separator());
        }

    }