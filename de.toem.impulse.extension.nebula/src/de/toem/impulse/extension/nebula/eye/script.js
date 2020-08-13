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

// custom paintings  (in the background)
/*
graph.setTransparent(true);
gc.setForeground(color);
gc.drawLine(x,y,x+100,y+100);
*/