package de.toem.impulse.extension.opc.ua;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.config.OpcUaClientConfig;
import com.digitalpetri.opcua.sdk.client.api.identity.AnonymousProvider;
import com.digitalpetri.opcua.sdk.client.api.identity.IdentityProvider;
import com.digitalpetri.opcua.sdk.client.api.identity.UsernameProvider;
import com.digitalpetri.opcua.stack.client.UaTcpStackClient;
import com.digitalpetri.opcua.stack.core.Identifiers;
import com.digitalpetri.opcua.stack.core.Stack;
import com.digitalpetri.opcua.stack.core.security.SecurityPolicy;
import com.digitalpetri.opcua.stack.core.types.builtin.ExpandedNodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.enumerated.BrowseDirection;
import com.digitalpetri.opcua.stack.core.types.enumerated.MessageSecurityMode;
import com.digitalpetri.opcua.stack.core.types.enumerated.NodeClass;
import com.digitalpetri.opcua.stack.core.types.structured.BrowseDescription;
import com.digitalpetri.opcua.stack.core.types.structured.BrowseResult;
import com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription;
import com.digitalpetri.opcua.stack.core.types.structured.ReferenceDescription;

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

		// Certificate
		KeyPair clientKeyPair = null;
		X509Certificate certificate = null;
		try {
			if (adapter.certificateFile != null && adapter.certAlias != null) {
				KeyStore keyStore = KeyStore.getInstance("PKCS12");
				char[] password = adapter.certPassword != null ? PasswordController.decrypt(adapter.certPassword).toCharArray() : new char[0];
				keyStore.load(Utils.getInput(adapter.certificateFile), password);
				Key privateKey = keyStore.getKey(adapter.certAlias, password);
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
		final EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints(adapter.server).get();
		final EndpointDescription endpoint = Arrays.stream(endpoints)
				.filter(e -> e.getSecurityPolicyUri().equals(securityPolicy.getSecurityPolicyUri()) && e.getSecurityMode().equals(securityMode)).findFirst()
				.orElseThrow(() -> new Exception("No desired endpoints returned"));

		final IdentityProvider identityProvider;
		if (adapter.identification == OpcUaAdapter.IDENT_PASSWORD)
			identityProvider = new UsernameProvider(adapter.user,PasswordController.decrypt(adapter.password));
		else 
			identityProvider = new AnonymousProvider();
		final Logger logger = LoggerFactory.getLogger(null);
		logger.info("Using endpoint: {} [{}]", endpoint.getEndpointUrl(), securityPolicy);

		OpcUaClientConfig config = OpcUaClientConfig.builder().setApplicationName(LocalizedText.english(adapter.applicationName))
				.setApplicationUri(adapter.applicationUri).setProductUri(adapter.productUri).setCertificate(certificate).setKeyPair(clientKeyPair)
				.setEndpoint(endpoint).setIdentityProvider(identityProvider).setRequestTimeout(uint(adapter.requestTimeout))
				.setSessionTimeout(uint(adapter.sessionTimeout)).setMaxResponseMessageSize(uint(adapter.maxResponseMessageSize)).build();

		OpcUaClient client = new OpcUaClient(config);
		client.connect().get();

		// logging
		if (adapter.logToConsole) {
			loggingClients.add(client);
			Logger.logToConsole(true);
		}
		runningClients.add(client);
		return client;
	}

	static public boolean synchronizedNodes(OpcUaClient client, ICell base) {
		return new AbstractSynchronizer<ReferenceDescription, OpcUaNode>(true, true) {

			final Logger logger = LoggerFactory.getLogger(null);

			@Override
			protected boolean matches(ReferenceDescription sourceChild, OpcUaNode targetChild, int index) {
				return Utils.equals(sourceChild.getNodeId().local().get().toParseableString(), targetChild.nodeId);
			}

			@Override
			protected Iterable<OpcUaNode> getTargetChildren(Object targetObject) {
				return targetObject instanceof ICell ? (List) ((ICell) targetObject).getChildren() : Collections.EMPTY_LIST;
			}

			@Override
			protected Iterable<ReferenceDescription> getSourceChildren(Object sourceObject) {
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
							UInteger.valueOf(NodeClass.Object.getValue() | NodeClass.Variable.getValue() | NodeClass.View.getValue()), UInteger.valueOf(-1));
					CompletableFuture<BrowseResult> result = client.browse(bd);

					if (result.get().getStatusCode().isGood())
						for (ReferenceDescription ref : result.get().getReferences()) {
							if (ref.getNodeId().isNotNull() && ref.getNodeId().isLocal() && !nodes.contains(ref.getNodeId())) {
								list.add(ref);
								nodes.add(ref.getNodeId());
//								Utils.log(ref.getNodeId().toString(), ref.getBrowseName(), ref.getReferenceTypeId());
							}
						}
					// while(result.get().getContinuationPoint() != null){
					// result = client.browseNext(releaseContinuationPoints,
					// continuationPoints);
					// if (result.get().getStatusCode().isGood())
					// for(ReferenceDescription
					// ref:result.get().getReferences()){
					// list.add(ref);
					// }
					// }
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
}
