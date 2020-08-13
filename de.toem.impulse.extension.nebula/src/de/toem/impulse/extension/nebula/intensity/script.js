// graph: IntentsityGraph figure (org.eclipse.nebula.visualization.widgets.figures.IntensityGraphFigure)
// x,y,width,height: Geometry (int)
// color, background: Colors (Object)
// readable    : Input signal (de.toem.simpulse.samples.IReadableSamples)
// progress    : Progress control (de.toem.pattern.threading.IProgress)
// console     : Console output (de.toem.impulse.scripting.IScriptConsole)

// simple modifications


// configure axes
graph.getXAxis().setAutoScale(false);
graph.getXAxis().setTitle("myX");
graph.getYAxis().setAutoScale(false);
graph.getYAxis().setTitle("myY");      
graph.getYAxis().setRange(-50, 50, false);

// configure data
graph.setMax(100);
graph.setMin(-100);
var dataHeight = 64;
var dataWidth = 64;
graph.setDataHeight(dataHeight);
graph.setDataWidth(dataHeight);

// create simulation data
var IntArray = Java.type("int[]");
var simuData = new IntArray(dataWidth * dataHeight * 2);
var i,j,seed = 1;
for (i = 0; i < dataHeight; i++) {
    for (j = 0; j < dataWidth; j++) {
        var xn = j - dataWidth;
        var yn = i - dataHeight;
        var p = Math.sqrt(xn * xn + yn * yn);
        simuData[i * dataWidth + j] = Math.sin(p * 2 * Math.PI / dataWidth + seed * Math.PI / 100) * 100;
    }
}
graph.setDataArray(simuData);
