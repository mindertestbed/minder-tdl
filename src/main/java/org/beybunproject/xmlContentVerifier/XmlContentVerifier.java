package org.beybunproject.xmlContentVerifier;

import org.beybunproject.xmlContentVerifier.iso_schematron_xslt2.SchematronClassResolver;
import org.beybunproject.xmlContentVerifier.utils.ExceptionUtils;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Author: yerlibilgin
 * Date: 12/09/15.
 */
public class XmlContentVerifier {

  static {
    System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
  }

  /**
   * Checks the schema of the xml WRT the given xsd and returns the result
   *
   * @param schema the schema definition that will be used for verification
   * @param xml    the xml that will be verified
   */
  public static void verifyXsd(Schema schema, byte[] xml) {
    verifyXsd(schema, new ByteArrayInputStream(xml));
  }

  /**
   * Assuming plain XSD and XML bytes, this method delegates the
   * verifyXsd function with ByteArrayInputStreams and performs verification
   * directly on the resources.
   * <p>
   * Any external xsd reference will fail.
   *
   * @param xsd
   * @param xml
   */
  public static void verifyXsd(byte[] xsd, byte[] xml) {
    verifyXsd(new ByteArrayInputStream(xsd), new ByteArrayInputStream(xml));
  }

  /**
   * Assuming plain XSD and XML, this method performs direct verification on the XML.
   *
   * @param xsd as input stream
   * @param xml as input stream
   */
  public static void verifyXsd(InputStream xsd, InputStream xml) {
    javax.xml.validation.Schema schema = null;
    try {
      javax.xml.validation.SchemaFactory schemaFactory = javax.xml.validation.SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schema = schemaFactory.newSchema(new StreamSource(xsd));
    } catch (Exception ex) {
      throw new RuntimeException("Unable to parse schema", ex);
    }

    try {
      Source xmlFile = new StreamSource(xml);
      Validator validator = schema.newValidator();
      validator.validate(xmlFile);

    } catch (Exception ex) {
      throw new RuntimeException("XML Verification failed", ex);
    }
  }

  public static void verifyXsd(Schema xsd, InputStream inputStream) {
    javax.xml.validation.Schema schema = null;
    try {
      javax.xml.validation.SchemaFactory schemaFactory = javax.xml.validation.SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(xsd);
      schema = schemaFactory.newSchema(new StreamSource(xsd.getInputStream(), xsd.getSystemId()));
    } catch (Exception ex) {
      throw new RuntimeException("Unable to parse schema", ex);
    }

    try {
      Source xmlFile = new StreamSource(inputStream);
      Validator validator = schema.newValidator();
      validator.validate(xmlFile);

    } catch (Exception ex) {
      throw new RuntimeException("XML Verification failed", ex);
    }
  }


  public static void verifyXsd(String url, byte []bytes){
    verifyXsd(url, new ByteArrayInputStream(bytes));
  }

  public static void verifyXsd(String url, InputStream inputStream) {
    javax.xml.validation.Schema schema = null;
    try {
      javax.xml.validation.SchemaFactory schemaFactory = javax.xml.validation.SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schema = schemaFactory.newSchema(new URL(url));
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }

    try {
      Source xmlFile = new StreamSource(inputStream);
      Validator validator = schema.newValidator();
      validator.validate(xmlFile);

    } catch (Exception ex) {
      throw new RuntimeException("XML Verification failed", ex);
    }
  }

  public static void verifyXsd(String url, String xmlUrl) {
    try {
      verifyXsd(new URL(url), new URL(xmlUrl));
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }
  }

  public static void verifyXsd(URL url, URL xmlUrl) {
    javax.xml.validation.Schema schema = null;
    try {
      javax.xml.validation.SchemaFactory schemaFactory = javax.xml.validation.SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schema = schemaFactory.newSchema(url);
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }

    try {
      Source xmlFile = new StreamSource(xmlUrl.openStream());
      Validator validator = schema.newValidator();
      validator.validate(xmlFile);

    } catch (Exception ex) {
      throw new RuntimeException("XML Verification failed", ex);
    }
  }

