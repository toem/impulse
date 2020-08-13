package de.toem.impulse.extension.nebula;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import de.toem.basics.core.Assert;
import de.toem.basics.core.Utils;
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
import de.toem.impulse.samples.IReadableSamples;
import de.toem.impulse.scripting.DefaultScriptContextProvider;
import de.toem.impulse.scripting.IScriptContextProvider;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.threading.Actives;
import de.toem.pattern.threading.IExecutable;
import de.toem.pattern.threading.IProgress;

public abstract class AbstractNebulaChartCell extends AbstractChartCell implements IExternalPlanner<IPlotItem>, IScriptContextProvider {

    public String script;
    public String scriptLanguage;

    @Override
    public void provideToScriptContext(IScriptContextInterface context) {
        if (Utils.equals(context.getContextId(), "script")) {

            DefaultScriptContextProvider.provideDefaultScriptContext(context, true, false, false, true, false, true);

            context.addSymbol("gc", false, GC.class);
            context.addSymbol("x", false, Integer.class);
            context.addSymbol("y", false, Integer.class);
            context.addSymbol("width", false, Integer.class);
            context.addSymbol("height", false, Integer.class);
            context.addSymbol("color", false, Color.class);
            context.addSymbol("background", false, Color.class);
            context.addSymbol("plannable", false, IPlotItem.class);
            context.addSymbol("axis", false, IDomainAxis.class);
            context.addSymbol("theme", false, ITheme.class);
            context.addSymbol("readable", false, IReadableSamples.class);
            context.addSymbol("graph", false, XYGraph.class);

            context.setScript(script, scriptLanguage);
        }
    }

    @Override
    public void plan(ISinglePaintPlanner<IPlotItem> planner, IProgress progress, IPaintPlanGenerator generator, IPropertyModel parameters, IDomainAxis axis,
            org.eclipse.swt.graphics.Rectangle childArea, boolean extend) {

        if (extend)
            return;
        byte[] image = create(progress, (IPlotItem) generator.getItem(), generator.getAxis(), generator.getTheme(), parameters,
                childArea != null ? childArea : generator.getArea());
        generator.setScheme(IPlan.SCHEME_APPLICABALE_AREA_MATCH | IPlan.SCHEME_APPLICABALE_SAMPLES_MATCH | IPlan.SCHEME_APPLICABALE_PAINTSTYLE_MATCH | IPlan.SCHEME_APPLICABALE_COLOR_MATCH);
        // Utils.log("creates birt",image);
        if (image != null) {
            generator.add(IPaint.PAINT_CHART, generator.getArea().x, 0, image);
        }

    }

    @Override
    public int getPriority() {
        return IPaintPlanner.PRIORITY_MID;
    }

    public byte[] create(IProgress progress, IPlotItem plannable, IDomainAxis axis, final ITheme theme, final IPropertyModel parameters, Rectangle area) {

        byte[][] result = new byte[1][];

        Actives.runInMain(new IExecutable() {

            @Override
            public void execute(IProgress p) {

                Color color = (Color) plannable.getColor();
                Font font = (Font) theme.getFont();
                Color background = (Color) theme.getColor(ITheme.COLOR_PLOT_BACKGROUND);
                Figure chart = createChart(p, null, plannable, null, theme, parameters, area.x, area.y, area.width, area.height, font, (Color) color,
                        (Color) background);
                if (chart != null) {
                    chart.setBounds(new org.eclipse.draw2d.geometry.Rectangle(0, 0, area.width, area.height));
                    chart.setBackgroundColor((Color) background);
                    chart.setForegroundColor((Color) theme.getColor(ITheme.COLOR_VERT_SCALE_TEXT));
                    chart.setFont(font);

                    if (true) {
                        PainterGraphics graphics = new PainterGraphics(area.width, area.height);
                        graphics.setAdvanced(true);
                        graphics.setAntialias(SWT.ON);
                        graphics.setForegroundColor((Color) color);
                        graphics.setBackgroundColor((Color) background);
                        graphics.fillRectangle(0, 0, area.width, area.height);
                        chart.validate();
                        chart.paint(graphics);

                        result[0] = graphics.getBytes();
                    } else {
                        Image image = new Image(Display.getCurrent(), area.width, area.height);

                        GC gc = new GC(image);
                        SWTGraphics graphics = new SWTGraphics(gc);

                        graphics.setForegroundColor((Color) color);
                        graphics.setBackgroundColor((Color) background);
                        graphics.fillRectangle(0, 0, area.width, area.height);
                        chart.validate();
                        chart.paint(graphics);

                        ImageData imageData = image.getImageData();
                        try {
                            ImageLoader imageLoader = new ImageLoader();
                            imageLoader.data = new ImageData[] { imageData };
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            imageLoader.save(out, SWT.IMAGE_PNG);
                            out.close();
                            result[0] = out.toByteArray();
                        } catch (IOException e) {
                        }

                        gc.dispose();
                        image.dispose();
                    }
                }
            }
        }, true);

        return result[0];

    }

    public Figure createChart(IProgress progress, GC gc, IPlotItem plannable, IDomainAxis axis, ITheme theme, IPropertyModel parameters, int x, int y,
            int width, int height, Font font, Color color, Color background) {
        return null;
    }

}
