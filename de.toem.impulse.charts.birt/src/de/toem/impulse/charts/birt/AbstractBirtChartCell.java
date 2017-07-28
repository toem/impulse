package de.toem.impulse.charts.birt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.birt.chart.api.ChartEngine;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.ColorDefinition;
import org.eclipse.birt.chart.model.attribute.FontDefinition;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.attribute.Text;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.data.DataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.layout.TitleBlock;
import org.eclipse.birt.core.framework.PlatformConfig;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.controller.base.ComboSelectController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.controller.source.PropertySource;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.DialogToolkit;
import de.toem.impulse.axis.IDomainAxis;
import de.toem.impulse.cells.charts.AbstractChartCell;
import de.toem.impulse.paint.IPaintable;
import de.toem.impulse.paint.IPlannable;
import de.toem.impulse.paint.ITheme;
import de.toem.impulse.samples.IReadableMembers;
import de.toem.impulse.samples.IReadableSamples;
import de.toem.impulse.samples.ISamplesLegend;
import de.toem.impulse.values.MemberDescriptor;
import de.toem.impulse.values.StructMember;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.properties.PropertyModel;
import de.toem.toolkits.text.MultilineText;

public abstract class AbstractBirtChartCell extends AbstractChartCell {

    public final static String[] dimensionOptions = new String[] { "2D", "2D with Depth"/* , "3D" */ };
    public final static int DIMENSION_2D = 0;
    public final static int DIMENSION_2D_DEPTH = 1;
    public final static int DIMENSION_3D = 2;
    public int dimensions = DIMENSION_2D_DEPTH;
    public final static String[] titleOptions = new String[] { "No title", "Description", "Config name", "Config description" };
    public final static int TITLE_NO_TITLE = 0;
    public final static int TITLE_DESCRIPTION = 1;
    public final static int TITLE_CONFIG_NAME = 2;
    public final static int TITLE_CONFIG_DESCRIPTION = 3;
    public int showTitle = TITLE_CONFIG_DESCRIPTION;

    public boolean log10 = true;
    public boolean transposed = false;
    public boolean showLegend = true;
    public boolean enableScript = false;
    public boolean stacked = false;
    public String script;

    public int maxCategories = 20;
    public int maxSeries = 10;

    transient protected ColorDefinition background;
    transient protected ColorDefinition color;
    transient protected ColorDefinition textColor;
    transient protected ColorDefinition scaleColor;
    transient protected ColorDefinition plotBackground;
    transient protected String title;
    transient protected Map<String, DataSet> dataSets;

    @Override
    public IPropertyModel getPropertyModel(boolean configured) {
        IPropertyModel model = new PropertyModel() {
            public IControlProvider getControls() {
                return new AbstractControlProvider() {

                    @Override
                    protected boolean fillThis() {

                        tlk().addText(container(), new TextController(editor(), new PropertySource("members")) {

                        }, cols(), DialogToolkit.LABEL | SWT.BORDER, "Members:");

                        tlk().addText(container(), new TextController(editor(), new PropertySource("samples")) {

                        }, cols(), DialogToolkit.LABEL | SWT.BORDER, "Samples:");

                        tlk().addCombo(container(), new ComboSelectController(editor(), new PropertySource("categorize"), new String[] { "Members", "Samples" }, new String[] { "Members", "Samples" }),
                                cols(), DialogToolkit.LABEL|SWT.READ_ONLY, "Categorize:");

                        return true;
                    }
                };

            }
        }.add("members", "", "Members").add("samples", "0-5,6", "Samples").add("categorize", "Members", "Categorize", new String[] { "Members", "Samples" });
        if (configured)
            model.setValue(parameters);
        return model;
    }