  public static void verifySchematron(byte[] sch, byte[] xml, Properties properties) {
    ByteArrayInputStream bSchematron = new ByteArrayInputStream(sch);
    ByteArrayInputStream bXml = new ByteArrayInputStream(xml);
    verifySchematron(bSchematron, bXml, properties);
  }

  public static void verifySchematron(byte[] sch, InputStream xml, Properties properties) {
    ByteArrayInputStream bSchematron = new ByteArrayInputStream(sch);
    verifySchematron(bSchematron, xml, properties);
  }

  public static void verifySchematron(URL schematronUrl, byte[] xml, Properties properties) {
    ByteArrayInputStream bXml = new ByteArrayInputStream(xml);
    verifySchematron(schematronUrl, bXml, properties);
  }

  /**
   * Performs schematron verification with the given schematrno file on the provided xml
   *
   * @param url
   * @param xml
   */
  public static void verifySchematron(URL url, InputStream xml, Properties properties) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    urlTransform(SchematronClassResolver.rstrm("iso_dsdl_include.xsl"), url, baos, properties);
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransformStream(SchematronClassResolver.rstrm("iso_abstract_expand.xsl"), bais, baos, properties);
    bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransformStream(SchematronClassResolver.rstrm("iso_svrl_for_xslt2.xsl"), bais, baos, properties);
    bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransformStream(bais, xml, baos, properties);
    checkSchematronResult(baos.toString());
  }

  /**
   * Performs schematron verification with the given schematron file on the provided xml
   *
   * @param schematron
   * @param xml
   */
  public static void verifySchematron(Schema schematron, byte[] xml) {
    verifySchematron(schematron, new ByteArrayInputStream(xml), null);
  }

  /**
   * Performs schematron verification with the given schematron file on the provided xml
   *
   * @param schematron
   * @param xml
   */
  public static void verifySchematron(Schema schematron, byte[] xml, Properties properties) {
    verifySchematron(schematron, new ByteArrayInputStream(xml), properties);
  }

  public static void verifySchematron(String url, byte[] xml) {
    verifySchematron(url, xml, null);
  }

  public static void verifySchematron(String url, byte[] xml, Properties properties) {
    try {
      verifySchematron(new URL(url), xml, properties);
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }
  }


  /**
   * Performs schematron verification with the given schematron file on the provided xml
   *
   * @param schematron
   * @param xml
   */
  public static void verifySchematron(Schema schematron, InputStream xml, Properties properties) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    schemaTransform(SchematronClassResolver.rstrm("iso_dsdl_include.xsl"), schematron, baos, properties);
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransformStream(SchematronClassResolver.rstrm("iso_abstract_expand.xsl"), bais, baos, properties);
    bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransformStream(SchematronClassResolver.rstrm("iso_svrl_for_xslt2.xsl"), bais, baos, properties);
    bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransformStream(bais, xml, baos, properties);
    checkSchematronResult(baos.toString());
  }

  /**
   * Performs schematron verification with the given schematrno file on the provided xml
   *
   * @param schematron
   * @param xml
   */
  public static void verifySchematron(InputStream schematron, InputStream xml, Properties properties) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    simpleTransformStream(SchematronClassResolver.rstrm("iso_dsdl_include.xsl"), schematron, baos, properties);
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransformStream(SchematronClassResolver.rstrm("iso_abstract_expand.xsl"), bais, baos, properties);
    bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransformStream(SchematronClassResolver.rstrm("iso_svrl_for_xslt2.xsl"), bais, baos, properties);
    bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransformStream(bais, xml, baos, properties);
    checkSchematronResult(baos.toString());
  }


  private static void urlTransform(InputStream rstrm, URL url, OutputStream outputStream, Properties properties) {
    try {
      TransformerFactory tFactory = TransformerFactory.newInstance();

      tFactory.setErrorListener(canceller);
      Transformer transformer = tFactory.newTransformer(new StreamSource(rstrm));
      if (properties != null)
        for (String property : properties.stringPropertyNames()) {
          transformer.setParameter(property, properties.getProperty(property));
        }
      transformer.transform(new StreamSource(url.toExternalForm()), new StreamResult(outputStream));
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }
  }

  private static void schemaTransform(InputStream rstrm, Schema schematron, OutputStream outputStream, Properties properties) {
    try {
      TransformerFactory tFactory = TransformerFactory.newInstance();
      tFactory.setURIResolver(schematron);
      tFactory.setErrorListener(canceller);

      final StreamSource source = new StreamSource(rstrm);
      Transformer transformer = tFactory.newTransformer(source);
      if (properties != null)
        for (String property : properties.stringPropertyNames()) {
          transformer.setParameter(property, properties.getProperty(property));
        }
      transformer.transform(new StreamSource(schematron.getInputStream(), schematron.getSystemId()), new StreamResult(outputStream));
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }
  }

  /**
   * Simple transformation method.
   *
   * @param xslStream    - The input stream that the xsl will be read from
   * @param sourceStream - Input that the xml for verification will be read from.
   * @param outputStream - The output stream that the result will be written into.
   */
  public static void simpleTransformStream(InputStream xslStream, InputStream sourceStream, OutputStream outputStream,
                                           Properties properties) {
    try {
      SchematronClassResolver resolver = new SchematronClassResolver();
      TransformerFactory tFactory = TransformerFactory.newInstance();
      tFactory.setURIResolver(resolver);

      tFactory.setErrorListener(canceller);

      Transformer transformer = tFactory.newTransformer(new StreamSource(xslStream));
      if (properties != null)
        for (String property : properties.stringPropertyNames()) {
          transformer.setParameter(property, properties.getProperty(property));
        }
      transformer.transform(new StreamSource(sourceStream), new StreamResult(outputStream));
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }
  }

  /**
   * Simple transformation method.
   *
   * @param xsl - The byte array that includes the xsl
   * @param xml - The byte array that includes the xml
   */
  public static void simpleTransform(byte[] xsl, byte[] xml, Properties properties) {
    ByteArrayInputStream bXsl = new ByteArrayInputStream(xsl);
    ByteArrayInputStream bXml = new ByteArrayInputStream(xml);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    simpleTransformStream(bXsl, bXml, baos, properties);
    baos.toByteArray();
  }

  public static Schema schemaFromByteArray(byte[] bytes) {
    return schemaFromByteArray(null, bytes, ArchiveType.PLAIN);
  }

  public static Schema schemaFromByteArray(String name, byte[] bytes, ArchiveType archiveType) {
    return SchemaFactory.schemaFromByteArray(name, bytes, archiveType);
  }

  public static Schema schemaFromURL(String url) {
    return schemaFromURL(null, url, ArchiveType.PLAIN);
  }

  public static Schema schemaFromURL(String name, String url, ArchiveType archiveType) {
    return SchemaFactory.schemaFromUrl(name, url, archiveType);
  }

  private static void checkSchematronResult(String result) {
    //simple check might be dangerous.
    if (result.contains("<svrl:failed-assert")) {
      //System.out.println(result);
      throw new RuntimeException("Schematron verification failed");
    }
  }


  private static final ErrorListener canceller = new ErrorListener() {
    @Override
    public void warning(TransformerException exception) throws TransformerException {
      //throw new RuntimeException(exception.getMessage(), exception);
    }

    @Override
    public void error(TransformerException exception) throws TransformerException {
      throw new RuntimeException(exception.getMessage(), exception);
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException {
      throw new RuntimeException(exception.getMessage(), exception);
    }
  };
}
