//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.opc.ua;

import java.io.File;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.controller.abstrac.IController;
import de.toem.eclipse.toolkits.controller.base.ButtonController;
import de.toem.eclipse.toolkits.controller.base.CheckController;
import de.toem.eclipse.toolkits.controller.base.ComboEditController;
import de.toem.eclipse.toolkits.controller.base.ComboSelectController;
import de.toem.eclipse.toolkits.controller.base.PasswordController;
import de.toem.eclipse.toolkits.controller.base.RadioSetController;
import de.toem.eclipse.toolkits.controller.base.TabFolderController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.controller.cells.CellTreeController;
import de.toem.eclipse.toolkits.dialog.AbstractDialog;
import de.toem.eclipse.toolkits.dialog.ControlProviderDialog;
import de.toem.eclipse.toolkits.provider.control.NameDescriptionEnableProvider;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.DialogToolkit;
import de.toem.eclipse.toolkits.tlk.IEditorParent;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.eclipse.toolkits.util.EclipseUtils;
import de.toem.impulse.cells.ports.AbstractPortAdapterBaseCell;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.MultiPipePort;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.element.ElementHierarchyModifier;
import de.toem.pattern.element.Elements;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.IElement;
import de.toem.pattern.element.IElementModifier;
import de.toem.pattern.general.scan.TextScanResult;
import de.toem.pattern.messages.Messages;
import de.toem.pattern.threading.Actives;
import de.toem.pattern.threading.IExecutable;
import de.toem.pattern.threading.IProgress;
import de.toem.toolkits.text.MultilineText;

public class OpcUaAdapterDialog extends ControlProviderDialog {

    public OpcUaAdapterDialog(IEditorParent parent, IController controller) {
        super(parent, controller, getControls());
    }

    public OpcUaAdapterDialog(IEditorParent parent, IElement parentElement, ICell newCell) {
        super(parent, parentElement, newCell, getControls());
    }

    public OpcUaAdapterDialog(IEditorParent parent, IElement element) {
        super(parent, element, getControls());
    }

    public OpcUaAdapterDialog(IEditorParent parent, List<IElement> elements) {
        super(parent, elements, getControls());
    }

    public OpcUaAdapterDialog(IEditorParent parent) {
        super(parent, getControls());
    }

    public OpcUaAdapterDialog() {
        super(getControls());
    }

    protected void createConcreteControl(Composite container) {
        IControlProvider provider = getControls();
        provider.setLayout(COLS, COLS);
        tlk.addControls(container, provider);
    }

