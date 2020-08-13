package de.toem.impulse.extension.opc.ua;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import java.lang.reflect.Array;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.Stack;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.serialization.UaSerializable;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.XmlElement;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.controller.base.PasswordController;
import de.toem.impulse.extension.opc.Logger;
import de.toem.impulse.extension.opc.LoggerFactory;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.synchronization.AbstractSynchronizer;

public class OpcUa {

    static public final AtomicLong clientHandles = new AtomicLong(1L);
    static final List<OpcUaClient> runningClients = new ArrayList<OpcUaClient>();
    static final List<OpcUaClient> loggingClients = new ArrayList<OpcUaClient>();

    static synchronized public OpcUaClient createAndConnect(OpcUaAdapter adapter) throws Exception {

        final Logger logger = LoggerFactory.getLogger();

        // Certificate
        KeyPair clientKeyPair = null;
        X509Certificate certificate = null;
        try {
            if (adapter.certificateFile != null && adapter.certAlias != null) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                char[] password = adapter.certPassword != null ? PasswordController.decrypt(adapter.certPassword).toCharArray() : new char[0];
                keyStore.load(Utils.getInput(adapter.certificateFile), password);
                Key privateKey = keyStore.getKey(adapter.certAlias, password);
                if (privateKey instanceof PrivateKey)
                    certificate = (X509Certificate) keyStore.getCertificate(adapter.certAlias);
                PublicKey publicKey = certificate.getPublicKey();
                if (publicKey != null && privateKey instanceof PrivateKey) {
                    clientKeyPair = new KeyPair(publicKey, (PrivateKey) privateKey);
                }
            }
        } catch (Throwable e) {
        }

        // Security
        final MessageSecurityMode securityMode;
        switch (adapter.securityMode) {
        case OpcUaAdapter.SECURITY_SIGN:
            securityMode = MessageSecurityMode.Sign;
            break;
        case OpcUaAdapter.SECURITY_SIGN_ENCRYPT:
            securityMode = MessageSecurityMode.SignAndEncrypt;
            break;
        default:
            securityMode = MessageSecurityMode.None;
            break;
        }
        final SecurityPolicy securityPolicy;
        if (securityMode != MessageSecurityMode.None) {
            switch (adapter.securityPolicy) {
            case OpcUaAdapter.POLICY_Basic128Rsa15:
                securityPolicy = SecurityPolicy.Basic128Rsa15;
                break;
            case OpcUaAdapter.POLICY_Basic256:
                securityPolicy = SecurityPolicy.Basic256;
                break;
            case OpcUaAdapter.POLICY_Basic256Sha256:
                securityPolicy = SecurityPolicy.Basic256Sha256;
                break;
            default:
                securityPolicy = SecurityPolicy.None;
                break;
            }
        } else
            securityPolicy = SecurityPolicy.None;

        // Endpoint
        List<EndpointDescription> endpoints = null;                   
        try {
            endpoints = DiscoveryClient.getEndpoints(adapter.server).get();
        } catch (Throwable ex) {

            // try the explicit discovery endpoint as well
            String discoveryUrl = adapter.server;

            if (!discoveryUrl.endsWith("/")) {
                discoveryUrl += "/";
            }
            discoveryUrl += "discovery";

            logger.info("Trying explicit discovery URL: {}", discoveryUrl);
            endpoints = DiscoveryClient.getEndpoints(discoveryUrl).get();
        }
        
        // determine end point
        EndpointDescription endpoint = null;

        // 2 step approuch from Shigeru
        // 1st step - on the same server
        for (EndpointDescription e : endpoints) {
            if (e.getEndpointUrl().equals(adapter.server) && e.getSecurityPolicyUri().equals(securityPolicy.getUri())
                    && e.getSecurityMode().equals(securityMode)) {
                endpoint = e;
                break;
            }
        }
        // 2nd step - DEFAULT           
        if (endpoint == null)
            endpoint = endpoints.stream().filter(e -> e.getSecurityPolicyUri().equals(securityPolicy.getUri())).filter(e -> e.getSecurityMode() == securityMode)
                    .findFirst().orElseThrow(() -> new Exception("No desired endpoints returned"));

