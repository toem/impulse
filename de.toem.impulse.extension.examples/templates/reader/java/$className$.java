//  ------------------------------------------------------------------
//  http://toem.de
//  ------------------------------------------------------------------
// template: $readerTemplate$

package $packageName$;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import de.toem.impulse.cells.ports.IPortProgress;
import de.toem.impulse.cells.record.Scope;
import de.toem.impulse.cells.record.Signal;
import de.toem.impulse.domain.TimeBase;
import de.toem.impulse.samples.IFloatSamplesWriter;
import de.toem.impulse.samples.ISamples.ProcessType;
import de.toem.impulse.samples.ISamples.SignalDescriptor;
import de.toem.impulse.samples.ISamples.SignalType;
import de.toem.impulse.serializer.AbstractSingleDomainRecordReader;
import de.toem.impulse.serializer.ParseException;
import de.toem.pattern.element.ICover;
import de.toem.pattern.threading.IProgress;

public class $className$ extends AbstractSingleDomainRecordReader {

	long current; // current domain position
%if createStreamReader
	int changed; // change flag for stream reader
%endif

%if readerTemplate == "text"
	Signal float1,float2,float3;
%endif
%if readerTemplate == "binary"
	Signal float1,float2,float3;
%endif

	public $className$() {
		super();
	}

	public $className$(String id, InputStream in) {
		super(id, in);
	}

	@Override
	protected int isApplicable(String name, String contentType) {
		return 8; // need 8 bytes to check
	}

	@Override
	protected int isApplicable(byte[] buffer) {
		if (false)
			return APPLICABLE;
		return NOT_APPLICABLE;
	}
%if createStreamReader
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
%endif

	@Override
	protected void parse(IProgress progress, InputStream in) throws ParseException {


		// Init the record
		initRecord("$readerLabel$", TimeBase.ns);

%if readerTemplate == "text"		
		// Create some content
		Scope system = addScope(null, "System"); // null means at root
		float1 = addSignal(system, "Signal1", "My first float Signal", ProcessType.Discrete, SignalType.Float,
				SignalDescriptor.Float64);
		float2 = addSignal(system, "Signal2", "My second float Signal", ProcessType.Discrete, SignalType.Float,
				SignalDescriptor.Float64);
		Scope another = addScope(system, "Another");
		float3 = addSignal(another, "Signal3", "Another float", ProcessType.Discrete, SignalType.Float,
				SignalDescriptor.Float64);
%endif
%if readerTemplate == "binary"		
		// Create some content
		Scope system = addScope(null, "System"); // null means at root
		float1 = addSignal(system, "Signal1", "My first float Signal", ProcessType.Discrete, SignalType.Float,
				SignalDescriptor.Float64);
		float2 = addSignal(system, "Signal2", "My second float Signal", ProcessType.Discrete, SignalType.Float,
				SignalDescriptor.Float64);
		Scope another = addScope(system, "Another");
		float3 = addSignal(another, "Signal3", "Another float", ProcessType.Discrete, SignalType.Float,
				SignalDescriptor.Float64);
%endif
%if createStreamReader 
		// mark what has changed
		changed = CHANGED_RECORD;
%endif		

		
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

		// read the input stream 
%if readerTemplate == "text"		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line=null;
		try {
			while((line = reader.readLine())!=null)
				parse(line);
		} catch (IOException e) {
		}
%endif
%if readerTemplate == "binary"
		byte[] buffer = new byte[4096];
		int filled = 0;
		int read = 0;
		try {
			while((read = in.read(buffer, filled, buffer.length-filled))>=0){
				filled+=read;
				int used = parse(buffer,filled);
				System.arraycopy(buffer, used, buffer, 0, filled-used);
				filled -= used;
			}
		} catch (IOException e) {
		}
%endif

		// And close finally
		close(current);
	}


%if readerTemplate == "text"
%if createStreamReader 
	synchronized 
%endif
	private void parse(String line) throws ParseException {

		// if you find a problem, throw an exception
		if (false)
			throw new ParseException("Not Valid");
	
		// get the current domain position
		current+=10;
		
		// write data
		((IFloatSamplesWriter) getWriter(float1)).write(current, Math.sin((current + 100) / 3000.0) * 10.0);
		((IFloatSamplesWriter) getWriter(float2)).write(current, Math.cos((current + 200) / 1000.0) * 10.0);
		((IFloatSamplesWriter) getWriter(float3)).write(current, Math.sin((current + 300) / 6000.0) * 10.0 + 10.00001);
%if createStreamReader 
		// mark what has changed
		changed = CHANGED_SIGNALS;
%endif

	}
%endif

%if readerTemplate == "binary"
%if createStreamReader 
	synchronized 
%endif
private int parse(byte[] data, int len) throws ParseException {


		// if you find a problem, throw an exception
		if (false)
			throw new ParseException("Not Valid");
	
		// get the current domain position
		current+=10;
		
		// write data
		((IFloatSamplesWriter) getWriter(float1)).write(current, Math.sin((current + 100) / 3000.0) * 10.0);
		((IFloatSamplesWriter) getWriter(float2)).write(current, Math.cos((current + 200) / 1000.0) * 10.0);
		((IFloatSamplesWriter) getWriter(float3)).write(current, Math.sin((current + 300) / 6000.0) * 10.0 + 10.00001);
%if createStreamReader 
		// mark what has changed
		changed = CHANGED_SIGNALS;
%endif

		return len; // return used bytes
	}
%endif
}