    public static IControlProvider getControls() {
        IControlProvider provider = new AbstractControlProvider() {

            CellTreeController tree;
            IController user;
            IController password;
            IController securityPolicy;
            IController certInfo;

            @Override
            public boolean fillThis() {
                try {

                    Composite tabFolder = tlk().addTabFolder(container(), new TabFolderController(editor(), "opc.adapter.dialog.tab"), cols(), SWT.NULL, null);

                    // general tab
                    Composite general = tlk().addComposite(tabFolder, null, cols(), null, SWT.NULL, "General", null);

                    ComboEditController server = (ComboEditController) tlk().addCombo(general,
                            new ComboEditController(editor(), OpcUaAdapter.class.getField("server"), true)
                                    .setHintIdentifier("de.toem.impulse.extension.opc.serverHistory").setHintDomain(IElement.DOCUMENT),
                            tlk().ld(cols() - 1, SWT.FILL, TLK.IGNORE_CONTROL), DialogToolkit.LABEL | SWT.BORDER, "Endpoint:");

                    tlk().addButtonSet(general, new RadioSetController(editor(), OpcUaAdapter.class.getField("identification")) {
                        @Override
                        protected void doUpdateExternal() {
                            tlk().showControl(user, this.getValueAsInt() == OpcUaAdapter.IDENT_PASSWORD);
                            tlk().showControl(password, this.getValueAsInt() == OpcUaAdapter.IDENT_PASSWORD);
                        }
                    }, 2, cols(), SWT.RADIO | TLK.LABEL, "Identification:", OpcUaAdapter.IDENT_OPTIONS, null);
                    user = tlk().addText(general, new TextController(editor(), OpcUaAdapter.class.getField("user")), cols() - 1,
                            DialogToolkit.LABEL | SWT.BORDER, "User/Passwd:");
                    password = tlk().addText(general, new PasswordController(editor(), OpcUaAdapter.class.getField("password")), 1, SWT.BORDER | SWT.PASSWORD,
                            null);
                    tlk().addButtonSet(general, new RadioSetController(editor(), OpcUaAdapter.class.getField("securityMode")) {
                        @Override
                        protected void doUpdateExternal() {
                            tlk().showControl(securityPolicy, this.getValueAsInt() != OpcUaAdapter.SECURITY_NONE);
                        }
                    }, 3, cols(), SWT.RADIO | TLK.LABEL, "Security mode:", OpcUaAdapter.SECURITY_OPTIONS, null);
                    securityPolicy = tlk().addButtonSet(general, new RadioSetController(editor(), OpcUaAdapter.class.getField("securityPolicy")), 4, cols(),
                            SWT.RADIO | TLK.LABEL, "Security policy:", OpcUaAdapter.POLICY_OPTIONS, null);

                    tlk().addText(general, new TextController(editor(), OpcUaAdapter.class.getField("publishRate")),
                            tlk().ld(cols() - 1, SWT.FILL, TLK.IGNORE_CONTROL), DialogToolkit.LABEL | SWT.BORDER, "Publishing rate[ms]:");
                    tlk().addButton(general, new CheckController(editor(), OpcUaAdapter.class.getField("logToConsole")),
                            tlk().ld(cols(), SWT.RIGHT, SWT.DEFAULT), SWT.CHECK, "Log to impulse console", null);
                    tlk().addNothing(general, tlk().ld(cols(), TLK.GRAB, SWT.DEFAULT));

                    // details tab
                    Composite details = tlk().addComposite(tabFolder, null, cols(), null, SWT.NULL, "Details", null);

                    tlk().addText(details, new TextController(editor(), OpcUaAdapter.class.getField("applicationName")),
                            tlk().ld(cols() - 1, SWT.FILL, TLK.IGNORE_CONTROL), DialogToolkit.LABEL | SWT.BORDER, "Application name:");
                    tlk().addText(details, new TextController(editor(), OpcUaAdapter.class.getField("applicationUri")),
                            tlk().ld(cols() - 1, SWT.FILL, TLK.IGNORE_CONTROL), DialogToolkit.LABEL | SWT.BORDER, "Application Uri:");
                    tlk().addText(details, new TextController(editor(), OpcUaAdapter.class.getField("productUri")), tlk().ld(cols() - 1, SWT.FILL, TLK.IGNORE_CONTROL),
                            DialogToolkit.LABEL | SWT.BORDER, "Product Uri:");
                    tlk().addText(details, new TextController(editor(), OpcUaAdapter.class.getField("sessionTimeout")) {

                        @Override
                        protected TextScanResult doCheck(String formatted, int options) {
                            if (Utils.isEmpty(formatted) || Utils.parseInt(formatted, -1) > 0)
                                return TextScanResult.SCAN_OK;
                            return TextScanResult.SCAN_ERROR;
                        }

                    }, tlk().ld(cols() - 1, SWT.FILL, TLK.IGNORE_CONTROL), DialogToolkit.LABEL | SWT.BORDER, "Session timeout[ms]:");
                    tlk().addText(details, new TextController(editor(), OpcUaAdapter.class.getField("requestTimeout")) {

                        @Override
                        protected TextScanResult doCheck(String formatted, int options) {
                            if (Utils.isEmpty(formatted) || Utils.parseInt(formatted, -1) > 0)
                                return TextScanResult.SCAN_OK;
                            return TextScanResult.SCAN_ERROR;
                        }

                    }, tlk().ld(cols() - 1, SWT.FILL, TLK.IGNORE_CONTROL), DialogToolkit.LABEL | SWT.BORDER, "Request timeout[ms]:");
                    tlk().addText(details, new TextController(editor(), OpcUaAdapter.class.getField("maxResponseMessageSize")) {

                        @Override
                        public void setControl(Control control, int function) {
                            super.setControl(control, function);
                            text().setMessage("Default");
                        }

                        @Override
                        protected TextScanResult doCheck(String formatted, int options) {
                            if (Utils.isEmpty(formatted) || Utils.parseInt(formatted, -1) >= 0)
                                return TextScanResult.SCAN_OK;
                            return TextScanResult.SCAN_ERROR;
                        }

                        @Override
                        protected Object convert(Object value) {
                            if (Utils.equals(value, (Integer) 0))
                                return "";
                            return value;
                        }
                    }, tlk().ld(cols() - 1, SWT.FILL, TLK.IGNORE_CONTROL), DialogToolkit.LABEL | SWT.BORDER, "Max response size:");
                    tlk().addNothing(details, tlk().ld(cols(), TLK.GRAB, SWT.DEFAULT));

                    // cert tab
                    Composite cert = tlk().addComposite(tabFolder, null, cols(), null, SWT.NULL, "Certificate", null);
                    final TextController file = (TextController) tlk().addText(cert,
                            new TextController(editor(), OpcUaAdapter.class.getField("certificateFile")), tlk().ld(cols() - 2, SWT.FILL, TLK.IGNORE_CONTROL),
                            SWT.BORDER | TLK.LABEL, "Certificate file [pfx]:");
                    tlk().addButton(cert, new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {

                            String base = null;
                            if (editor().getBaseElement().isBound() && editor().getBaseElement().getContainer().isBound()
                                    && editor().getBaseElement().getContainer().hasCell(MultiPipePort.class)) {
                                MultiPipePort port = (MultiPipePort) editor().getBaseElement().getContainer().getCell();
                                if (!Utils.isEmpty(port.pathBase)) {
                                    base = port.pathBase;
                                    while (base.endsWith(File.separator))
                                        base = base.substring(0, base.length() - 1);
                                }
                            }
                            String path = file.getValueAsString();
                            if (!Utils.isEmpty(base) && path != null && !path.trim().startsWith(File.separator))
                                path = base + File.separator + path;
                            FileDialog dialog = new FileDialog(EclipseUtils.getActiveShell());
                            if (path != null && new File(path).exists())
                                dialog.setFileName(path);
                            else if (base != null)
                                dialog.setFileName(base);
                            path = dialog.open();
                            if (path != null) {
                                if (base != null && path.startsWith(base + File.separator))
                                    path = path.substring((base + File.separator).length());
                                file.setValue(path);
                            }
                            super.execute(id, data);
                        }
                    }, 1, SWT.NULL, "...", null);
                    IController certPassword = tlk().addText(cert, new PasswordController(editor(), OpcUaAdapter.class.getField("certPassword")),
                            tlk().ld(cols() - 1, SWT.FILL, TLK.IGNORE_CONTROL), SWT.BORDER | TLK.LABEL | SWT.PASSWORD, "Password:");
                    tlk().addCombo(cert, new ComboSelectController(editor(), OpcUaAdapter.class.getField("certAlias")) {
                        public void populate() {
                            super.populate();
                            try {
                                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                                if (file.getValueAsString() != null)
                                    keyStore.load(Utils.getInput(file.getValueAsString()), certPassword.getValueAsString() != null
                                            ? PasswordController.decrypt(certPassword.getValueAsString()).toCharArray() : new char[0]);
                                Enumeration<String> alias = keyStore.aliases();
                                while (alias.hasMoreElements()) {
                                    addItem(alias.nextElement());
                                }

                            } catch (Throwable e) {

                            }
                        }

                        @Override
                        public boolean needsUpdate() {
                            return true;
                        }

                        @Override
                        protected void doUpdateExternal() {
                            try {
                                if (this.getValueAsString() != null && !Utils.isEmpty(file.getValueAsString())) {
                                    KeyStore keyStore = KeyStore.getInstance("PKCS12");
                                    char[] password = certPassword.getValueAsString() != null
                                            ? PasswordController.decrypt(certPassword.getValueAsString()).toCharArray() : new char[0];
                                    keyStore.load(Utils.getInput(file.getValueAsString()), password);
                                    Key privateKey = keyStore.getKey(this.getValueAsString(), password);
                                    X509Certificate certificate = (X509Certificate) keyStore.getCertificate(this.getValueAsString());
                                    PublicKey publicKey = certificate.getPublicKey();

                                    String info = "";
                                    info += privateKey instanceof PrivateKey ? "Private key found!<br/>" : "No private key found!<br/>";
                                    info += publicKey != null ? "Public key found!<br/>" : "No public key found!<br/>";
                                    info += certificate != null ? "Certificate found:<br/>" : "No certificate found!<br/>";
                                    if (certificate != null) {
                                        info += MultilineText.toXml(certificate.toString());
                                    }
                                    certInfo.setValue(info);
                                    return;
                                }

                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            certInfo.setValue(null);
                        }
                    }.setNullItem(""), tlk().ld(cols() - 1, SWT.FILL, TLK.IGNORE_CONTROL), SWT.BORDER | TLK.LABEL | SWT.READ_ONLY, "Alias:");

                    certInfo = tlk().addText(cert, new TextController(editor(), null), tlk().ld(cols() - 1, SWT.FILL, TLK.IGNORE_CONTROL, SWT.FILL, 60),
                            TLK.LABEL | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI, "Certificate:");

                    tlk().addNothing(cert, 1);
                    tlk().addNothing(cert, tlk().ld(1, TLK.GRAB, SWT.DEFAULT));

                    // nodes tree
                    tree = (CellTreeController) tlk().addTree(container(), new CellTreeController(editor(), null) {

                        @Override
                        protected void doUpdateHints() {
                            super.doUpdateHints();
                            showEnabled();
                        }

                    }.setCheckField("enabled"), tlk().ld(cols() - 1, SWT.FILL, 450, SWT.FILL, 300), SWT.MULTI | SWT.CHECK, null, null);
                    Composite buttons = tlk().addComposite(container(), null, 1, 1, SWT.NULL, null, null);
                    tlk().addButton(buttons, new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {
                            if (checkConnection(this))
                                server.addCurrentTextToHistory();
                        }
                    }, tlk().ld(1, SWT.FILL, SWT.DEFAULT, SWT.FILL, 65), SWT.PUSH, "Test connection", null);
                    tlk().addButton(buttons, new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {
                            if (syncNodes(this, false))
                                server.addCurrentTextToHistory();
                        }
                    }, tlk().ld(1, SWT.FILL, SWT.DEFAULT, SWT.FILL, 65), SWT.PUSH, "Synchronize nodes (server)", null);
                    tlk().addButton(buttons, new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {
                            if (syncNodes(this, true))
                                server.addCurrentTextToHistory();
                        }
                    }, tlk().ld(1, true, false), SWT.PUSH, "Load nodes (clean)", null);

                    tlk().addButton(buttons, new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {
                            showEnabled();
                        }
                    }, tlk().ld(1, true, false), SWT.PUSH, "Show monitored nodes", null);
                    tlk().addButton(buttons, new ButtonController(editor(), null) {

                        @Override
                        public void execute(String id, Object data) {
                            tree.uncheckAll();
                        }
                    }, tlk().ld(1, true, false), SWT.PUSH, "Disable all nodes", null);

