//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.12 um 09:21:15 AM CET 
//


package org.opcfoundation.opcua.binaryschema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für SwitchOperand.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="SwitchOperand"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="Equals"/&gt;
 *     &lt;enumeration value="GreaterThan"/&gt;
 *     &lt;enumeration value="LessThan"/&gt;
 *     &lt;enumeration value="GreaterThanOrEqual"/&gt;
 *     &lt;enumeration value="LessThanOrEqual"/&gt;
 *     &lt;enumeration value="NotEqual"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "SwitchOperand")
@XmlEnum
public enum SwitchOperand {

    @XmlEnumValue("Equals")
    EQUALS("Equals"),
    @XmlEnumValue("GreaterThan")
    GREATER_THAN("GreaterThan"),
    @XmlEnumValue("LessThan")
    LESS_THAN("LessThan"),
    @XmlEnumValue("GreaterThanOrEqual")
    GREATER_THAN_OR_EQUAL("GreaterThanOrEqual"),
    @XmlEnumValue("LessThanOrEqual")
    LESS_THAN_OR_EQUAL("LessThanOrEqual"),
    @XmlEnumValue("NotEqual")
    NOT_EQUAL("NotEqual");
    private final String value;

    SwitchOperand(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SwitchOperand fromValue(String v) {
        for (SwitchOperand c: SwitchOperand.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
