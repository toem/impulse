package net.sourceforge.jseditor.editors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;

import de.toem.basics.core.Utils;

public class JSContentFormatter implements IContentFormatter {

    public static String format(String text) {
        try {
            String script = Utils.readStringFromInputStream(JSContentFormatter.class.getResourceAsStream("JSBeautify.js"), null, true);
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            engine.put("text", text);
            Object o = engine.eval(script);
            if (o instanceof String)
                return (String) o;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void format(IDocument document, IRegion region) {
        
        String text = document.get();
        text = JSPsoidoCode.commentPsoidoCode(text) ;      
        String formatted = format(text);
        if (formatted != null){
            formatted = JSPsoidoCode.uncommentPsoidoCode(formatted) ; 
            document.set(formatted);
        }
    }

    @Override
    public IFormattingStrategy getFormattingStrategy(String contentType) {
        // TODO Auto-generated method stub
        return null;
    }

}
