package de.toem.impulse.extension.birt;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.controller.base.ComboSelectController;
import de.toem.eclipse.toolkits.controller.base.TextController;
import de.toem.eclipse.toolkits.controller.source.PropertySource;
import de.toem.eclipse.toolkits.tlk.AbstractControlProvider;
import de.toem.eclipse.toolkits.tlk.DialogToolkit;
import de.toem.eclipse.toolkits.tlk.IPaintable;
import de.toem.eclipse.toolkits.tlk.ITlkPainter;
import de.toem.eclipse.toolkits.tlk.TLK;
import de.toem.impulse.axis.IDomainAxis;
import de.toem.impulse.cells.charts.AbstractChartCell;
import de.toem.impulse.paint.IPaint;
import de.toem.impulse.paint.IPlotItem;
import de.toem.impulse.paint.ITheme;
import de.toem.impulse.paint.plan.IPlan;
import de.toem.impulse.paint.plan.IPlan.IExternalPlanner;
import de.toem.impulse.paint.plan.IPlan.IPaintPlanGenerator;
import de.toem.impulse.paint.plan.IPlan.IPaintPlanner;
import de.toem.impulse.paint.plan.IPlan.ISinglePaintPlanner;
import de.toem.impulse.samples.IMemberDescriptor;
import de.toem.impulse.samples.IReadableMembers;
import de.toem.impulse.samples.IReadableSamples;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.values.StructMember;
import de.toem.pattern.controls.IControlProvider;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.properties.PropertyModel;
import de.toem.pattern.threading.IProgress;
import de.toem.toolkits.text.MultilineText;

public abstract class AbstractBirtChartCell extends AbstractChartCell implements IExternalPlanner<IPlotItem> {

    public final static String[] dimensionOptions = new String[] { "2D", "2D with Depth"/* , "3D" */ };
    public final static int DIMENSION_2D = 0;
    public final static int DIMENSION_2D_DEPTH = 1;
    public final static int DIMENSION_3D = 2;
    public int dimensions = DIMENSION_2D_DEPTH;
    public final static String[] titleOptions = new String[] { "No title", "Description", "Plot name", "Plot description" };
    public final static int TITLE_NO_TITLE = 0;
    public final static int TITLE_DESCRIPTION = 1;
    public final static int TITLE_CONFIG_NAME = 2;
    public final static int TITLE_CONFIG_DESCRIPTION = 3;
    public int showTitle = TITLE_CONFIG_DESCRIPTION;

    public boolean log10 = true;
    public boolean transposed = false;
    public boolean showLegend = true;
    public boolean enableScript = false;
    public boolean stacked = false; // bar
    public boolean inner = false; // pie
    public boolean explode = false; // pie

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

                        tlk().addText(container(), new TextController(editor(), new PropertySource("members")).setTooltip("Identify the structure or array elements you want to display. Use a comma-separated list of all members, e.g.'X,Y'\""), cols(),
                                DialogToolkit.LABEL | TLK.BORDER, "Members:");

                        tlk().addText(container(), new TextController(editor(), new PropertySource("samples")) .setTooltip("Identify the samples you want to display. Use a comma-separated list of all sample indices, for example, '0,1,5-8'"), cols(), DialogToolkit.LABEL | TLK.BORDER, "Samples:");

                        tlk().addCombo(container(), new ComboSelectController(editor(), new PropertySource("categorize"), new String[] { "Members", "Samples" },
                                new String[] { "Members", "Samples" }).setTooltip("Choose if you want to categorize the members or the samples"), cols(), DialogToolkit.LABEL | TLK.READ_ONLY, "Categorize:");

