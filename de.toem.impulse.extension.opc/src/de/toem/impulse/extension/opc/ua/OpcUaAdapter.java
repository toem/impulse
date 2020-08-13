//  ------------------------------------------------------------------
//  Copyright (c) 2012-2019 Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.opc.ua;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.serialization.UaStructure;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.ULong;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.enumerated.DataChangeTrigger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.DeadbandType;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.ContentFilter;
import org.eclipse.milo.opcua.stack.core.types.structured.DataChangeFilter;
import org.eclipse.milo.opcua.stack.core.types.structured.EventFilter;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.SimpleAttributeOperand;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.tlk.ITlkPainter;
import de.toem.impulse.cells.ports.AbstractSyncablePortAdapterCell;
import de.toem.impulse.cells.ports.IPortAdapter;
import de.toem.impulse.cells.ports.IPortProgress;
import de.toem.impulse.cells.ports.IPortProviderFactory;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.MultiAdapterPort;
import de.toem.impulse.cells.preferences.ImpulsePorts;
import de.toem.impulse.cells.record.Scope;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.domain.DateBase;
import de.toem.impulse.domain.IDomainBase;
import de.toem.impulse.domain.TimeBase;
import de.toem.impulse.extension.opc.ImpulseOpcExtension;
import de.toem.impulse.paint.IActiveValueProvider;
import de.toem.impulse.samples.IBinarySamplesWriter;
import de.toem.impulse.samples.IEventSamplesWriter;
import de.toem.impulse.samples.IFloatSamplesWriter;
import de.toem.impulse.samples.IIntegerSamplesWriter;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.samples.ISamplesWriter;
import de.toem.impulse.samples.IStructSamplesWriter;
import de.toem.impulse.samples.ITextSamplesWriter;
import de.toem.impulse.scripting.DefaultScriptContextProvider;
import de.toem.impulse.scripting.Scripting;
import de.toem.impulse.serializer.AbstractPortAdapterRecordReader;
import de.toem.impulse.serializer.AbstractSingleDomainRecordReader;
import de.toem.impulse.serializer.IRecordReader;
import de.toem.impulse.serializer.ParseException;
import de.toem.impulse.values.StructMember;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.element.FieldAnnotation;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.ICover;
import de.toem.pattern.threading.Actives;
import de.toem.pattern.threading.IExecutable;
import de.toem.pattern.threading.IProgress;

@CellAnnotation(type = OpcUaAdapter.TYPE, dynamicChildOf = { MultiAdapterPort.TYPE, ImpulsePorts.TYPE })
public class OpcUaAdapter extends AbstractSyncablePortAdapterCell implements IRecordPort, IPortAdapter {
    public static final String TYPE = "port.record.opc.ua";

    public String server = "opc.tcp://opcua.demo-this.com:51210/UA/SampleServer";
    public int publishRate = 1000;
    public boolean logToConsole;
    public static final int TRIGGER_DEFAULT = 0;
    public static final int TRIGGER_STATUS = 1;
    public static final int TRIGGER_STATUS_VALUE = 2;
    public static final int TRIGGER_STATUS_VALUE_TIMESTAMP = 3;
    public static final String[] TRIGGER_OPTIONS = { "Un-set", "Status", "Status/Value", "Status/Value/Timestamp" };
    public int trigger = TRIGGER_DEFAULT;

    // identification
    public static final int IDENT_ANONYMOUS = 0;
    public static final int IDENT_PASSWORD = 1;
    public static final String[] IDENT_OPTIONS = { "Anonymous", "Password" };
    public int identification = IDENT_ANONYMOUS;
    public String user;
    public String password;
    // security certificate
    // public String identityFile;
    // public String certPassword = "";
    // @FieldAnnotation(affectedBy = { "certificateFile", "certPassword" })
    // public String certAlias = "myKey";

