package net.sourceforge.jseditor.editors;

public class JSPsoidoCode {

    public static String removePsoidoCode(String text){
        if (text == null)
            return null;
        return text.replaceAll("\\<\\:([^:>]*)\\:\\>", "");
    }
    
    public static String commentPsoidoCode(String text){
        if (text == null)
            return null;
        text = text.replaceAll("\\<\\:", "/*<:");
        text = text.replaceAll("\\:\\>", ":>*/");  
        return text;
    }
  
    public static String uncommentPsoidoCode(String text){
        if (text == null)
            return null;
        text = text.replaceAll("\\/\\*\\<\\:", "<:");
        text = text.replaceAll("\\:\\>\\*\\/", ":>");
        return text;
    }
}
