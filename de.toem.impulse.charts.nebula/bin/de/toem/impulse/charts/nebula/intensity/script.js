// graph: IntentsityGraph figure ( https://eclipse.org/nebula/widgets/visualization/visualization.php)
// x,y,width,height: geometry
// color, background: Colors
// readable: input of IReadableSamples  (http://toem.de/index.php/projects/impulse/scripts/reference)

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
var simuData = java.lang.reflect.Array.newInstance(java.lang.Integer.TYPE, dataWidth * dataHeight * 2);
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