    @Override
    public IPaintable createChart(final IPlannable plannable, IDomainAxis axis, final ITheme theme, final IPropertyModel parameters) {
        try {

            return new IPaintable() {

                @Override
                public void paint(GC gc, int x, int y, int width, int height, Color color, Color background) {

                    try {

                        // parameters
                        boolean categoriesMembers = "Members".equals(parameters.getVal("categorize"));
                        List<Integer> samples = new ArrayList<Integer>();
                        for (String e : (parameters.getVal("samples") != null ? parameters.getVal("samples") : "").split(",")) {
                            e = e.trim();
                            if (!Utils.isEmpty(e)) {
                                int idx = e.indexOf("-");
                                if (idx >= 0) {
                                    int v1 = idx > 0 ? Utils.parseInt(e.substring(0, idx), -1) : 0;
                                    int v2 = idx < e.length() - 1 ? Utils.parseInt(e.substring(idx + 1), -1) :  (categoriesMembers ? maxSeries : maxCategories);
                                    if (v1 >= 0 && v2 >= 0)
                                        for (int v = v1; v <= v2; v++)
                                            samples.add(v);
                                } else {
                                    int v = Utils.parseInt(e, -1);
                                    if (v >= 0)
                                        samples.add(v);
                                }
                            }
                        }
                        List<String> members = new ArrayList<String>();
                        for (String m : (parameters.getVal("members") != null ? parameters.getVal("members") : "").split(",")) {
                            m = m.trim();
                            if (!Utils.isEmpty(m))
                                members.add(m);
                        }
                        
                        // title
                        if (showTitle == TITLE_DESCRIPTION)
                            title = description;
                        else if (showTitle == TITLE_CONFIG_NAME)
                            title = plannable.getText();
                        else if (showTitle == TITLE_CONFIG_DESCRIPTION)
                            title = plannable.getDescription();

                        // colors
                        AbstractBirtChartCell.this.background = ColorDefinitionImpl.create(background.getRed(), background.getGreen(), background.getBlue());
                        AbstractBirtChartCell.this.color = ColorDefinitionImpl.create(color.getRed(), color.getGreen(), color.getBlue());
                        Color tc = theme.getColor(ITheme.COLOR_SHADE_I6);
                        AbstractBirtChartCell.this.plotBackground = ColorDefinitionImpl.create(tc.getRed(), tc.getGreen(), tc.getBlue());
                        tc = theme.getColor(ITheme.COLOR_SAMPLES_TEXT);
                        AbstractBirtChartCell.this.textColor = ColorDefinitionImpl.create(tc.getRed(), tc.getGreen(), tc.getBlue());
                        tc = theme.getColor(ITheme.COLOR_VERT_SCALE_TEXT);
                        AbstractBirtChartCell.this.scaleColor = ColorDefinitionImpl.create(tc.getRed(), tc.getGreen(), tc.getBlue());

                        // Data Set
                        IReadableSamples readable = plannable.getReadable();
                        dataSets = new LinkedHashMap<String, DataSet>();
                        if (readable.getLegend() != null && readable.getLegend().hasMembers()) {
                            if (categoriesMembers) {

                                // categories
                                ISamplesLegend legend = readable.getLegend();
                                List<String> categories = new ArrayList<String>();
                                for (MemberDescriptor member : legend.getMembers()) {
                                    if ((member.getType() == StructMember.STRUCT_TYPE_FLOAT || member.getType() == StructMember.STRUCT_TYPE_INTEGER)
                                            && (members.isEmpty() || members.contains(member.getName())))
                                        categories.add(member.getName());
                                    if (categories.size() >= maxCategories)
                                        break;
                                }
                                dataSets.put("CATEGORIES", TextDataSetImpl.create(categories.toArray(new String[categories.size()])));

                                // series
                                int noOfSeries = 0;
                                for (int i = 0; i < readable.getCount() && noOfSeries < maxSeries; i++) {
                                    if (!samples.isEmpty() && !samples.contains(i))
                                        continue;
                                    double[] values = new double[categories.size()];
                                    IReadableMembers compound = readable.compoundAt(i);
                                    for (int n = 0; n < categories.size(); n++) {
                                        values[n] = compound.doubleValueOf(categories.get(n));
                                    }
                                    String series = String.valueOf(i);
                                    Object member = compound.memberWithContent("LABEL");
                                    if (member != null)
                                        series = compound.stringValueOf(member);
                                    dataSets.put(series, NumberDataSetImpl.create(values));
                                    noOfSeries++;
                                }
                            } else {
                                // categories
                                List<String> categories = new ArrayList<String>();
                                List<Integer> eventIndex = new ArrayList<Integer>();
                                for (int i = 0; i < readable.getCount() && categories.size() < maxCategories; i++) {
                                    if (!samples.isEmpty() && !samples.contains(i))
                                        continue;
                                    IReadableMembers compound = readable.compoundAt(i);
                                    String category = String.valueOf(i);
                                    Object member = compound.memberWithContent("LABEL");
                                    if (member != null)
                                        category = compound.stringValueOf(member);
                                    categories.add(category);
                                    eventIndex.add(i);
                                }
                                dataSets.put("CATEGORIES", TextDataSetImpl.create(categories.toArray(new String[categories.size()])));

                                // series
                                Map<String, double[]> series = new HashMap<String, double[]>();
                                ISamplesLegend legend = readable.getLegend();
                                for (MemberDescriptor member : legend.getMembers()) {
                                    if ((member.getType() == StructMember.STRUCT_TYPE_FLOAT || member.getType() == StructMember.STRUCT_TYPE_INTEGER)
                                            && (members.isEmpty() || members.contains(member.getName())))
                                        series.put(member.getName(), new double[categories.size()]);
                                    if (series.size() >= maxSeries)
                                        break;
                                }

                                // fill values
                                int i = 0;
                                for (Integer idx : eventIndex) {
                                    IReadableMembers compound = readable.compoundAt(idx);
                                    for (String name : series.keySet()) {
                                        series.get(name)[i] = compound.doubleValueOf(name);
                                    }
                                    i++;
                                }

                                // create series
                                for (String name : series.keySet()) {
                                    dataSets.put(name, NumberDataSetImpl.create(series.get(name)));
                                }
                            }
                        } else if (!categoriesMembers) {
                            List<String> categories = new ArrayList<String>();
                            List<Integer> eventIndex = new ArrayList<Integer>();
                            for (int n = 0; n < readable.getCount() && categories.size() < maxCategories; n++) {
                                if (!samples.isEmpty() && !samples.contains(n))
                                    continue;
                                categories.add(readable.positionAt(n).toString());
                                eventIndex.add(n);
                            }
                            dataSets.put("CATEGORIES", TextDataSetImpl.create(categories.toArray(new String[categories.size()])));
                            double[] values = new double[categories.size()];
                            for (int n = 0; n < categories.size(); n++) {
                                values[n] = readable.doubleValueAt(eventIndex.get(n));
                            }
                            dataSets.put("0", NumberDataSetImpl.create(values));
                        }

                        // chart
                        PlatformConfig config = new PlatformConfig();
                        IDeviceRenderer idr = ChartEngine.instance(config).getRenderer("dv.SWT");//$NON-NLS-1$
                        Chart cm = createBirtChart(theme);

                        // render
                        Rectangle d = new Rectangle(x, y, width, height);
                        Image imgChart = new Image(gc.getDevice(), d);
                        GC gcImage = new GC(imgChart);
                        idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gcImage);

                        Bounds bo = BoundsImpl.create(0, 0, d.width, d.height);
                        bo.scale(72d / idr.getDisplayServer().getDpiResolution());

                        Generator gr = Generator.instance();
                        GeneratedChartState gcs = gr.build(idr.getDisplayServer(), cm, bo, null, null,
                                new StyleProcessor(theme, color, background, theme.getColor(ITheme.COLOR_SCALE_TEXT)));

                        gr.render(idr, gcs);
                        gc.drawImage(imgChart, d.x, d.y);

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };
        } catch (Throwable e) {
        }
        return null;
    }

    protected abstract Chart createBirtChart(ITheme theme);

    protected void initDefaults(Chart chart, LegendItemType legendType, ITheme theme) {

        // script
        chart.setScript(script != null && enableScript ? MultilineText.toAscii(script) : null);

        // Plot
        chart.getBlock().setBackground(background.copyInstance());
        chart.getBlock().getOutline().setVisible(false);
        Plot p = chart.getPlot();
        p.getClientArea().setBackground(background.copyInstance());
        p.getOutline().setVisible(false);

        // Title
        TitleBlock tb = chart.getTitle();
        tb.getLabel().getCaption().setValue(title != null ? title : "");
        applyFont(tb.getLabel().getCaption(), true, theme);
        tb.setVisible(title != null);

        // Legend
        Legend lg = chart.getLegend();
        applyFont(lg.getText(), false, theme);
        lg.setItemType(legendType);
        lg.setVisible(showLegend);

        // dimensions / orientation
        if (dimensions == 0)
            chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
        else if (dimensions == 1)
            chart.setDimension(ChartDimension.TWO_DIMENSIONAL_WITH_DEPTH_LITERAL);
        else if (dimensions == 2)
            chart.setDimension(ChartDimension.THREE_DIMENSIONAL_LITERAL);
        chart.setSeriesThickness(10);
        if (chart instanceof ChartWithAxes)
            ((ChartWithAxes) chart).setTransposed(transposed);
    }

    protected ColorDefinition colorDefinitionOf(int color) {
        return ColorDefinitionImpl.create((color >>> 16) & 0xff, (color >>> 8) & 0xff, color & 0xff);
    }

    protected void applyFont(Text text, boolean title, ITheme theme) {
        FontDefinition fontDefinition = text.getFont();
        float fontHeight = theme.getFont().getFontData()[0].getHeight();
        String name = theme.getFont().getFontData()[0].getName();
        fontDefinition.setSize(fontHeight);
        fontDefinition.setName(name);
        text.setColor(title ? textColor.copyInstance() : scaleColor.copyInstance());
    }
}
