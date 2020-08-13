//  ------------------------------------------------------------------
//  http://toem.de
//  ------------------------------------------------------------------
// template: text

package de.toem.impulse.extension.yakindu.yet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.toem.basics.core.Utils;
import de.toem.impulse.cells.ports.IPortProgress;
import de.toem.impulse.cells.record.Scope;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.domain.IDomainBase;
import de.toem.impulse.domain.TimeBase;
import de.toem.impulse.samples.IEventSamplesWriter;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.samples.ISamplesWriter;
import de.toem.impulse.samples.IStructSamplesWriter;
import de.toem.impulse.serializer.AbstractSingleDomainRecordReader;
import de.toem.impulse.serializer.ParseException;
import de.toem.impulse.values.StructMember;
import de.toem.pattern.element.ICover;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.threading.IProgress;

public class YetReader extends AbstractSingleDomainRecordReader {

    long current; // current domain position
    int changed; // change flag for stream reader
    int version;
    Map<String, Module> modules = new HashMap<>();
    boolean changedStates = false;

    static final int INIT = 0;
    static final String META_STATE_ENTERED = "@StateEntered";
    static final String META_STATE_EXITED = "@StateExited";
    static final String META_RUN_START = "@RunCycleStart";
    static final String META_RUN_END = "@RunCycleEnd";
    static final String META_SET = "@Set";

    public YetReader() {
        super();
    }

    public YetReader(String id, InputStream in) {
        super(id, in);
    }

    static public IPropertyModel getPropertyModel() {
        return getDefaultPropertyModel();
    }

    @Override
    protected int isApplicable(String name, String contentType) {
        return 0x40;
    }

    @Override
    protected int isApplicable(byte[] buffer) {
        String lines = new String(buffer);
        return lines.contains("%version=") && lines.contains("%domain=") ? APPLICABLE : NOT_APPLICABLE;

    }
    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    synchronized public ICover flush() {
        if (changed != CHANGED_NONE) {
            changed = CHANGED_NONE;
            return super.doFlush(current);
        }
        return null;
    }

    @Override
    public int hasChanged() {
        return changed;
    }

    @Override
    protected void parse(IProgress progress, InputStream in) throws ParseException {

        // Init the record
        initRecord("YET Reader", TimeBase.ms);


        // Wait for stream mode
        if (progress instanceof IPortProgress)
            synchronized (progress) {
                while (!((IPortProgress) progress).isStreaming() && !progress.isCanceled())
                    try {
                        progress.wait(250);
                    } catch (InterruptedException e) {
                    }
            }

        // Start at 0 ns
        current = 0;
        open(current);
        changed = CHANGED_RECORD;

        // read the input stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        try {
            while ((line = reader.readLine()) != null)
                parse(line.trim());
        } catch (IOException e) {
        }

