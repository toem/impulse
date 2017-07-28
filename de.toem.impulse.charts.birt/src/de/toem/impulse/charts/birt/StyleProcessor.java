/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package de.toem.impulse.charts.birt;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.ColorDefinition;
import org.eclipse.birt.chart.model.attribute.FontDefinition;
import org.eclipse.birt.chart.model.attribute.HorizontalAlignment;
import org.eclipse.birt.chart.model.attribute.StyledComponent;
import org.eclipse.birt.chart.model.attribute.TextAlignment;
import org.eclipse.birt.chart.model.attribute.VerticalAlignment;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.FontDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.InsetsImpl;
import org.eclipse.birt.chart.model.attribute.impl.TextAlignmentImpl;
import org.eclipse.birt.chart.style.BaseStyleProcessor;
import org.eclipse.birt.chart.style.IStyle;
import org.eclipse.birt.chart.style.SimpleStyle;
import org.eclipse.swt.graphics.Color;

import de.toem.impulse.paint.ITheme;

/**
 * SimpleProcessor
 */
public final class StyleProcessor extends BaseStyleProcessor
{

	private  SimpleStyle sstyle;




	StyleProcessor(ITheme theme, Color color, Color background , Color text)
	{
		super( );
		TextAlignment ta = TextAlignmentImpl.create( );
        ta.setHorizontalAlignment( HorizontalAlignment.RIGHT_LITERAL );
        ta.setVerticalAlignment( VerticalAlignment.BOTTOM_LITERAL );
        
        float fontHeight = theme.getFont().getFontData()[0].getHeight();
        String name = theme.getFont().getFontData()[0].getName();
        FontDefinition font = FontDefinitionImpl.create( name, 
                fontHeight, false, false, false, false, false,0, ta );

        
        ColorDefinition textColor = ColorDefinitionImpl.create(text.getRed(),text.getGreen(),text.getBlue());     

        sstyle = new SimpleStyle( font,
                textColor,
                null,
                null,
                InsetsImpl.create( 1.0, 1.0, 1.0, 1.0 ) );
	}


	public IStyle getStyle( Chart model, StyledComponent name )
	{
		return sstyle.copy( );
	}
}