// see  hthe book "Integrating and Extending BIRT"
// for more information about birt scripting.
// The following examples are extracted from package 'org.eclipse.birt.chart.example'

/*
function beforeGeneration(chart,icsc) {
	
	var xAxisPrimary = chart.getPrimaryBaseAxes()[0];
	xAxisPrimary.getTitle().setVisible(true);
}
*/
/*
function beforeDrawAxisLabel(axis, label, scriptContext) {
	importPackage(Packages.org.eclipse.birt.chart.model.attribute);
	if (axis.getType() == AxisType.TEXT_LITERAL)
		label.getCaption().getColor().set(140, 198, 62);
	else
		label.getCaption().getColor().set(208, 32, 0);
}
*/
/*
function beforeDrawAxisTitle(axis, title, scriptContext) {
	importPackage(Packages.org.eclipse.birt.chart.model.attribute);
	{
		if (axis.getType() == AxisType.LINEAR_LITERAL)
			title.getCaption().setValue('Y-Axis Title By JavaScript');
	}
	title.getCaption().getColor().set(32, 168, 255);
}
*/
/*
function beforeDrawDataPointLabel(dataPoints, label, scriptContext) {
	val = dataPoints.getOrthogonalValue();
	clr = label.getCaption().getColor();
	if (val < -10)
		clr.set(32, 168, 255);
	else if ((val >= -10) & (val <= 10))
		clr.set(168, 0, 208);
	else if (val > 10)
		clr.set(0, 208, 32);
}
*/
/*
function beforeDrawMarkerLine(axis, line, scriptContext) {
	importPackage(Packages.java.util);
	if (scriptContext.getLocale().equals(Locale.US)) {
		line.getLabel().getCaption().getColor().set(165, 184, 55);
		line.getLineAttributes().getColor().set(165, 184, 55);
	}
}
*/
/*
function beforeDrawMarkerRange(axis, range, scriptContext) {
	range.getLabel().getCaption().getColor().set(225, 104, 105);
}
*/
/*
function beforeDrawSeries(series, renderer, scriptContext) {
	importPackage(Packages.org.eclipse.birt.chart.model.component.impl);
	series.setCurveFitting(CurveFittingImpl.create());
	series.getLabel().getCaption().getColor().set(12, 232, 182);
}
*/
/*
function beforeDrawSeriesTitle(series, label, scriptContext) {
	label.setVisible(true);
	label.getCaption().setValue('Cities');
	label.getCaption().getColor().set(222, 32, 182);
	series.getLabel().getCaption().getColor().set(12, 232, 182);
}
*/
/*
function beforeDrawBlock(block, scriptContext) {
	importPackage(Packages.org.eclipse.birt.chart.model.attribute.impl);
	if (block.isLegend()) {
		block.getOutline().setVisible(true);
		block.getOutline().getColor().set(21, 244, 231);
	}

	else if (block.isPlot()) {
		block.getOutline().setVisible(true);
		block.getOutline().getColor().set(244, 21, 231);
	}

	else if (block.isTitle()) {
		block.getOutline().setVisible(true);
		block.setBackground(ColorDefinitionImpl.CREAM());
		block.getOutline().getColor().set(0, 0, 0);
	}
}
*/
/*
function beforeDrawLegendEntry(label, scriptContext) {
	label.getCaption().getColor().set(35, 184, 245);
	label.getCaption().getFont().setBold(true);
	label.getCaption().getFont().setItalic(true);
	label.getOutline().setVisible(true);
	label.getOutline().getColor().set(177, 12, 187);
}
*/