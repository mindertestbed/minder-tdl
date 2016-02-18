package org.beybunproject.xmlContentVerifier.schemareport.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the rest.controllers.xmlmodel.response package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

  private final static QName _LogItem_QNAME = new QName("", "logItem");
  private final static QName _SchemaValidationReport_QNAME = new QName("", "schemaValidationReport");


  /**
   * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: rest.controllers.xmlmodel.response
   */
  public ObjectFactory() {
  }

  /**
   * Create an instance of {@link LogItem }
   */
  public LogItem createLogItem() {
    return new LogItem();
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link LogItem }{@code >}}
   */
  @XmlElementDecl(namespace = "", name = "logItem")
  public JAXBElement<LogItem> createLogItem(LogItem value) {
    return new JAXBElement<LogItem>(_LogItem_QNAME, LogItem.class, null, value);
  }

  /**
   * Create an instance of {@link SchemaValidationReport }
   */
  public SchemaValidationReport createSchemaValidationReport() {
    return new SchemaValidationReport();
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link SchemaValidationReport }{@code >}}
   */
  @XmlElementDecl(namespace = "", name = "schemaValidationReport")
  public JAXBElement<SchemaValidationReport> createSchemaValidationReport(SchemaValidationReport value) {
    return new JAXBElement<SchemaValidationReport>(_SchemaValidationReport_QNAME, SchemaValidationReport.class, null, value);
  }

}
