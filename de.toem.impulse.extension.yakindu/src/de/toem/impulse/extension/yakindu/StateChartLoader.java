package de.toem.impulse.extension.yakindu;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.emf.core.GMFEditingDomainFactory;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.yakindu.sct.domain.extension.DomainRegistry;
import org.yakindu.sct.domain.extension.IDomain;
import org.yakindu.sct.model.sgraph.SGraphPackage;
import org.yakindu.sct.model.sgraph.Statechart;

import com.google.inject.Injector;

import de.toem.basics.core.Utils;
import de.toem.pattern.element.Elements;
import de.toem.pattern.element.IElement;
import de.toem.pattern.element.Link;

public class StateChartLoader {

    private byte[] bytes;
    private URI uri;
    private EList<EObject> contents;

    public static StateChartLoader fromFilePath(String path, byte[] bytes, boolean parse) {
        bytes = bytes != null ? bytes : Utils.readBinaryFromFile(new File(path));
        return (bytes != null) ? new StateChartLoader(bytes, URI.createFileURI(path), parse) : null;
    }

    public static StateChartLoader fromByteArray(byte[] bytes, boolean parse) {
        return (bytes != null) ? new StateChartLoader(bytes, URI.createURI(""), parse) : null;
    }

    public static StateChartLoader fromResource(Link resource, byte[] bytes, boolean parse) {
        try {
            IElement base = Elements.getElement(ResourcesPlugin.getWorkspace().getRoot());
            IElement element = resource != null && base.isBound() ? resource.resolveElement(base) : IElement.NONE;
            URI uri = null;
            if (element.isBound()) {

                if (element.getResource() instanceof IFile) {
                    uri = URI.createPlatformResourceURI(((IFile) element.getResource()).getFullPath().toString(), true);
                }
                if (bytes == null) {
                    InputStream in = element.getResourceData(null);
                    bytes = in != null ? Utils.readBinaryFromInputStream(in) : null;
                }
            }
            if (uri == null)
                uri = URI.createURI("");
            return (bytes != null) ? new StateChartLoader(bytes, uri, parse) : null;

        } catch (Throwable e) {
        }
        return null;
    }

    public StateChartLoader(byte[] bytes, URI uri, boolean parse) {
        this.bytes = bytes;
        this.uri = uri;
        if (parse)
            parse();
    }

    public void parse() {

        // create resource
        String domainID = DomainRegistry.determineDomainID(uri);
        if (Utils.isEmpty(domainID))
            domainID = "org.yakindu.domain.default";
        IDomain domain = DomainRegistry.getDomain(domainID);
        Injector injector = domain.getInjector(IDomain.FEATURE_RESOURCE);
        Resource resource = injector.getInstance(Resource.class);

        // add to the editing domain resource set
        TransactionalEditingDomain editingDomain = GMFEditingDomainFactory.INSTANCE.createEditingDomain();
        editingDomain.getResourceSet().getResources().add(resource);
        TransactionalEditingDomain.Registry.INSTANCE.getEditingDomain("org.yakindu.sct.domain").getResourceSet();

        // load resource
//        Utils.log(uri);
        resource.setURI(uri/* URI.createURI("") */);
        InputStream in = new ByteArrayInputStream(bytes);
        try {
            resource.load(in, Collections.EMPTY_MAP);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        // get objects
        contents = resource.getContents();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public boolean isEmpty() {
        return bytes == null || contents == null || contents.isEmpty();
    }

    public EList<EObject> getContents() {
        return contents;
    }

    public Statechart getStateChart() {
        return (Statechart) EcoreUtil.getObjectByType(contents, SGraphPackage.Literals.STATECHART);
    }

    public Diagram getDiagram() {
        return getDiagram(null);
    }

    public Diagram getDiagram(String name) {
        int idx = Utils.parseInt(name, -1);
        if (contents != null)
            for (EObject c : contents)
                if (c instanceof Diagram) {
                    if (name == null || name.equals(((Diagram) c).getName()) || idx == 0)
                        return (Diagram) c;
                    idx--;
                }
        return null;
    }

    public List<Diagram> getDiagrams() {
        List<Diagram> diagrams = new ArrayList<>();
        if (contents != null)
            for (EObject c : contents)
                if (c instanceof Diagram)
                    diagrams.add((Diagram) c);
        return diagrams;
    }

    public List<String> getDiagramNames() {
        List<String> diagrams = new ArrayList<>();
        int idx = 0;
        if (contents != null)
            for (EObject c : contents)
                if (c instanceof Diagram) {
                    if (!Utils.isEmpty(((Diagram) c).getName()))
                        diagrams.add(((Diagram) c).getName());
                    else
                        diagrams.add(String.valueOf(idx));
                    idx++;
                }
        return diagrams;
    }
}
