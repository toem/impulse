// graph: XYGraph figure (org.eclipse.nebula.visualization.xygraph.figures.XYGraph)
// x,y,width,height: Geometry (int)
// color, background: Colors (Object)
// readable    : Input signal (de.toem.simpulse.samples.IReadableSamples)
// progress    : Progress control (de.toem.pattern.threading.IProgress)
// console     : Console output (de.toem.impulse.scripting.IScriptConsole)

// simple modifications
/*
graph.primaryXAxis.setAutoScale(false);
graph.primaryXAxis.setTitle("my x");
graph.primaryYAxis.setAutoScale(false);
graph.primaryYAxis.setTitle("my y");
graph.primaryYAxis.setDashGridLine(true);
graph.primaryYAxis.setShowMajorGrid(true);
*/

// add a trace - use "Manual Fill" flag to disable default traces
/*
var CircularBufferDataProvider = Java.type("org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider");
var Sample = Java.type("org.eclipse.nebula.visualization.xygraph.dataprovider.Sample");
var Trace = Java.type("org.eclipse.nebula.visualization.xygraph.figures.Trace");
var traceDataProvider = new CircularBufferDataProvider(false);
var length = Math.min(500, readable.getCount());
traceDataProvider.setBufferSize(length);
for (var n = 0; n < length; n++) {
	var value = readable.compoundAt(n);
    if (!value.isNone()) {
    	traceDataProvider.addSample(new Sample(value.getUnits(), value.floatValue()));
    }
}
var trace = new Trace("my trace", graph.primaryXAxis, graph.primaryYAxis, traceDataProvider);
trace.setForegroundColor(color);
graph.addTrace(trace);
*/

// custom paintings  (in the background)
/*
graph.setTransparent(true);
gc.setForeground(color);
gc.drawLine(x,y,x+100,y+100);
*/