        // identification
        final IdentityProvider identityProvider;
        if (adapter.identification == OpcUaAdapter.IDENT_PASSWORD)
            identityProvider = new UsernameProvider(adapter.user, PasswordController.decrypt(adapter.password));
        else
            identityProvider = new AnonymousProvider();

        logger.info("Using endpoint: {} [{}]", endpoint.getEndpointUrl(), securityPolicy);

        OpcUaClientConfig config = OpcUaClientConfig.builder().setApplicationName(LocalizedText.english(adapter.applicationName))
                .setApplicationUri(adapter.applicationUri).setProductUri(adapter.productUri).setCertificate(certificate).setKeyPair(clientKeyPair)
                .setEndpoint(endpoint).setIdentityProvider(identityProvider).setRequestTimeout(uint(adapter.requestTimeout))
                .setSessionTimeout(uint(adapter.sessionTimeout)).setMaxResponseMessageSize(uint(adapter.maxResponseMessageSize)).build();

        OpcUaClient client = OpcUaClient.create(config);
        client.connect().get();

        // logging
        if (adapter.logToConsole) {
            loggingClients.add(client);
            Logger.logToConsole(adapter.logToConsole);
        }
        runningClients.add(client);
        return client;
    }

    static public boolean synchronizedNodes(OpcUaClient client, ICell base) {
        return new AbstractSynchronizer<ReferenceDescription, OpcUaNode>(true, true) {

            final Logger logger = LoggerFactory.getLogger();

            @Override
            protected boolean matches(ReferenceDescription sourceChild, OpcUaNode targetChild, int index) {
                return Utils.equals(sourceChild.getNodeId().local().get().toParseableString(), targetChild.nodeId);
            }

            @Override
            protected Iterable<OpcUaNode> getTargetChildren(Object sourceObject, Object targetObject) {
                return targetObject instanceof ICell ? (List) ((ICell) targetObject).getChildren() : Collections.EMPTY_LIST;
            }

            @Override
            protected Iterable<ReferenceDescription> getSourceChildren(Object sourceObject, Object targetObject) {
                List<ReferenceDescription> list = new ArrayList<ReferenceDescription>();
                Set<ExpandedNodeId> nodes = new HashSet<ExpandedNodeId>();
                try {
                    NodeId root;
                    if (sourceObject == client)
                        root = Identifiers.RootFolder;
                    else {
                        root = ((ReferenceDescription) sourceObject).getNodeId().local().get();
                        if (((ReferenceDescription) sourceObject).getNodeClass() == NodeClass.Variable)
                            return list;
                    }

                    BrowseDescription bd = new BrowseDescription(root, BrowseDirection.Forward, Identifiers.HierarchicalReferences, true,
                            UInteger.valueOf(
                                    NodeClass.Object.getValue() | NodeClass.Variable.getValue() | NodeClass.View.getValue()/*
                                                                                                                            * |NodeClass.Method.getValue()
                                                                                                                            */),
                            UInteger.valueOf(BrowseResultMask.All.getValue()));

                    // read first result
                    BrowseResult result = client.browse(bd).get();
                    if (result.getStatusCode().isGood() && result.getReferences() != null) {

                        // read references
                        for (ReferenceDescription ref : result.getReferences()) {
                            if (ref.getNodeId().isNotNull() && ref.getNodeId().isLocal() && !nodes.contains(ref.getNodeId())) {
                                list.add(ref);
                                nodes.add(ref.getNodeId());
                            }
                        }

                        // check next results
                        ByteString continuationPoint = result.getContinuationPoint();
                        while (continuationPoint != null && continuationPoint.isNotNull()) {

                            result = client.browseNext(false, continuationPoint).get();

                            if (result.getStatusCode().isGood() && result.getReferences() != null) {

                                // read references
                                for (ReferenceDescription ref : result.getReferences()) {
                                    if (ref.getNodeId().isNotNull() && ref.getNodeId().isLocal() && !nodes.contains(ref.getNodeId())) {
                                        list.add(ref);
                                        nodes.add(ref.getNodeId());
                                    }
                                }
                            }
                            continuationPoint = result.getContinuationPoint();
                        }
                    }
                }

                catch (InterruptedException | ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return list;
            }

            @Override
            protected OpcUaNode add(ReferenceDescription sourceChild, Object targetObject, int index) {
                if (targetObject instanceof ICell) {
                    OpcUaNode newNode = new OpcUaNode();
                    newNode.nodeId = sourceChild.getNodeId().local().get().toParseableString();
                    newNode.description = sourceChild.getBrowseName() != null ? sourceChild.getBrowseName().toParseableString() : null;
                    ((ICell) targetObject).addChild(newNode);
                    logger.info("Added node: {}", newNode);
                    return newNode;
                }
                return null;
            }

            @Override
            protected boolean remove(OpcUaNode targetChild) {
                if (targetChild instanceof ICell && targetChild.getParent() != null) {
                    targetChild.getParent().removeChild(targetChild);
                    logger.info("Removed node: {}", targetChild);
                    return true;
                }
                return false;
            }

            @Override
            protected boolean sync(ReferenceDescription sourceChild, OpcUaNode targetChild, int index) {
                targetChild.setName(sourceChild.getDisplayName().getText());
                targetChild.nodeClass = sourceChild.getNodeClass().toString();
                logger.info("Updated node: {}", targetChild);
                return true;
            }

        }.synchronize(client, base);

    }

    static public String[][] readAttributes(OpcUaClient client, OpcUaAdapter base, String nodeId, Set<AttributeId> attributes) {

        try {
            final OpcUaAdapter root = (OpcUaAdapter) base.clone();
            List<String[]> value = new ArrayList<String[]>();
            client = OpcUa.createAndConnect(root);
            List<ReadValueId> readIds = new ArrayList<ReadValueId>();
            for (AttributeId a : attributes) {
                // Utils.log(a);
                value.add(new String[] { String.valueOf(a), null });
                readIds.add(new ReadValueId(NodeId.parse(nodeId), a.uid(), null, null));
            }
            ReadResponse n = client.read(0, TimestampsToReturn.Both, readIds).get();
            int idx = 0;
            for (DataValue r : n.getResults()) {
                // Utils.log(r.getValue());
                value.get(idx++)[1] = OpcUa.valToString(r.getValue().getValue(), 128);

            }

            return value.toArray(new String[value.size()][]);

        } catch (Throwable e) {
        }
        return null;

    }

    static synchronized public void disconnect(OpcUaClient client) throws Exception {

        // disconnect
        runningClients.remove(client);
        client.disconnect().get();

        if (runningClients.isEmpty())
            Stack.releaseSharedResources();

        // logging
        if (loggingClients.contains(client))
            loggingClients.remove(client);
        if (loggingClients.isEmpty())
            Logger.logToConsole(false);
    }

    static public String valToString(Object value) {
        return valToString(value, -1);
    }

    static public String valToString(Object value, int maxSize) {
        if (value != null && value.getClass().isArray()) {
            String combined = "";
            for (int n = 0; n < Array.getLength(value); n++) {
                combined += (combined.isEmpty() ? "" : "; ");
                String val = valToString(Array.get(value, n));
                if (val != null) {
                    combined += val;
                }
            }
            return combined;
        } else if (value instanceof DateTime)
            value = ((DateTime) value).getJavaDate().toString();
        else if (value instanceof ByteString)
            value = ((ByteString) value).toString();
        else if (value instanceof LocalizedText)
            value = ((LocalizedText) value).getText();
        else if (value instanceof QualifiedName)
            value = ((QualifiedName) value).toParseableString();
        else if (value instanceof NodeId)
            value = ((NodeId) value).toParseableString();
        else if (value instanceof ExpandedNodeId)
            value = ((ExpandedNodeId) value).toParseableString();
        else if (value instanceof UUID)
            value = ((UUID) value).toString();
        else if (value instanceof XmlElement)
            value = ((XmlElement) value).getFragment();
        else if (value instanceof StatusCode)
            value = ((StatusCode) value).isGood() ? "Good" : ((StatusCode) value).isBad() ? "Bad" : "Uncertain";
        else if (value instanceof Boolean)
            value = ((Boolean) value).toString();
        else if (value instanceof UaSerializable) {
            return "{"+value.toString()+ "}";
        }
        String strValue = String.valueOf(value);
        if (strValue.length() > maxSize && maxSize > 0)
            strValue = strValue.substring(0, maxSize);

        return strValue;
    }

}