                        return true;
                    }
                };

            }
        }.add("members", "", "Members", null,
                "Identify the structure or array elements you want to display. Use a comma-separated list of all members, e.g.'X,Y'")
                .add("samples", "0-5,6", "Samples", null,
                        "Identify the samples you want to display. Use a comma-separated list of all sample indices, for example, '0,1,5-8'.")
                .add("categorize", "Members", "Categorize", new String[] { "Members", "Samples" },
                        "Choose if you want to categorize the members or the samples");
        if (configured)
            model.setTotal(parameters);
        return model;
    }

    @Override
    public void plan(ISinglePaintPlanner<IPlotItem> planner, IProgress progress, IPaintPlanGenerator generator, IPropertyModel parameters, IDomainAxis axis,
            Rectangle childArea, boolean extend) {
        if (extend)
            return;
        byte[] image = create(progress, (IPlotItem) generator.getItem(), generator.getAxis(), generator.getTheme(), parameters,
                childArea != null ? childArea : generator.getArea());
        generator.setScheme(IPlan.SCHEME_APPLICABALE_AREA_MATCH | IPlan.SCHEME_APPLICABALE_SAMPLES_MATCH | IPlan.SCHEME_APPLICABALE_PAINTSTYLE_MATCH);
        // Utils.log("creates birt",image);
        if (image != null) {
            generator.add(IPaint.PAINT_CHART, generator.getArea().x, 0, image);
        }

    }

    @Override
    public int getPriority() {
        return IPaintPlanner.PRIORITY_LOW;
    }
    
    public byte[] create(IProgress progress, IPlotItem plannable, IDomainAxis axis, final ITheme theme, final IPropertyModel parameters, Rectangle area) {

        try {

            Color color = (Color) plannable.getColor();

            // parameters
            boolean categoriesMembers = "Members".equals(parameters.get("categorize"));
            List<Integer> samples = new ArrayList<Integer>();
            for (String e : (parameters.get("samples") != null ? parameters.get("samples") : "").split(",")) {
                e = e.trim();
                if (!Utils.isEmpty(e)) {
                    int idx = e.indexOf("-");
                    if (idx >= 0) {
                        int v1 = idx > 0 ? Utils.parseInt(e.substring(0, idx), -1) : 0;
                        int v2 = idx < e.length() - 1 ? Utils.parseInt(e.substring(idx + 1), -1) : (categoriesMembers ? maxSeries : maxCategories);
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
            for (String m : (parameters.get("members") != null ? parameters.get("members") : "").split(",")) {
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
            AbstractBirtChartCell.this.background = ColorDefinitionImpl.create(0, 0, 0, 0);
            AbstractBirtChartCell.this.color = ColorDefinitionImpl.create(((Color) color).getRed(), ((Color) color).getGreen(), ((Color) color).getBlue());
            Color tc = (Color) theme.getColor(ITheme.COLOR_SHADE_I6);
            AbstractBirtChartCell.this.plotBackground = ColorDefinitionImpl.create(tc.getRed(), tc.getGreen(), tc.getBlue());
            tc = (Color) theme.getColor(ITheme.COLOR_PLOT_TEXT);
            AbstractBirtChartCell.this.textColor = ColorDefinitionImpl.create(tc.getRed(), tc.getGreen(), tc.getBlue());
            tc = (Color) theme.getColor(ITheme.COLOR_VERT_SCALE_TEXT);
            AbstractBirtChartCell.this.scaleColor = ColorDefinitionImpl.create(tc.getRed(), tc.getGreen(), tc.getBlue());

            // Data Set
            IReadableSamples readable = plannable.getSamples();
            dataSets = new LinkedHashMap<String, DataSet>();
            List<IMemberDescriptor> memberDescriptors = readable.getMemberDescriptors();
            if (!Utils.isEmpty(memberDescriptors)) {
                if (categoriesMembers) {

                    // categories
                    List<String> categories = new ArrayList<String>();
                    for (IMemberDescriptor member : memberDescriptors) {
                        if ((member.getType() == StructMember.STRUCT_TYPE_FLOAT || member.getType() == StructMember.STRUCT_TYPE_INTEGER|| member.getType() == StructMember.STRUCT_TYPE_UNKNOWN)
                                && (members.isEmpty() || members.contains(member.getName()) || members.contains(String.valueOf(member.getId()))))
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
                        List<Object> member = compound.membersWithContent(ISample.CONTENT_LABEL);
                        if (member.size() > 0)
                            series = compound.stringValueOf(member.get(0));
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
                        List<Object> member = compound.membersWithContent(ISample.CONTENT_LABEL);
                        if (member.size() > 0)
                            category = compound.stringValueOf(member.get(0));
                        categories.add(category);
                        eventIndex.add(i);
                    }
                    dataSets.put("CATEGORIES", TextDataSetImpl.create(categories.toArray(new String[categories.size()])));

                    // series
                    Map<String, double[]> series = new HashMap<String, double[]>();
                    for (IMemberDescriptor member : memberDescriptors) {
                        if ((member.getType() == StructMember.STRUCT_TYPE_FLOAT || member.getType() == StructMember.STRUCT_TYPE_INTEGER|| member.getType() == StructMember.STRUCT_TYPE_UNKNOWN)
                                && (members.isEmpty() || members.contains(member.getName()) || members.contains(String.valueOf(member.getId()))))
                            series.put(member.getName(), new double[categories.size()]);
                        if (series.size() >= maxSeries)
                            break;
                    }
//                    Utils.log(series.keySet());

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

            PlatformConfig config = new PlatformConfig();

            IDeviceRenderer idr = ChartEngine.instance(config).getRenderer("dv.PNG");

            if (idr == null)
                return null;
            BufferedImage img = new BufferedImage(area.width, area.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) img.getGraphics();
            idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, g2d);
            idr.setProperty(IDeviceRenderer.CACHED_IMAGE, img);

            // render

            // // chart
            // PlatformConfig config = new PlatformConfig();
            // IDeviceRenderer idr = ChartEngine.instance(config).getRenderer("dv.SWT");//$NON-NLS-1$
            Chart cm = createBirtChart(theme);
            //
            // // render
            // Image imgChart = new Image(Display.getDefault(), area);
            // GC gcImage = new GC(imgChart);
            // idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gcImage);

            Bounds bo = BoundsImpl.create(0, 0, area.width, area.height);
            bo.scale(72d / idr.getDisplayServer().getDpiResolution());

            Generator gr = Generator.instance();
            GeneratedChartState gcs = gr.build(idr.getDisplayServer(), cm, bo, null, null,
                    new StyleProcessor(theme, (Color) theme.getColor(ITheme.COLOR_SCALE_TEXT)));

            gr.render(idr, gcs);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            baos.flush();
            return baos.toByteArray();

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IPaintable createChart(final IPlotItem plannable, IDomainAxis axis, final ITheme theme, final IPropertyModel parameters) {
        try {

            return new IPaintable() {

                @Override
                public void paint(ITlkPainter painter, int x, int y, int width, int height, Object color, Object background) {
                    // TODO Auto-generated method stub

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
        else if (dimensions == 2) {
            chart.setDimension(ChartDimension.THREE_DIMENSIONAL_LITERAL);
        }
        chart.setSeriesThickness(10);
        if (chart instanceof ChartWithAxes)
            ((ChartWithAxes) chart).setTransposed(transposed);
    }

    protected ColorDefinition colorDefinitionOf(int color) {
        return ColorDefinitionImpl.create((color >>> 16) & 0xff, (color >>> 8) & 0xff, color & 0xff);
    }

    protected void applyFont(Text text, boolean title, ITheme theme) {
        FontDefinition fontDefinition = text.getFont();
        float fontHeight = theme.getFontData().getHeight();
        String name = theme.getFontData().getName();
        fontDefinition.setSize(fontHeight);
        fontDefinition.setName(name);
        text.setColor(title ? textColor.copyInstance() : scaleColor.copyInstance());
    }
}
