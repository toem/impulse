package net.sourceforge.jseditor.editors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JSSyntaxErrorParser {
    public static ScriptException parse(String text) {
        try {
            text = JSPsoidoCode.removePsoidoCode(text);
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            engine.eval("function checkSynntax(){" + text + "\n\n}");
        } catch (Throwable e) {
            if (e instanceof ScriptException)
                return (ScriptException) e;
        }
        return null;
    }

}