    // security
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_SIGN = 1;
    public static final int SECURITY_SIGN_ENCRYPT = 2;
    public static final String[] SECURITY_OPTIONS = { "None", "Sign", "Sign and Encrypt" };
    public int securityMode = SECURITY_NONE;
    public static final int POLICY_Basic128Rsa15 = 0;
    public static final int POLICY_Basic256 = 1;
    public static final int POLICY_Basic256Sha256 = 2;
    public static final int POLICY_NONE = 3;
    public static final String[] POLICY_OPTIONS = { "Basic128Rsa15", "Basic256", "Basic256Sha256", "None" };
    public int securityPolicy = POLICY_Basic128Rsa15;

    // security certificate
    public String certificateFile;
    public String certPassword = "";
    @FieldAnnotation(affectedBy = { "certificateFile", "certPassword" })
    public String certAlias = "myKey";

    // settings
    public String applicationName = "Unknown Client";
    public String applicationUri = "urn:unknown:client";
    public String productUri = "urn:unknown:product";
    public int sessionTimeout = 120000;
    public int maxResponseMessageSize = 0;
    public int requestTimeout = 10000;
    public static final int DOMAIN_REL_TIME = 0;
    public static final int DOMAIN_DATE_TIME = 1;
    public static final int DOMAIN_ABS_TIME = 2;
    public static final int DOMAIN_ABS_TIME_MS = 3;
    public static final String[] DOMAIN_OPTIONS = { "Relative Time (UTC 100ns)", "Date/Time", "Abs Time (s)", "Abs Time (ms)" };
    public int domainBase = DOMAIN_REL_TIME;

    // script
    public boolean enableStimulation;
    public String stimulationScript;
    public String stimulationScriptLanguage;

    @Override
    public void provideToScriptContext(IScriptContextInterface context) {
        super.provideToScriptContext(context);
        if (Utils.equals(context.getContextId(), "stimulationScript")) {

            DefaultScriptContextProvider.provideDefaultScriptContext(context, true, false, true, false, false, true);

            context.addSymbol("client", false, OpcUaClient.class);
            context.setLoader(ImpulseOpcExtension.class.getClassLoader());

            context.setScript(stimulationScript, stimulationScriptLanguage);
        }
        Object n = Thread.class;
    }

    @Override
    public int getNature() {
        return IRecordPort.NATURE_CONNECT | IRecordPort.NATURE_FLOATING | IRecordPort.NATURE_REFRESH_CONTINUOUS | IRecordPort.NATURE_CURRENT_VALUE;
    }

    class OpcInput extends AbstractPortAdapterRecordReader implements Closeable, IPortProviderFactory {

        long started;
        long current;
        long currentAt;
        boolean closed;
        int changed;
        int domainBase;

        class OpcUaSignal {
            OpcUaNode node;
            Signal signal;
            ISamplesWriter writer;
            StructMember[] members;
            IActiveValueProvider painter;
            Object value;
        }

        Map<UInteger, OpcUaSignal> signals = new HashMap<UInteger, OpcUaSignal>();
        IPortProgress progress;
        OpcUaClient client;

