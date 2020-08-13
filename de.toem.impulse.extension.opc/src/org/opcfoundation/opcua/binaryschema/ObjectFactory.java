//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.12 um 09:21:15 AM CET 
//


package org.opcfoundation.opcua.binaryschema;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.opcfoundation.opcua.binaryschema package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.opcfoundation.opcua.binaryschema
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Documentation }
     * 
     */
    public Documentation createDocumentation() {
        return new Documentation();
    }

    /**
     * Create an instance of {@link TypeDictionary }
     * 
     */
    public TypeDictionary createTypeDictionary() {
        return new TypeDictionary();
    }

    /**
     * Create an instance of {@link ImportDirective }
     * 
     */
    public ImportDirective createImportDirective() {
        return new ImportDirective();
    }

    /**
     * Create an instance of {@link OpaqueType }
     * 
     */
    public OpaqueType createOpaqueType() {
        return new OpaqueType();
    }

    /**
     * Create an instance of {@link EnumeratedType }
     * 
     */
    public EnumeratedType createEnumeratedType() {
        return new EnumeratedType();
    }

    /**
     * Create an instance of {@link StructuredType }
     * 
     */
    public StructuredType createStructuredType() {
        return new StructuredType();
    }

    /**
     * Create an instance of {@link TypeDescription }
     * 
     */
    public TypeDescription createTypeDescription() {
        return new TypeDescription();
    }

    /**
     * Create an instance of {@link EnumeratedValue }
     * 
     */
    public EnumeratedValue createEnumeratedValue() {
        return new EnumeratedValue();
    }

    /**
     * Create an instance of {@link FieldType }
     * 
     */
    public FieldType createFieldType() {
        return new FieldType();
    }

}
