package de.toem.impulse.ports.jdt;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;

public interface IBreakpointListener {

    void added(IBreakpoint bp);

    void removed(IBreakpoint bp);

    static final int MODE_DISABLED = 0;
    static final int MODE_ENABLED = 1;
    static final int MODE_EXPRESSIONS = 2;
    static final int MODE_SUSPEND = 4;
    int getMode(IBreakpoint bp);
    String getExpressionFilter(IBreakpoint bp);   
    public void hit(IThread thread, IBreakpoint bp, Object source, IValue value,long time) ;

    

    
}