        @Override
        protected void process(IPortProgress progress) {

            // progress
            if (progress == null)
                return;
            this.progress = progress;

            this.domainBase = OpcUaAdapter.this.domainBase;

            try {

                // Init the record and signals
                IDomainBase base = TimeBase.ns100;
                if (domainBase == DOMAIN_DATE_TIME)
                    base = DateBase.dateTime;
                else if (domainBase == DOMAIN_ABS_TIME)
                    base = DateBase.time;
                else if (domainBase == DOMAIN_ABS_TIME_MS)
                    base = DateBase.timeMs;
                initRecord(server, base);

                // init opc
                client = OpcUa.createAndConnect(OpcUaAdapter.this);

                // create subscription
                UaSubscription subscription = client.getSubscriptionManager().createSubscription(publishRate).get();
                List<MonitoredItemCreateRequest> requests = new ArrayList<MonitoredItemCreateRequest>();

                // Iterate over all enabled nodes
                for (ICell cell : OpcUaAdapter.this.getTribe(false))
                    if (cell instanceof OpcUaNode && ((OpcUaNode) cell).enabled) {

                        // node
                        OpcUaNode node = (OpcUaNode) cell;

                        // handle
                        UInteger clientHandle = subscription.nextClientHandle();

                        MonitoredItemCreateRequest request = null;
                        List<StructMember> members = null;

                        // variable
                        if (node.isVariable()) {

                            // readValueId
                            ReadValueId readValueId = new ReadValueId(NodeId.parse(node.nodeId), AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);

                            // trigger
                            int t = node.trigger > 0 ? node.trigger : trigger;
                            ExtensionObject et = null;
                            if (t == TRIGGER_STATUS)
                                et = ExtensionObject.encode(client.getSerializationContext(),
                                        new DataChangeFilter(DataChangeTrigger.Status, Unsigned.uint(DeadbandType.None.getValue()), 0.0));
                            else if (t == TRIGGER_STATUS_VALUE)
                                et = ExtensionObject.encode(client.getSerializationContext(),
                                        new DataChangeFilter(DataChangeTrigger.StatusValue, Unsigned.uint(DeadbandType.None.getValue()), 0.0));
                            else if (t == TRIGGER_STATUS_VALUE_TIMESTAMP)
                                et = ExtensionObject.encode(client.getSerializationContext(),
                                        new DataChangeFilter(DataChangeTrigger.StatusValueTimestamp, Unsigned.uint(DeadbandType.None.getValue()), 0.0));

                            // monitoring parameters
                            MonitoringParameters parameters = new MonitoringParameters(clientHandle,
                                    (node.sampleRate > 0) ? (double) node.sampleRate : (double) publishRate, et, uint(node.queueSize > 0 ? node.queueSize : 10),
                                    true);

                            // request
                            request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);

                        } else if (node.isObject()) {

                            // readValueId
                            ReadValueId readValueId = new ReadValueId(NodeId.parse(node.nodeId), AttributeId.EventNotifier.uid(), null,
                                    QualifiedName.NULL_VALUE);

                            // standard attributes
                            List<SimpleAttributeOperand> select = new ArrayList<SimpleAttributeOperand>();
                            select.add(new SimpleAttributeOperand(null, new QualifiedName[] { new QualifiedName(0, "Time") }, AttributeId.Value.uid(), null));
                            select.add(
                                    new SimpleAttributeOperand(null, new QualifiedName[] { new QualifiedName(0, "Message") }, AttributeId.Value.uid(), null));
                            select.add(new SimpleAttributeOperand(null, new QualifiedName[] { new QualifiedName(0, "SourceName") }, AttributeId.Value.uid(),
                                    null));
                            select.add(
                                    new SimpleAttributeOperand(null, new QualifiedName[] { new QualifiedName(0, "Severity") }, AttributeId.Value.uid(), null));
                            select.add(
                                    new SimpleAttributeOperand(null, new QualifiedName[] { new QualifiedName(0, "EventId") }, AttributeId.Value.uid(), null));
                            select.add(
                                    new SimpleAttributeOperand(null, new QualifiedName[] { new QualifiedName(0, "EventType") }, AttributeId.Value.uid(), null));
                            // additional attributes
                            if (!Utils.isEmpty(node.additionalAttributes))
                                for (String attr : node.additionalAttributes.trim().split("\\,")) {
                                    if (!Utils.isEmpty(attr.trim())) {
                                        select.add(new SimpleAttributeOperand(null, new QualifiedName[] { new QualifiedName(0, attr.trim()) },
                                                AttributeId.Value.uid(), null));
                                    }
                                }

                            // filter
                            EventFilter filter = new EventFilter(select.toArray(new SimpleAttributeOperand[select.size()]), new ContentFilter(null));
                            ExtensionObject eo = ExtensionObject.encode(client.getSerializationContext(), filter);

                            // parameters
                            MonitoringParameters parameters = new MonitoringParameters(clientHandle, 0.0, eo, uint(10), true);

                            // request
                            request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);

                            // create members
                            members = new ArrayList<StructMember>();
                            members.add(new StructMember("Message", StructMember.STRUCT_TYPE_TEXT, null, ISample.FORMAT_DEFAULT));
                            members.add(new StructMember("SourceName", StructMember.STRUCT_TYPE_LOCAL_ENUM, null, ISample.FORMAT_DEFAULT));
                            members.add(new StructMember("Severity", StructMember.STRUCT_TYPE_INTEGER, null, ISample.FORMAT_DEFAULT));
                            members.add(new StructMember("EventId", StructMember.STRUCT_TYPE_LOCAL_ENUM, null, ISample.FORMAT_DEFAULT));
                            members.add(new StructMember("EventType", StructMember.STRUCT_TYPE_LOCAL_ENUM, null, ISample.FORMAT_DEFAULT));
                            if (!Utils.isEmpty(node.additionalAttributes))
                                for (String attr : node.additionalAttributes.trim().split("\\,")) {
                                    if (!Utils.isEmpty(attr.trim())) {
                                        members.add(new StructMember(attr.trim(), StructMember.STRUCT_TYPE_UNKNOWN, null, ISample.FORMAT_DEFAULT));
                                    }
                                }
                        }
                        // got a request
                        if (request != null) {

                            // add requests
                            requests.add(request);

                            // create signal
                            OpcUaSignal signal = new OpcUaSignal();
                            signal.node = node;
                            signal.members = members != null ? members.toArray(new StructMember[members.size()]) : null;
                            signals.put(clientHandle, signal);
                        }
                    }

