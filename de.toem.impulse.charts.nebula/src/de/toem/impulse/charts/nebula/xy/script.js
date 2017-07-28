// graph: XYGraph figure ( https://eclipse.org/nebula/widgets/visualization/visualization.php)
// x,y,width,height: geometry
// color, background: Colors
// readable: input of IReadableSamples  (http://toem.de/index.php/projects/impulse/scripts/reference)

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
importPackage(Packages.org.eclipse.nebula.visualization.xygraph.dataprovider);
importPackage(Packages.org.eclipse.nebula.visualization.xygraph.figures);
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