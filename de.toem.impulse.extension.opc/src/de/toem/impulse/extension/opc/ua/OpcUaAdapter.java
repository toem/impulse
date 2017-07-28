//  ------------------------------------------------------------------
//  Copyright (C) 2012-2014  Thomas Haber 
//  http://toem.de
//  ------------------------------------------------------------------
package de.toem.impulse.extension.opc.ua;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import com.digitalpetri.opcua.sdk.client.api.subscriptions.UaSubscription;
import com.digitalpetri.opcua.stack.core.AttributeId;
import com.digitalpetri.opcua.stack.core.UaSerializationException;
import com.digitalpetri.opcua.stack.core.serialization.DelegateRegistry;
import com.digitalpetri.opcua.stack.core.serialization.EncoderDelegate;
import com.digitalpetri.opcua.stack.core.serialization.UaEncoder;
import com.digitalpetri.opcua.stack.core.serialization.UaEnumeration;
import com.digitalpetri.opcua.stack.core.serialization.UaSerializable;
import com.digitalpetri.opcua.stack.core.serialization.UaStructure;
import com.digitalpetri.opcua.stack.core.types.builtin.ByteString;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.DateTime;
import com.digitalpetri.opcua.stack.core.types.builtin.DiagnosticInfo;
import com.digitalpetri.opcua.stack.core.types.builtin.ExpandedNodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.ExtensionObject;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.builtin.XmlElement;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UByte;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.ULong;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UShort;
import com.digitalpetri.opcua.stack.core.types.enumerated.MonitoringMode;
import com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.digitalpetri.opcua.stack.core.types.structured.EventFilter;
import com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import com.digitalpetri.opcua.stack.core.types.structured.MonitoringParameters;
import com.digitalpetri.opcua.stack.core.types.structured.ReadValueId;
import com.digitalpetri.opcua.stack.core.types.structured.SimpleAttributeOperand;

import de.toem.basics.core.Utils;
import de.toem.impulse.cells.ports.AbstractPortAdapterBaseCell;
import de.toem.impulse.cells.ports.IPortAdapter;
import de.toem.impulse.cells.ports.IPortProgress;
import de.toem.impulse.cells.ports.IPortProviderFactory;
import de.toem.impulse.cells.ports.IRecordPort;
import de.toem.impulse.cells.ports.MultiAdapterPort;
import de.toem.impulse.cells.preferences.ImpulsePorts;
import de.toem.impulse.cells.record.Scope;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.domain.TimeBase;
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
import de.toem.impulse.serializer.AbstractPortAdapterRecordReader;
import de.toem.impulse.serializer.AbstractSingleDomainRecordReader;
import de.toem.impulse.serializer.IRecordReader;
import de.toem.impulse.values.StructMember;
import de.toem.pattern.element.CellAnnotation;
import de.toem.pattern.element.FieldAnnotation;
import de.toem.pattern.element.ICell;
import de.toem.pattern.element.ICover;
import de.toem.pattern.threading.IProgress;

@CellAnnotation(type = OpcUaAdapter.TYPE, dynamicChildOf = { MultiAdapterPort.TYPE, ImpulsePorts.TYPE })
public class OpcUaAdapter extends AbstractPortAdapterBaseCell implements IRecordPort, IPortAdapter {
    public static final String TYPE = "port.record.opc.ua";

    public String server = "opc.tcp://opcua.demo-this.com:51210/UA/SampleServer";
    public int publishRate = 1000;
    public boolean logToConsole;

    // identification
    public static final int IDENT_ANONYMOUS = 0;
    public static final int IDENT_PASSWORD = 1;
    public static final String[] IDENT_OPTIONS = { "Anonymous", "Password" };
    public int identification = IDENT_ANONYMOUS;
    public String user;
    public String password;

    // security
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_SIGN = 1;
    public static final int SECURITY_SIGN_ENCRYPT = 2;
    public static final String[] SECURITY_OPTIONS = { "None", "Sign", "Sign and Encrypt" };
    public int securityMode = SECURITY_NONE;
    public static final int POLICY_Basic128Rsa15 = 0;
    public static final int POLICY_Basic256 = 1;
    public static final int POLICY_Basic256Sha256 = 2;
    public static final String[] POLICY_OPTIONS = { "Basic128Rsa15", "Basic256", "Basic256Sha256" };
    public int securityPolicy = POLICY_Basic128Rsa15;