                // Consumer
                BiConsumer<UaMonitoredItem, Integer> onItemCreated = (item, id) -> {
                    OpcUaSignal signal = signals.get(item.getClientHandle());
                    if (signal != null) {
                        if (signal.node.isVariable())
                            item.setValueConsumer(v -> {

                                process(signal, v);
                            });
                        else if (signal.node.isObject())
                            item.setEventConsumer(v -> {

                                process(signal, v);
                            });
                    }
                };

                // create monitored items
                List<UaMonitoredItem> items = subscription.createMonitoredItems(TimestampsToReturn.Both, requests, onItemCreated).get();

                changed = CHANGED_RECORD;

                // stimulation
                final Thread[] simulationThread = new Thread[1];
                if (enableStimulation)
                    Actives.run(new IExecutable() {

                        @Override
                        public void execute(IProgress p) {
                            simulationThread[0] = Thread.currentThread();
                            Scripting scripting = new Scripting(OpcUaAdapter.this, "stimulationScript") {

                                @Override
                                public void init() {
                                    setSymbol("client", OpcInput.this.client);
                                    // for (String i : imports)
                                    // addImport(i, null);
                                }

                                @Override
                                protected Object eval(ScriptEngine engine, boolean init, String script) throws ScriptException {
                                    return script != null ? engine.eval(removePsoidoCode(script)) : null;
                                }
                            };
                            scripting.run(OpcInput.this.progress);
                        }
                    });

                // wait for streaming mode
                synchronized (progress) {
                    while (!progress.isStreaming() && !progress.isCanceled())
                        try {
                            progress.wait(100);
                        } catch (Throwable e) {
                        }
                }
                