        // close
        synchronized (this) {
            close(current + 1);
            changed = CHANGED_NONE;
        }
    }

    synchronized private void parse(String line) throws ParseException {

        if (line.startsWith("%")) {
            int idx = line.indexOf('=');
            if (idx > 0) {
                String key = line.substring(1, idx).trim().toLowerCase();
                String val = line.substring(idx + 1).trim().toLowerCase();
                if (key.equals("domain")) {
                    IDomainBase base = TimeBase.ms;

                    switch (val) {
                    case "time_ms":
                        base = TimeBase.ms;
                        ;
                        break;
                    case "time_us":
                        base = TimeBase.us;
                        ;
                        break;
                    case "time_ns":
                        base = TimeBase.ns;
                        ;
                        break;
                    }
                    initRecord("YET Reader", base);
                    changed = changed > CHANGED_RECORD ? changed : CHANGED_RECORD;
                } else if (key.equals("version")) {
                    this.version = Utils.parseInt(val, 1);
                }
            }

        } else if (line.startsWith("#")) {
            String[] columns = line.split(",");
            if (columns != null && columns.length >= 2) {
                String col = columns[0].trim();
                if (col.startsWith("#")) {

                    // position
                    long position = current;
                    if (col.substring(1).trim().equalsIgnoreCase("init")) {
                        position = INIT;
                    } else
                        position = Utils.parseLong(col.substring(1).trim(), Long.MIN_VALUE);

                    // module.slot@meta
                    col = columns[1].trim();
                    String module = null;
                    String slot = null;
                    String meta = null;
                    int idx = col.indexOf('@');
                    if (idx > 0) {
                        meta = col.substring(idx);
                        col = col.substring(0, idx);
                    }
                    idx = col.indexOf('.');
                    if (idx > 0) {
                        slot = col.substring(idx + 1);
                        module = col.substring(0, idx);
                    } else
                        module = col;

                    // parameter
                    Object parameter = null;
                    if (columns.length >= 3) {
                        col = columns[2].trim();
                        parameter = Utils.parseValue(col, null);
                    }

                    // handle
                    if (!modules.containsKey(module))
                        modules.put(module, new Module(module));
                    modules.get(module).handle(position, slot, meta, parameter);

                    // inc current position
                    if (position >current) {
                        current = position;
                        changed = changed > CHANGED_CURRENT ? changed : CHANGED_CURRENT;
                    }
                }
            }
        } else {
            // ignore
        }
    }

    class Module {
        Scope scope;
        List<String> states = new ArrayList<>();
        List<String> active = new ArrayList<>();
        IEventSamplesWriter statesWriter;
        IStructSamplesWriter raisedWriter;
        StructMember[] raisedMembers;
        IStructSamplesWriter metaWriter;
        StructMember[] metaMembers;
        Map<String, ISamplesWriter> variableWriter = new HashMap<>();

        Module(String name) {
            scope = YetReader.this.addScope(null, name, null);
            Signal signal = YetReader.this.addSignal(scope, "@States", "Current actives states", ProcessType.Discrete, SignalType.Event,
                    new SignalDescriptor(ISample.CONTENT_STATE, ISample.FORMAT_TEXT));
            statesWriter = (IEventSamplesWriter) YetReader.this.getWriter(signal);
            signal = YetReader.this.addSignal(scope, "@Raised", "Raised events", ProcessType.Discrete, SignalType.Struct,
                    new SignalDescriptor(ISample.CONTENT_EVENT, ISample.FORMAT_DEFAULT));
            raisedWriter = (IStructSamplesWriter) YetReader.this.getWriter(signal);
            raisedMembers = raisedWriter.createMembers(2);
            raisedWriter.createMember(raisedMembers, 0, "Event", ISample.STRUCT_TYPE_ENUM, ISample.CONTENT_EVENT, ISample.FORMAT_TEXT);
            raisedWriter.createMember(raisedMembers, 1, "Parameter", ISample.STRUCT_TYPE_TEXT, ISample.CONTENT_EVENTPARM, ISample.FORMAT_DEFAULT);
            signal = YetReader.this.addSignal(scope, "@Meta", "Meta events", ProcessType.Discrete, SignalType.Struct,
                    new SignalDescriptor(ISample.CONTENT_EVENT, ISample.FORMAT_DEFAULT));
            metaWriter = (IStructSamplesWriter) YetReader.this.getWriter(signal);
            metaMembers = metaWriter.createMembers(5);
            metaWriter.createMember(metaMembers, 0, "Event", ISample.STRUCT_TYPE_ENUM, ISample.CONTENT_EVENT, ISample.FORMAT_TEXT);
            metaWriter.createMember(metaMembers, 1, "Parameter", ISample.STRUCT_TYPE_TEXT, ISample.CONTENT_EVENTPARM, ISample.FORMAT_DEFAULT);
        }

        public void handle(long position, String slot, String meta, Object parameter) {

            if (META_SET.equalsIgnoreCase(meta)) {
                // set a variable
                if (!variableWriter.containsKey(slot)) {
                    SignalType type = SignalType.Unknown;
                    if (parameter instanceof Long)
                        type = SignalType.Integer;
                    else if (parameter instanceof Double)
                        type = SignalType.Float;
                    else if (parameter instanceof Boolean)
                        type = SignalType.Logic;
                    else
                        type = SignalType.Text;
                    Signal signal = YetReader.this.addSignal(scope, slot, null, ProcessType.Discrete, type, SignalDescriptor.DEFAULT);
                    variableWriter.put(slot, YetReader.this.getWriter(signal));
                    changed = changed > CHANGED_RECORD ? changed : CHANGED_RECORD;
                }
                variableWriter.get(slot).writeSample(position, false, parameter);
            } else if (META_STATE_ENTERED.equalsIgnoreCase(meta)) {
                // enter state
                String state = String.valueOf(parameter);
                if (!states.contains(state))
                    states.add(state);
                if (!active.contains(state))
                    active.add(state);
                changedStates = true;
            } else if (META_STATE_EXITED.equalsIgnoreCase(meta)) {
                // exit state
                String state = String.valueOf(parameter);
                if (!states.contains(state))
                    states.add(state);
                if (active.contains(state))
                    active.remove(state);
                changedStates = true;
            }
            if (Utils.isEmpty(meta)) {
                // raise event
                raisedMembers[0].setValue(slot);
                raisedMembers[1].setValue(String.valueOf(parameter));
                raisedMembers[1].setValid(parameter != null);
                raisedWriter.writeSample(position, false, raisedMembers);
            } else if (Utils.isEmpty(slot)) {
                // meta
                metaMembers[0].setValue(meta);
                metaMembers[1].setValue(parameter != null ? String.valueOf(parameter) : null);
                metaMembers[1].setValid(parameter != null);
                metaWriter.writeSample(position, false, metaMembers);
            }

            // states have changed
            if ((META_RUN_START.equalsIgnoreCase(meta) || META_RUN_END.equalsIgnoreCase(meta)) && changedStates) {
                String total = null;
                for (String s : states)
                    if (active.contains(s))
                        total = total == null ? s : total + ";" + s;
                statesWriter.writeSample(position, false, total);
                changedStates = false;
            }
            changed = changed > CHANGED_SIGNALS ? changed : CHANGED_SIGNALS;
        }

    }
}