    // certificate
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

    class OpcInput extends AbstractPortAdapterRecordReader implements Closeable, IPortProviderFactory {

        long current;
        boolean closed;
        int changed;

        class OpcUaSignal {
            OpcUaNode node;
            Signal signal;
            ISamplesWriter writer;
            StructMember[] members;
            IActiveValueProvider painter;
            Object value;
            long started;
        }

        Map<UInteger, OpcUaSignal> signals = new HashMap<UInteger, OpcUaSignal>();
        IPortProgress progress;

        @Override
        protected void process(IPortProgress progress) {

            // progress
            if (progress == null)
                return;
            this.progress = progress;
            OpcUaClient client = null;

            try {

                // Init the record and signals
                initRecord(server, TimeBase.ns100);

                // init opc
                client = OpcUa.createAndConnect(OpcUaAdapter.this);

                UaSubscription subscription = client.getSubscriptionManager().createSubscription(publishRate).get();

                List<MonitoredItemCreateRequest> requests = new ArrayList<MonitoredItemCreateRequest>();

                for (ICell cell : OpcUaAdapter.this.getTribe(false))
                    if (cell instanceof OpcUaNode && ((OpcUaNode) cell).enabled) {

                        OpcUaNode node = (OpcUaNode) cell;

                        // create request
                        UInteger clientHandle = uint(OpcUa.clientHandles.getAndIncrement());
                        MonitoredItemCreateRequest request = null;
                        List<StructMember> members = null;
                        if (node.isVariable()) {
                            ReadValueId readValueId = new ReadValueId(NodeId.parse(node.nodeId), AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);
                            MonitoringParameters parameters = new MonitoringParameters(clientHandle,
                                    (node.sampleRate > 0) ? (double) node.sampleRate : (double) publishRate, null,
                                    uint(node.queueSize > 0 ? node.queueSize : 5), true);
                            request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);

                        } else if (node.isObject()) {

                            ReadValueId readValueId = new ReadValueId(NodeId.parse(node.nodeId), AttributeId.EventNotifier.uid(), null,
                                    QualifiedName.NULL_VALUE);
                            List<SimpleAttributeOperand> select = new ArrayList<SimpleAttributeOperand>();
                            select.add(new SimpleAttributeOperand(null, new QualifiedName[] { new QualifiedName(0, "Time") }, AttributeId.Value.uid(), null));
                            select.add(
                                    new SimpleAttributeOperand(null, new QualifiedName[] { new QualifiedName(0, "Message") }, AttributeId.Value.uid(), null));
                            select.add(new SimpleAttributeOperand(null, new QualifiedName[] { new QualifiedName(0, "SourceName") }, AttributeId.Value.uid(),
                                    null));
                            select.add(
                                    new SimpleAttributeOperand(null, new QualifiedName[] { new QualifiedName(0, "Severity") }, AttributeId.Value.uid(), null));
                            if (!Utils.isEmpty(node.additionalAttributes))
                                for (String attr : node.additionalAttributes.trim().split("\\,")) {
                                    if (!Utils.isEmpty(attr.trim())) {
                                        select.add(new SimpleAttributeOperand(null, new QualifiedName[] { new QualifiedName(0, attr.trim()) },
                                                AttributeId.Value.uid(), null));
                                    }
                                }
                            EventFilter filter = new EventFilter(select.toArray(new SimpleAttributeOperand[select.size()]), null);
                            ExtensionObject eo = ExtensionObject.encodeAsByteString(filter, EventFilter.BinaryEncodingId);
                            MonitoringParameters parameters = new MonitoringParameters(clientHandle, 0.0, eo, uint(0), true);
                            request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);

                            // create members
                            members = new ArrayList<StructMember>();
                            members.add(new StructMember("Message", StructMember.STRUCT_TYPE_TEXT, null, ISample.FORMAT_DEFAULT));
                            members.add(new StructMember("SourceName", StructMember.STRUCT_TYPE_ENUM, null, ISample.FORMAT_DEFAULT));
                            members.add(new StructMember("Severity", StructMember.STRUCT_TYPE_INTEGER, null, ISample.FORMAT_DEFAULT));
                            if (!Utils.isEmpty(node.additionalAttributes))
                                for (String attr : node.additionalAttributes.trim().split("\\,")) {
                                    if (!Utils.isEmpty(attr.trim())) {
                                        members.add(new StructMember(attr.trim(), StructMember.STRUCT_TYPE_UNKNOWN, null, ISample.FORMAT_DEFAULT));
                                    }
                                }
                        }
                        // got a request
                        if (request != null) {
                            requests.add(request);
                            OpcUaSignal signal = new OpcUaSignal();
                            signal.node = node;
                            signal.members = members != null ? members.toArray(new StructMember[members.size()]) : null;
                            signals.put(clientHandle, signal);
                        }
                    }
                // create monitored items
                List<UaMonitoredItem> items = subscription.createMonitoredItems(TimestampsToReturn.Both, requests).get();