                    tlk().addButton(container(), new CheckController(editor(), AbstractPortAdapterBaseCell.class.getField("insertAsRoot")) {

                        @Override
                        public boolean enabled() {
                            return !(getCell() instanceof IRecordPort && ((IRecordPort) getCell()).isPort());
                        }
                    }, tlk().ld(cols(), SWT.RIGHT, SWT.DEFAULT), SWT.CHECK, "Insert at Root", null);
                } catch (SecurityException e) {
                } catch (NoSuchFieldException e) {
                }
                return true;
            }

            private boolean checkConnection(IController controller) {
                ICell base = controller.getCell();
                if (base instanceof OpcUaAdapter) {
                    final OpcUaAdapter root = (OpcUaAdapter) base;
                    OpcUaClient client = null;
                    try {
                        client = OpcUa.createAndConnect(root);
                        Messages.openInformation("Connection Test", "Connection successful");
                        return true;
                    } catch (Throwable e) {
                        LoggerFactory.getLogger(null).error("Connection failed", e);
                        Messages.openError("Connection Test", "Connection failed:" + e.getLocalizedMessage());
                    } finally {
                        if (client != null)
                            try {
                                OpcUa.disconnect(client);
                            } catch (Exception e) {
                            }
                    }
                }
                return false;
            }

            private boolean syncNodes(IController controller, boolean clean) {
                ICell base = controller.getCell();
                if (base instanceof OpcUaAdapter) {
                    final OpcUaAdapter root = (OpcUaAdapter) base.clone();
                    if (clean)
                        root.removeAllChildren();
                    OpcUaClient client = null;

                    try {

                        client = OpcUa.createAndConnect(root);
                        OpcUa.synchronizedNodes(client, root);

                        List<IElementModifier> modifiers = new ArrayList<IElementModifier>();
                        if (base.hasChildren())
                            modifiers.add(ElementHierarchyModifier.removeAll(base.getElement()));
                        if (root.hasChildren())
                            modifiers.add(ElementHierarchyModifier.add(base.getElement(), Elements.getElements(root.getChildren()), null));
                        editor().apply("Sync Nodes", null, modifiers.toArray(new IElementModifier[modifiers.size()]), true);

                        Messages.openInformation("Synchronisation", "Synchronisation successful.");

                    } catch (Throwable e) {
                        LoggerFactory.getLogger(null).error("Synchronisation", e);
                        Messages.openError("Synchronisation", "Synchronisation failed:" + e.getLocalizedMessage());
                    } finally {
                        if (client != null)
                            try {
                                OpcUa.disconnect(client);
                            } catch (Exception e) {
                            }
                    }
                }
                return false;
            }

            void showEnabled() {
                List<ICell> list = new ArrayList<ICell>();
                for (ICell child : tree.getCell().getTribe(false))
                    if (child.getValueAsBoolean("enabled"))
                        list.add(child);
                tree.makeVisible(list);
            }

        }.add(new NameDescriptionEnableProvider());
        provider.setCellClass(OpcUaAdapter.class);
        return provider;
    }

}
