package de.toem.impulse.extension.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.toem.basics.core.Utils;

public class DocConverter {

    public static void main(String[] args) {
 
        StringBuilder overview = new StringBuilder();
        StringBuilder details = new StringBuilder();
       String header =  Utils.readStringFromFile("flux/flux.h",null);
       Pattern p1 = Pattern.compile("/\\*\\*[^;]*;|// (##?[^/#]+)\\/");
       Matcher m1 = p1.matcher(header);
       
       while(m1.find()){
           String item = m1.group(0);
           //Utils.log(item);
           
           if (item.startsWith("// #")){
               if (item.length()>4 && item.charAt(4)=='#'){
                   details.append("<h4>"+m1.group(1).substring(2).trim()+"</h4>\n");   
                   overview.append("<p>"+m1.group(1).substring(2).trim()+"</p>\n");   
               }else{
                   details.append("<h3>"+m1.group(1).substring(1).trim()+"</h3>\n");   
                   overview.append("<p>"+m1.group(1).substring(1).trim()+"</p>\n");  
               }
           }
           
           
           
           
           Pattern p2 = Pattern.compile("\\*\\/(flx\\S+|#define|void)\\s+(\\w+)\\(");
           Matcher m2 = p2.matcher(item);
           if (!m2.find())
               continue;
           String type = m2.group(1);
           String name = m2.group(2);
//           Utils.log(type,name);
           Pattern p2g = Pattern.compile("\\*\\/(flx\\S+.*|#define.*|void.*);");
           Matcher m2g = p2g.matcher(item);
           if (!m2g.find())
               continue;
           String declaration = m2g.group(1);
//           Utils.log(declaration);
           
           Pattern p3 = Pattern.compile("\\* ([^\\@\\*][^\\*]+)\\*");
           Matcher m3 = p3.matcher(item);
           String text = "";
           while (m3.find())
               text +=m3.group(1);
           text = text.trim();
//           Utils.log(text);
           
           Pattern p4 = Pattern.compile("@param([^\\*]+)\\*");
           Matcher m4 = p4.matcher(item);
           List<String> paramters  =  new ArrayList<String>();
           while (m4.find()){
               paramters.add(m4.group(1).trim());
//               Utils.log(m4.group(1));               
           }

           Pattern p5 = Pattern.compile("@return([^\\*]+)\\*");
           Matcher m5 = p5.matcher(item);
           String ret=null;
           if (m5.find()){
               ret = m5.group(1).trim();
//               Utils.log(ret);               
           }
           
           overview.append("<li><a href=\"#"+name+"\">"+type+ " "+name+"</a></li>\n");
           
           details.append("<a id=\""+name +"\"></a>\n");          
           details.append("<b>"+name+"</b>\n");
           details.append("<p>"+text+"</p>\n");
           details.append("<ul  class=\"uk-list uk-list-striped\">\n");  
           for (String p:paramters)
               details.append("<li>"+p+"</li>\n");   
           details.append("<li> <b>"+ret+"</b></li>\n");            
           details.append("</ul>\n");  
           details.append("<pre>"+declaration+"</pre><hr/>\n");
       }
       String result = "<div class=\"uk-alert uk-alert-success\"><p>Overview</p></div>";
       result += "<ul>\n";
       result += overview.toString();
       result += "</ul>\n";  
       result += "<div class=\"uk-alert uk-alert-success\"><p>Details</p></div>";
       
       result +=details.toString();
       Utils.write("flux/flux.html",result);
       System.out.println(result);
    }

}
