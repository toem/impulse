/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.group.command.CreateColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CreateColumnGroupDialog extends Dialog {

    private Button createButton;
    private Text groupNameText;
    private ILayer contextLayer;

    private CreateColumnGroupDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.BORDER | SWT.TITLE
                | SWT.APPLICATION_MODAL);
        setBlockOnOpen(false);
    }

    public static CreateColumnGroupDialog createColumnGroupDialog(Shell shell) {
        return new CreateColumnGroupDialog(shell);
    }

    public void setContextLayer(ILayer layer) {
        this.contextLayer = layer;
    }

    @Override
    public void create() {
        super.create();
        getShell()
                .setText(
                        Messages.getString("ColumnGroups.createColumnGroupDialogTitle")); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(final Composite parent) {

        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

        GridDataFactory.fillDefaults().minSize(200, 100)
                .align(SWT.FILL, SWT.FILL).grab(true, false)
                .applyTo(createInputPanel(composite));

        Composite buttonPanel = createButtonSection(composite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM)
                .grab(true, true).applyTo(buttonPanel);

        return composite;
    }

    private Composite createButtonSection(Composite composite) {

        Composite panel = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 0;
        layout.makeColumnsEqualWidth = false;
        layout.horizontalSpacing = 2;
        panel.setLayout(layout);

        this.createButton = createButton(panel, IDialogConstants.CLIENT_ID,
                Messages.getString("ColumnGroups.createButtonLabel"), false); //$NON-NLS-1$
        GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM)
                .grab(true, true).applyTo(this.createButton);

        this.createButton.setEnabled(false);
        getShell().setDefaultButton(this.createButton);

        this.createButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                doColumnGrouping();
            }
        });

        Button closeButton = createButton(
                panel,
                IDialogConstants.CANCEL_ID,
                Messages.getString("AbstractStyleEditorDialog.cancelButton"), false); //$NON-NLS-1$
        GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM)
                .grab(false, false).applyTo(closeButton);

        return panel;
    }

    private Composite createInputPanel(final Composite composite) {
        final Composite row = new Composite(composite, SWT.NONE);
        row.setLayout(new GridLayout(2, false));

        final Label createLabel = new Label(row, SWT.NONE);
        createLabel
                .setText(Messages.getString("ColumnGroups.createGroupLabel") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
        GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER)
                .applyTo(createLabel);

        this.groupNameText = new Text(row, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(this.groupNameText);
        this.groupNameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                CreateColumnGroupDialog.this.createButton.setEnabled(CreateColumnGroupDialog.this.groupNameText.getText().length() > 0);
            }
        });
        this.groupNameText.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (CreateColumnGroupDialog.this.createButton.isEnabled()) {
                    doColumnGrouping();
                }
            }
        });

        return row;
    }

    public void terminateDialog() {
        close();
    }

    @Override
    public boolean close() {
        return super.close();
    }

    private void doColumnGrouping() {
        BusyIndicator.showWhile(super.getShell().getDisplay(), new Runnable() {
            @Override
            public void run() {
                final CreateColumnGroupCommand command = new CreateColumnGroupCommand(
                        CreateColumnGroupDialog.this.groupNameText.getText());
                try {
                    CreateColumnGroupDialog.this.contextLayer.doCommand(command);
                } finally {
                    terminateDialog();
                }
            }
        });
    }
}