                // handler for monitor events
                for (UaMonitoredItem item : items) {
                    OpcUaSignal signal = signals.get(item.getClientHandle());
                    if (signal.node.isVariable())
                        item.setValueConsumer(v -> {

                            process(signal, v);
                        });
                    else if (signal.node.isObject())
                        item.setEventConsumer(v -> {

                            process(signal, v);
                        });

                }

                changed = CHANGED_RECORD;

                // wait after header parsing
                synchronized (progress) {
                    while (!progress.isStreaming() && !progress.isCanceled())
                        try {
                            progress.wait(100);

                        } catch (Throwable e) {
                        }
                }

                current = 0;
                // final long start = Utils.millies();
                // open(current);
                synchronized (progress) {
                    while (!closed && (progress == null || !progress.isCanceled())) {

                        progress.wait(100);
                    }
                }
                subscription.deleteMonitoredItems(items);
            } catch (Throwable e) {
                AbstractSingleDomainRecordReader.addParseErrorMessage(this.id, e, this.base);
            } finally {
                if (client != null)
                    try {
                        OpcUa.disconnect(client);
                    } catch (Exception e) {
                    }
                close(current);
                progress = null;

            }
        }

        synchronized private void process(OpcUaSignal opcUaSignal, Variant[] v) {
            try {
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
                    // type
                    SignalType type = SignalType.Struct;
                    SignalDescriptor descriptor = SignalDescriptor.DEFAULT;

                    // signal
                    opcUaSignal.signal = addSignal(scope, "Events", null, ProcessType.Discrete, type, descriptor);
                    opcUaSignal.writer = getWriter(opcUaSignal.signal);
                    changed = CHANGED_RECORD;
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
                            opcUaSignal.members[n - 1].setValue(valToString(v[n].getValue()));
                        } else if (v[n].getValue() instanceof ByteString) {
                            opcUaSignal.members[n - 1].adjustType(StructMember.STRUCT_TYPE_BINARY);
                            opcUaSignal.members[n - 1].setValue(((ByteString) v[n].getValue()).bytes());
                        } else {
                            opcUaSignal.members[n - 1].adjustType(StructMember.STRUCT_TYPE_TEXT);
                            opcUaSignal.members[n - 1].setValue(valToString(v[n].getValue()));
                        }
                    }
                } catch (Throwable e) {
                }
                changed = changed < CHANGED_SIGNALS ? CHANGED_SIGNALS : changed;

                if (opcUaSignal.signal != null && progress.isStreaming()) {

                    // domain / open
                    long localCurrent = current;
                    if (v[0].getValue() instanceof DateTime) {
                        if (opcUaSignal.started == 0) {
                            opcUaSignal.started = ((DateTime) v[0].getValue()).getUtcTime();
                            open(0);
                        } else {
                            localCurrent = ((DateTime) v[0].getValue()).getUtcTime() - opcUaSignal.started;
                            current = localCurrent > current ? localCurrent : current;
                        }
                    }
                    ((IStructSamplesWriter) opcUaSignal.writer).write(localCurrent, false, opcUaSignal.members);
                }

            } catch (Throwable e) {
            }
        }

        synchronized private void process(OpcUaSignal opcUaSignal, DataValue v) {
            try {
                Object value = v != null && v.getValue() != null ? v.getValue().getValue() : null;
                if (value instanceof ExtensionObject) {
                    value = ((ExtensionObject) value).decode();
                }

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
                            descriptor = new SignalDescriptor();
                            descriptor.setScale(((Object[]) value).length);
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
                    changed = CHANGED_RECORD;
                }

                // value
                boolean conflict = v.getStatusCode().isBad();
                try {
                    if (opcUaSignal.writer instanceof ITextSamplesWriter) {
                        boolean isArray = opcUaSignal.writer.getSignalType() == SignalType.TextArray;
                        int scale = opcUaSignal.writer.getSignalDescriptor().getScale();
                        if (value instanceof Object[] && !isArray) {
                            String newValue = "";
                            for (int n = 0; n < ((Object[]) value).length; n++)
                                newValue += (newValue.isEmpty() ? "" : "; ") + valToString(((Object[]) value)[n]);
                            opcUaSignal.value = newValue;
                        } else if (value instanceof Object[] && isArray) {
                            String[] newValue = new String[scale];
                            for (int n = 0; n < scale; n++)
                                newValue[n] = n < ((Object[]) value).length ?valToString(((Object[]) value)[n]):"";
                            opcUaSignal.value = newValue;
                        } else
                            opcUaSignal.value = valToString(value);
                    } else if (opcUaSignal.writer instanceof IEventSamplesWriter) {
                        boolean isArray = opcUaSignal.writer.getSignalType() == SignalType.EventArray;
                        if (value instanceof Object[] && isArray) {
                            int scale = opcUaSignal.writer.getSignalDescriptor().getScale();
                            String[] newValue = new String[scale];
                            for (int n = 0; n < newValue.length && n < scale; n++)
                                newValue[n] = valToString(((Object[]) value)[n]);
                            opcUaSignal.value = newValue;
                        } else
                            opcUaSignal.value = valToString(value);
                    } else if (opcUaSignal.writer instanceof IBinarySamplesWriter) {

                        if (value instanceof ByteString)
                            opcUaSignal.value = ((ByteString) value).bytes();
                        else
                            opcUaSignal.value = valToString(value);
                    } else if (opcUaSignal.writer instanceof IFloatSamplesWriter) {
                        boolean isArray = opcUaSignal.writer.getSignalType() == SignalType.FloatArray;
                        if (value instanceof Object[] && isArray) {
                            int scale = opcUaSignal.writer.getSignalDescriptor().getScale();
                            double[] newValue = new double[scale];
                            for (int n = 0; n < newValue.length && n < scale; n++)
                                newValue[n] = ((Object[]) value)[n] instanceof Number ? ((Number) ((Object[]) value)[n]).doubleValue() : 0.0;
                            opcUaSignal.value = newValue;
                        } else
                            opcUaSignal.value = value;
                    } else if (opcUaSignal.writer instanceof IIntegerSamplesWriter) {
                        boolean isArray = opcUaSignal.writer.getSignalType() == SignalType.IntegerArray;
                        if (value instanceof Object[] && isArray) {
                            int scale = opcUaSignal.writer.getSignalDescriptor().getScale();
                            long[] newValue = new long[scale];
                            for (int n = 0; n < newValue.length && n < scale; n++)
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
                changed = changed < CHANGED_SIGNALS ? CHANGED_SIGNALS : changed;

                if (opcUaSignal.signal != null && progress.isStreaming()) {

                    // domain / open
                    long localCurrent = current;
                    if (v.getSourceTime() != null) {
                        if (opcUaSignal.started == 0) {
                            opcUaSignal.started = v.getServerTime().getUtcTime();
                            open(0);
                        } else {
                            localCurrent = v.getSourceTime().getUtcTime() - opcUaSignal.started;
                            current = localCurrent > current ? localCurrent : current;
                        }
                    }

                    // write
                    if (opcUaSignal.writer instanceof IFloatSamplesWriter) {
                        if (opcUaSignal.value instanceof Float)
                            ((IFloatSamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, (Float) opcUaSignal.value);
                        else if (opcUaSignal.value instanceof Double)
                            ((IFloatSamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, (Double) opcUaSignal.value);
                        else if (opcUaSignal.value instanceof double[])
                            ((IFloatSamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, (double[]) opcUaSignal.value);
                    } else if (opcUaSignal.writer instanceof IIntegerSamplesWriter) {
                        if (opcUaSignal.value instanceof ULong)
                            ((IIntegerSamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, ((ULong) opcUaSignal.value).toBigInteger());
                        else if (opcUaSignal.value instanceof BigInteger)
                            ((IIntegerSamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, ((BigInteger) opcUaSignal.value));
                        else if (opcUaSignal.value instanceof Number)
                            ((IIntegerSamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, ((Number) opcUaSignal.value).longValue());
                        else if (opcUaSignal.value instanceof long[])
                            ((IIntegerSamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, (long[]) opcUaSignal.value);
                    } else if (opcUaSignal.writer instanceof IEventSamplesWriter) {
                        if (opcUaSignal.value instanceof Number)
                            ((IEventSamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, ((Number) opcUaSignal.value).intValue());
                        else if (opcUaSignal.value instanceof String[])
                            ((IEventSamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, (String[]) opcUaSignal.value);
                        else if (opcUaSignal.value instanceof Object)
                            ((IEventSamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, ((Object) opcUaSignal.value).toString());
                    } else if (opcUaSignal.writer instanceof IBinarySamplesWriter) {
                        if (opcUaSignal.value instanceof byte[])
                            ((IBinarySamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, (byte[]) opcUaSignal.value);
                    } else if (opcUaSignal.writer instanceof ITextSamplesWriter) {
                        if (opcUaSignal.value instanceof String[])
                            ((ITextSamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, (String[]) opcUaSignal.value);
                        else if (opcUaSignal.value instanceof Object)
                            ((ITextSamplesWriter) opcUaSignal.writer).write(localCurrent, conflict, ((Object) opcUaSignal.value).toString());
                    }
                }
            } catch (Throwable e) {
            }
        }

        private String valToString(Object value) {
            if (value instanceof DateTime)
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
                final StringBuilder writer = new StringBuilder();
                writer.append('{');
                EncoderDelegate<Object> delegate = DelegateRegistry.getEncoder(value);
                delegate.encode(value, new UaEncoder() {

                    private void append(String field, String valueOf) {
                        if (writer.length() > 1)
                            writer.append(';');
                        writer.append(field);
                        writer.append('=');
                        writer.append(valueOf);
                    }

                    @Override
                    public void encodeBoolean(String field, Boolean value) throws UaSerializationException {
                        append(field, valToString(value));
                    }

                    @Override
                    public void encodeSByte(String field, Byte value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeInt16(String field, Short value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeInt32(String field, Integer value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeInt64(String field, Long value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeByte(String field, UByte value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeUInt16(String field, UShort value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeUInt32(String field, UInteger value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeUInt64(String field, ULong value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeFloat(String field, Float value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeDouble(String field, Double value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeString(String field, String value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeDateTime(String field, DateTime value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeGuid(String field, UUID value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeByteString(String field, ByteString value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeXmlElement(String field, XmlElement value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeNodeId(String field, NodeId value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeExpandedNodeId(String field, ExpandedNodeId value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeStatusCode(String field, StatusCode value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeQualifiedName(String field, QualifiedName value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeLocalizedText(String field, LocalizedText value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeExtensionObject(String field, ExtensionObject value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeDataValue(String field, DataValue value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeVariant(String field, Variant value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public void encodeDiagnosticInfo(String field, DiagnosticInfo value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public <T extends UaStructure> void encodeMessage(String field, T value) throws UaSerializationException {
                        append(field, valToString(value));
                    }

                    @Override
                    public <T extends UaEnumeration> void encodeEnumeration(String field, T value) throws UaSerializationException {
                        append(field, valToString(value));
                    }

                    @Override
                    public <T extends UaSerializable> void encodeSerializable(String field, T value) throws UaSerializationException {
                        append(field, valToString(value));

                    }

                    @Override
                    public <T> void encodeArray(String field, T[] values, BiConsumer<String, T> encoder) throws UaSerializationException {

                    }
                });
                writer.append('}');
                return writer.toString();
            }
            return String.valueOf(value);
        }

        @Override
        public boolean supportsStreaming() {
            return true;
        }

        @Override
        synchronized public ICover flush() {
            changed = CHANGED_NONE;
            return super.doFlush(current);
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
                                    return progress != null && progress.isUpdating();
                                }

                                @Override
                                public Object getValue() {
                                    return opcuaSignal.value;
                                }

                                @Override
                                public boolean paint(GC gc, int x, int y, int width, int height, Object value) {

                                    gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_YELLOW));
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

    @Override
    public int getNature() {
        return IRecordPort.NATURE_CONNECT | IRecordPort.NATURE_FLOATING | IRecordPort.NATURE_REFRESH_CONTINUOUS;
    }
}
