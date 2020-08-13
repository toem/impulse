// painter: 
// context: 
// item: 
// itemArea: 

function onPrePaint(painter, context, item, itemArea ) {
	//painter.drawLine(0, 0, 300, 300);
}

function onPostPaint(painter, context, item, itemArea ) {
	//painter.drawLine(100, 100, 300, 300);
}

function onDrawChart(iter, painter, context, item, image,  x,  y,  width,  height,  x2,  y2,  width2,  height2,adaptColors,p5,p6,p7,p8) {
	
	// active states
	var activeStates = null;
	if (plan.getStatus() != null){
		activeStates = plan.getStatus().split(";");
		for (var n=0;n<	activeStates.length;n++){
			activeStates[n] = activeStates[n].trim();
			//console.println(activeStates[n]);
		}
			
	}
	
	// iterate over plan
	while (iter.hasNext()) {
	    var paint = iter.next();
	    
	    // annotation
	    if ((paint & IPaint.PAINT_BASE_MASK) == IPaint.PAINT_ANNOTATION) {
	        //console.println(plan.getStatus() != null ? plan.getStatus() :"");
	        if (iter.hasValue(java.lang.String.class)) {
	            var value = iter.value(java.lang.String.class);
	            var xa = width2 * iter.p() / width + x2;
	            var ya = height2 * iter.p2() / height + y2;
	            var wa = width2 * Math.abs(iter.p3()) / width;
	            var ha = height2 * Math.abs(iter.p4()) / height;
	            var r = width2 * 15 / width;
	            context.addData(xa, ya, wa, ha, value);
	            // Utils.log(iter.p(), iter.p2(), iter.p3(), iter.p4(), iter.value(java.lang.String.class));
	            // Utils.log(xa, ya, wa, ha);
				//console.println(value != null ? value :"");
				
				// is state avtive ?
				var isActive = false;
				if (activeStates != null)
					for (var n=0;n<	activeStates.length;n++){
						if (value.endsWith(activeStates[n]))
							isActive = true;
					}
	            if (isActive) {
	            	// console.println(xa+" "+ya+" "+ wa + " " + ha);
	                if (iter.p4() < 0) {
	                    //painter.setBackground(context.getTheme().getColor(ITheme.COLOR_SAMPLES_BACKGROUND));
	                    painter.setBackground(item.getColor());
	                    painter.setAlpha(32);
	                    painter.fillRoundRectangle(xa + 2, ya + 2, wa - 4, ha - 4, r, r);
	                    painter.setAlpha(255);
	                } else {
	                    painter.setBackground(item.getColor());
	                    painter.setAlpha(128);
	                    painter.fillRoundRectangle(xa + 2, ya + 2, wa - 4, ha - 4, r, r);
	                    painter.setAlpha(255);
	                }
	                if (iter.p4() < 0) {
	                    // painter.setBackground(context.getTheme().getColor(ITheme.COLOR_SAMPLES_BACKGROUND));
	                    // painter.fillRectangle(xa+6, ya+6, wa-12, ha-6);
	                }
	            }
	            // painter.drawRectangle(xa, ya, wa, ha);
	        }
	    }	
	}
	painter.drawImage(image, 0, 0, width, height, x2, y2, width2, height2,adaptColors);
	return  true;
}