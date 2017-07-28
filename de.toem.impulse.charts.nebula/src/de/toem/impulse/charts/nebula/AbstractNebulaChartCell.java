package de.toem.impulse.charts.nebula;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import de.toem.impulse.axis.IDomainAxis;
import de.toem.impulse.cells.charts.AbstractChartCell;
import de.toem.impulse.paint.IPaintable;
import de.toem.impulse.paint.IPlannable;
import de.toem.impulse.paint.ITheme;
import de.toem.pattern.properties.IPropertyModel;
import de.toem.pattern.properties.PropertyModel;

public abstract class AbstractNebulaChartCell extends AbstractChartCell {


    transient protected String title;
    transient protected IPlannable plannable;
    transient protected IDomainAxis axis;
    transient protected ITheme theme;
    
    @Override
    public IPaintable createChart(final IPlannable plannable, IDomainAxis axis, final ITheme theme, final IPropertyModel parameters) {
        this.plannable = plannable;
        this.axis = axis;
        this.theme = theme;
        try {
            this.theme = theme;

            return new IPaintable() {

                @Override
                public void paint(GC gc, int x, int y, int width, int height, Color color, Color background) {

                    try {
                        gc.setBackground(background);
                        gc.setForeground(color);
                        gc.fillRectangle(x,y,width,height);
                        
                        Figure chart = createChart(gc, x, y, width, height, color, background, parameters);
                        if (chart != null) {
                            SWTGraphics graphics = new SWTGraphics(gc);
                            chart.setBounds(new Rectangle(x,y,width,height));
                            chart.setBackgroundColor(background);
                            chart.setForegroundColor(theme.getColor(ITheme.COLOR_VERT_SCALE_TEXT));                         
                            chart.setFont(gc.getFont());
                            graphics.setForegroundColor(color);
                            graphics.setBackgroundColor(background);
                            chart.validate();
                            chart.paint(graphics);
                        }

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };
        } catch (Throwable e) {
        }
        return null;
    }

    public Figure createChart(GC gc, int x, int y, int width, int height, Color color, Color background, IPropertyModel parameters) {
        return null;
    }

}