                if (!progress.isCanceled()) {

                    // open
                    VariableNode node = client.getAddressSpace().createVariableNode(Identifiers.Server_ServerStatus_CurrentTime);
                    DataValue value = node.readValue().get();
                    Object time = value.getValue();
                    if (!(time instanceof DateTime))
                        time = value.getServerTime();
                    if (time instanceof DateTime) {
                        if (domainBase == DOMAIN_REL_TIME)
                            started = ((DateTime) time).getUtcTime();
                        else
                            started = ((DateTime) time).getJavaTime();

                        if (domainBase != DOMAIN_REL_TIME) {
                            open(started);
                            current = started;
                        } else {
                            open(0);
                            current = 0;
                        }
                        currentAt = Utils.millies();
                        changed = changed < CHANGED_RECORD ? CHANGED_RECORD : changed;
                    } else
                        throw new ParseException("Could not open - no server time received");

                    // wait for the end
                    synchronized (progress) {
                        while (!closed && (progress == null || !progress.isCanceled())) {

                            progress.wait(100);
                            synchronized (this) {
                                changed = changed < CHANGED_CURRENT ? CHANGED_CURRENT : changed;
                            }
                        }
                    }
                }
                subscription.deleteMonitoredItems(items);
            } catch (

            Throwable e) {
                AbstractSingleDomainRecordReader.addParseErrorMessage(this.id, e, this.base);
            } finally {
                if (client != null)
                    try {
                        OpcUa.disconnect(client);
                    } catch (Exception e) {
                    }

                synchronized (this) {
                    close(current + 1);
                    changed = CHANGED_NONE;
                }
                progress = null;

            }
        }

        synchronized private void process(OpcUaSignal opcUaSignal, Variant[] v) {
            try {

                // create signal
                if (opcUaSignal.signal == null && v != null && v != null) {
                    String path = opcUaSignal.node.getPath(OpcUaAdapter.this);
                    String[] splitted = path.split("\\\\");
                    ICell scope = this.getBase();
                    // scopes
                    for (int n = 0; n < splitted.length; n++) {
                        ICell child = scope.getChildByName(splitted[n], Scope.class);
                        if (child == null) {
                            child = addScope(scope, splitted[n]);
                        }
                        scope = child;
                    }

                    // Name
                    String name = splitted[splitted.length - 1] + "Events";
                    // type
                    SignalType type = SignalType.Struct;
                    SignalDescriptor descriptor = SignalDescriptor.DEFAULT;

                    // signal
                    opcUaSignal.signal = addSignal(scope, name, null, ProcessType.Discrete, type, descriptor);
                    opcUaSignal.writer = getWriter(opcUaSignal.signal);
                    changed = changed < CHANGED_RECORD ? CHANGED_RECORD : changed;
                }

                // value
                try {
                    opcUaSignal.members[0].setValue(v[1].getValue() instanceof LocalizedText ? ((LocalizedText) v[1].getValue()).getText() : null);
                    opcUaSignal.members[1].setValue(v[2].getValue() instanceof String ? v[2].getValue() : null);
                    opcUaSignal.members[2].setValue(v[3].getValue() instanceof Number ? ((Number) v[3].getValue()).intValue() : -1);
                    for (int n = 4; n < v.length; n++) {
                        if (v[n].getValue() instanceof Float || v[n].getValue() instanceof Double) {
                            opcUaSignal.members[n - 1].adjustType(StructMember.STRUCT_TYPE_FLOAT);
                            opcUaSignal.members[n - 1].setValue(v[n].getValue());
                        } else if (v[n].getValue() instanceof Number) {
                            opcUaSignal.members[n - 1].adjustType(StructMember.STRUCT_TYPE_INTEGER);
                            opcUaSignal.members[n - 1].setValue(((Number) v[n].getValue()).longValue());
                        } else if (v[n].getValue() instanceof Boolean) {
                            opcUaSignal.members[n - 1].adjustType(StructMember.STRUCT_TYPE_LOCAL_ENUM);
                            opcUaSignal.members[n - 1].setValue(OpcUa.valToString(v[n].getValue()));
                        } else if (v[n].getValue() instanceof ByteString) {
                            opcUaSignal.members[n - 1].adjustType(StructMember.STRUCT_TYPE_BINARY);
                            opcUaSignal.members[n - 1].setValue(((ByteString) v[n].getValue()).bytes());
                        } else {
                            opcUaSignal.members[n - 1].adjustType(StructMember.STRUCT_TYPE_TEXT);
                            opcUaSignal.members[n - 1].setValue(OpcUa.valToString(v[n].getValue()));
                        }
                    }
                } catch (Throwable e) {
                }
                changed = changed < CHANGED_VALUE ? CHANGED_VALUE : changed;

                if (opcUaSignal.signal != null && progress.isStreaming()) {

                    // domain / open
                    long localCurrent = current;
                    if (v[0].getValue() instanceof DateTime) {
                        if (domainBase == DOMAIN_REL_TIME)
                            localCurrent = ((DateTime) v[0].getValue()).getUtcTime() - started;
                        else
                            localCurrent = ((DateTime) v[0].getValue()).getJavaTime();
                        if (domainBase == DOMAIN_REL_TIME && localCurrent < 0)
                            localCurrent = 0;
                        current = localCurrent > current ? localCurrent : current;
                        currentAt = Utils.millies();

                    }
                    ((IStructSamplesWriter) opcUaSignal.writer).write(localCurrent, false, opcUaSignal.members);
                    changed = changed < CHANGED_SIGNALS ? CHANGED_SIGNALS : changed;
                }

            } catch (Throwable e) {
            }
        }

        synchronized private void process(OpcUaSignal opcUaSignal, DataValue v) {
            try {
                Object value = v != null && v.getValue() != null ? v.getValue().getValue() : null;
                if (value instanceof ExtensionObject) {
                    value = ((ExtensionObject) value).decode(client.getSerializationContext());
                }

                // create signal
                if (opcUaSignal.signal == null && value != null) {
                    String path = opcUaSignal.node.getPath(OpcUaAdapter.this);
                    String[] splitted = path.split("\\\\");
                    ICell scope = this.getBase();

                    // scopes
                    for (int n = 0; n < splitted.length - 1; n++) {
                        ICell child = scope.getChildByName(splitted[n], Scope.class);
                        if (child == null) {
                            child = addScope(scope, splitted[n]);
                        }
                        scope = child;
                    }

                    // type
                    SignalType type = SignalType.Unknown;
                    SignalDescriptor descriptor = SignalDescriptor.DEFAULT;
                    if (value != null) {
                        if (value instanceof Double || value instanceof Float)
                            type = SignalType.Float;
                        else if (value instanceof Number)
                            type = SignalType.Integer;
                        else if (value instanceof Boolean)
                            type = SignalType.Event;
                        else if (value instanceof ByteString)
                            type = SignalType.Binary;
                        else if (value instanceof Object[] && ((Object[]) value).length > 0) {

                            Object arrayVal = ((Object[]) value)[0];
                            if (arrayVal instanceof Double || arrayVal instanceof Float)
                                type = SignalType.FloatArray;
                            else if (arrayVal instanceof Number)
                                type = SignalType.IntegerArray;
                            else if (arrayVal instanceof Boolean)
                                type = SignalType.EventArray;
                            else {
                                type = SignalType.TextArray;
                            }
                        } else
                            type = SignalType.Text;
                    }

                    // signal
                    if (type != SignalType.Unknown) {
                        opcUaSignal.signal = addSignal(scope, opcUaSignal.node.getName(), null, ProcessType.Discrete, type, descriptor);
                        opcUaSignal.writer = getWriter(opcUaSignal.signal);
                    }
                    changed = changed < CHANGED_RECORD ? CHANGED_RECORD : changed;
                }

                // value
                boolean tag = v.getStatusCode().isBad();
                try {
                    if (opcUaSignal.writer instanceof ITextSamplesWriter) {
                        boolean isArray = opcUaSignal.writer.getSignalType() == SignalType.TextArray;
                        if (value instanceof Object[] && !isArray) {
                            String newValue = "";
                            for (int n = 0; n < ((Object[]) value).length; n++)
                                newValue += (newValue.isEmpty() ? "" : "; ") + OpcUa.valToString(((Object[]) value)[n]);
                            opcUaSignal.value = newValue;
                        } else if (value instanceof Object[] && isArray) {
                            int scale = ((Object[]) value).length;
                            String[] newValue = new String[scale];
                            for (int n = 0; n < scale; n++)
                                newValue[n] = n < ((Object[]) value).length ? OpcUa.valToString(((Object[]) value)[n]) : "";
                            opcUaSignal.value = newValue;
                        } else
                            opcUaSignal.value = OpcUa.valToString(value);
                    } else if (opcUaSignal.writer instanceof IEventSamplesWriter) {
                        boolean isArray = opcUaSignal.writer.getSignalType() == SignalType.EventArray;
                        if (value instanceof Object[] && isArray) {
                            int scale = ((Object[]) value).length;
                            String[] newValue = new String[scale];
                            for (int n = 0;  n < scale; n++)
                                newValue[n] = OpcUa.valToString(((Object[]) value)[n]);
                            opcUaSignal.value = newValue;
                        } else
                            opcUaSignal.value = OpcUa.valToString(value);
                    } else if (opcUaSignal.writer instanceof IBinarySamplesWriter) {

                        if (value instanceof ByteString)
                            opcUaSignal.value = ((ByteString) value).bytes();
                        else
                            opcUaSignal.value = OpcUa.valToString(value);
                    } else if (opcUaSignal.writer instanceof IFloatSamplesWriter) {
                        boolean isArray = opcUaSignal.writer.getSignalType() == SignalType.FloatArray;
                        if (value instanceof Object[] && isArray) {
                            int scale = ((Object[]) value).length;
                            double[] newValue = new double[scale];
                            for (int n = 0; n < scale; n++)
                                newValue[n] = ((Object[]) value)[n] instanceof Number ? ((Number) ((Object[]) value)[n]).doubleValue() : 0.0;
                            opcUaSignal.value = newValue;
                        } else
                            opcUaSignal.value = value;
                    } else if (opcUaSignal.writer instanceof IIntegerSamplesWriter) {
                        boolean isArray = opcUaSignal.writer.getSignalType() == SignalType.IntegerArray;
                        if (value instanceof Object[] && isArray) {
                            int scale = ((Object[]) value).length;
                            long[] newValue = new long[scale];
                            for (int n = 0; n < scale; n++)
                                newValue[n] = ((Object[]) value)[n] instanceof Number ? ((Number) ((Object[]) value)[n]).longValue() : 0;
                            opcUaSignal.value = newValue;
                        } else
                            opcUaSignal.value = value;
                    } else if (opcUaSignal.writer instanceof IStructSamplesWriter) {
                        if (value instanceof UaStructure) {
                            for (Field field : value.getClass().getDeclaredFields()) {
                                if (field.isAccessible()) {
                                    String name = field.getName();
                                    Class type = field.getType();
                                    Object val = field.get(value);
                                    // Utils.log(name, type, val);
                                }
                            }
                        } else
                            opcUaSignal.value = value;
                    } else
                        opcUaSignal.value = value;
                } catch (Throwable e) {
                }
                changed = changed < CHANGED_VALUE ? CHANGED_VALUE : changed;

                if (opcUaSignal.signal != null && progress.isStreaming()) {

                    // domain / open
                    long localCurrent = current;
                    DateTime time = v.getSourceTime();
                    if (time == null)
                        time = v.getServerTime();
                    if (time != null) {
                        if (domainBase == DOMAIN_REL_TIME)
                            localCurrent = time.getUtcTime() - started;
                        else
                            localCurrent = time.getJavaTime();
                        if (domainBase == DOMAIN_REL_TIME && localCurrent < 0)
                            localCurrent = 0;
                        current = localCurrent > current ? localCurrent : current;
                        currentAt = Utils.millies();
                    }

                    // write
                    if (opcUaSignal.writer instanceof IFloatSamplesWriter) {
                        if (opcUaSignal.value instanceof Float)
                            ((IFloatSamplesWriter) opcUaSignal.writer).write(localCurrent, tag, (Float) opcUaSignal.value);
                        else if (opcUaSignal.value instanceof Double)
                            ((IFloatSamplesWriter) opcUaSignal.writer).write(localCurrent, tag, (Double) opcUaSignal.value);
                        else if (opcUaSignal.value instanceof double[])
                            ((IFloatSamplesWriter) opcUaSignal.writer).write(localCurrent, tag, (double[]) opcUaSignal.value);
                    } else if (opcUaSignal.writer instanceof IIntegerSamplesWriter) {
                        if (opcUaSignal.value instanceof ULong)
                            ((IIntegerSamplesWriter) opcUaSignal.writer).write(localCurrent, tag, ((ULong) opcUaSignal.value).toBigInteger());
                        else if (opcUaSignal.value instanceof BigInteger)
                            ((IIntegerSamplesWriter) opcUaSignal.writer).write(localCurrent, tag, ((BigInteger) opcUaSignal.value));
                        else if (opcUaSignal.value instanceof Number)
                            ((IIntegerSamplesWriter) opcUaSignal.writer).write(localCurrent, tag, ((Number) opcUaSignal.value).longValue());
                        else if (opcUaSignal.value instanceof long[])
                            ((IIntegerSamplesWriter) opcUaSignal.writer).write(localCurrent, tag, (long[]) opcUaSignal.value);
                    } else if (opcUaSignal.writer instanceof IEventSamplesWriter) {
                        if (opcUaSignal.value instanceof Number)
                            ((IEventSamplesWriter) opcUaSignal.writer).write(localCurrent, tag, ((Number) opcUaSignal.value).intValue());
                        else if (opcUaSignal.value instanceof String[])
                            ((IEventSamplesWriter) opcUaSignal.writer).write(localCurrent, tag, (String[]) opcUaSignal.value);
                        else if (opcUaSignal.value instanceof Object)
                            ((IEventSamplesWriter) opcUaSignal.writer).write(localCurrent, tag, ((Object) opcUaSignal.value).toString());
                    } else if (opcUaSignal.writer instanceof IBinarySamplesWriter) {
                        if (opcUaSignal.value instanceof byte[])
                            ((IBinarySamplesWriter) opcUaSignal.writer).write(localCurrent, tag, (byte[]) opcUaSignal.value);
                    } else if (opcUaSignal.writer instanceof ITextSamplesWriter) {
                        if (opcUaSignal.value instanceof String[])
                            ((ITextSamplesWriter) opcUaSignal.writer).write(localCurrent, tag, (String[]) opcUaSignal.value);
                        else if (opcUaSignal.value instanceof Object)
                            ((ITextSamplesWriter) opcUaSignal.writer).write(localCurrent, tag, ((Object) opcUaSignal.value).toString());
                    }
                    changed = changed < CHANGED_SIGNALS ? CHANGED_SIGNALS : changed;
                }
            } catch (Throwable e) {
            }
        }

        @Override
        public boolean supportsStreaming() {
            return true;
        }

        @Override
        synchronized public ICover flush() {
            if (changed != CHANGED_NONE) {
                changed = CHANGED_NONE;
                long adjcurrent = current;
                long delta = Math.abs(Utils.millies() - currentAt);
                if (delta < 3000) {
                    if (domainBase == DOMAIN_REL_TIME)
                        adjcurrent += delta * 10000;
                    else
                        adjcurrent += delta;
//                    Utils.log(adjcurrent);
                    return super.doFlush(adjcurrent);
                }
            }
            return null;
        }

        @Override
        public int hasChanged() {
            return changed;
        }

        @Override
        synchronized public void close() throws IOException {
            closed = true;
        }

        @Override
        public Object getProvider(Class clazz, Object subject) {
            if (clazz.equals(IActiveValueProvider.class))
                for (OpcUaSignal opcuaSignal : signals.values())
                    if (opcuaSignal.signal == subject) {
                        if (opcuaSignal.painter == null) {

                            opcuaSignal.painter = new IActiveValueProvider() {

                                @Override
                                public boolean isActive() {
                                    return !closed && progress != null && (!progress.isStreaming() || progress.isUpdating());
                                }

                                @Override
                                public Object getValue() {
                                    return opcuaSignal.value;
                                }

                                @Override
                                public boolean paint(ITlkPainter painter, int x, int y, int width, int height, Object value) {

                                    return false; // let impulse paint the value

                                }
                            };
                        }
                        return opcuaSignal.painter;
                    }
            return null;
        }

    }

    @Override
    public boolean validate(ICell insertPoint) {
        return true;
    }

    @Override
    public Closeable getInput(IProgress iProgress) {
        return new OpcInput();
    }

    @Override
    public IRecordReader newReader(Closeable input) {
        if (input instanceof OpcInput)
            return (OpcInput) input;
        return null;
    }

    @Override
    public Object getProvider(Class clazz, Object subject) {
        return null;
    }

    @Override
    public boolean isPort() {
        return getParent() instanceof ImpulsePorts;
    }

}
