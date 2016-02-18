package org.beybunproject.xmlContentVerifier;

import org.beybunproject.xmlContentVerifier.iso_schematron_xslt2.SchematronClassResolver;
import org.beybunproject.xmlContentVerifier.schemareport.LoggingErrorHandler;
import org.beybunproject.xmlContentVerifier.schemareport.XMLReportGenerator;
import org.beybunproject.xmlContentVerifier.schemareport.model.SchemaValidationReport;
import org.beybunproject.xmlContentVerifier.utils.ExceptionUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.util.Properties;

/**
 * Author: yerlibilgin & ozgurmelis
 * Date: 12/09/15.
 */
public class XmlContentVerifier {

  static {
    System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
  }

  /**
   *
   * XSD VERIFICATION
   */
  /**
   * Checks the schema of the xml WRT the given xsd and returns the result including report in XML format.
   *
   * @param schema the schema definition that will be used for verification
   * @param xml    the xml that will be verified
   */
  public static String verifyXsd(Schema schema, byte[] xml) throws IOException, SAXException {
    return verifyXsd(schema, new ByteArrayInputStream(xml));
  }

  public static String verifyXsd(Schema xsd, InputStream inputStream) throws IOException, SAXException {
    javax.xml.validation.Schema schema = null;

    try {
      javax.xml.validation.SchemaFactory schemaFactory = javax.xml.validation.SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(xsd);
      schema = schemaFactory.newSchema(new StreamSource(xsd.getInputStream(), xsd.getSystemId()));
    } catch (Exception ex) {
      throw new RuntimeException("Unable to parse schema", ex);
    }

    Source xmlFile = new StreamSource(inputStream);
    return doXSDVerification(xmlFile, schema);

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
  public static String verifyXsd(byte[] xsd, byte[] xml) throws IOException, SAXException {
    return verifyXsd(new ByteArrayInputStream(xsd), new ByteArrayInputStream(xml));
  }

  /**
   * Assuming plain XSD and XML, this method performs direct verification on the XML.
   *
   * @param xsd as input stream
   * @param xml as input stream
   */
  public static String verifyXsd(InputStream xsd, InputStream xml) throws IOException, SAXException {
    javax.xml.validation.Schema schema = null;
    try {
      javax.xml.validation.SchemaFactory schemaFactory = javax.xml.validation.SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schema = schemaFactory.newSchema(new StreamSource(xsd));
    } catch (Exception ex) {
      throw new RuntimeException("Unable to parse schema", ex);
    }

    Source xmlFile = new StreamSource(xml);
    return doXSDVerification(xmlFile, schema);

  }


  public static String verifyXsd(String url, byte[] bytes) throws IOException, SAXException {
    return verifyXsd(url, new ByteArrayInputStream(bytes));
  }

  public static String verifyXsd(String url, InputStream inputStream) throws IOException, SAXException {
    javax.xml.validation.Schema schema = null;
    try {
      javax.xml.validation.SchemaFactory schemaFactory = javax.xml.validation.SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schema = schemaFactory.newSchema(new URL(url));
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }

    Source xmlFile = new StreamSource(inputStream);
    return doXSDVerification(xmlFile, schema);

  }

  public static String verifyXsd(String url, String xmlUrl) throws IOException, SAXException {
    return verifyXsd(new URL(url), new URL(xmlUrl));
  }

  public static String verifyXsd(URL url, URL xmlUrl) throws IOException, SAXException {
    javax.xml.validation.Schema schema = null;
    try {
      javax.xml.validation.SchemaFactory schemaFactory = javax.xml.validation.SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schema = schemaFactory.newSchema(url);
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }

    Source xmlFile = new StreamSource(xmlUrl.openStream());
    return doXSDVerification(xmlFile, schema);

  }

  private static String doXSDVerification(Source xmlFile, javax.xml.validation.Schema schema) throws IOException, SAXException {
    try {

      Validator validator = schema.newValidator();
      LoggingErrorHandler errorHandler = new LoggingErrorHandler();
      validator.setErrorHandler(errorHandler);
      validator.validate(xmlFile);

      if ((null == errorHandler.getReport().getFatalerror()) && (null == errorHandler.getReport().getErrors())) {
        errorHandler.getReport().setResult("SUCCESS");
      } else {
        errorHandler.getReport().setResult("FAILURE");
      }

      try {
        return XMLReportGenerator.generateXML(SchemaValidationReport.class.getName(), errorHandler.getReport());
      } catch (ParseException e) {
        throw new RuntimeException("Could not create XML report", e);
      }
    } catch (IOException ex) {
      throw new RuntimeException("XML Verification failed", ex);
    } catch (SAXException ex) {
      throw new RuntimeException("XML Verification fatal error", ex);
    }
  }

  /**
   *
   * SCHEMATRON VERIFICATION
   */

  /**
   * =============
   */
  public static String verifySchematron(byte[] sch, byte[] xml, Properties properties) throws RuntimeException {
    ByteArrayInputStream bSchematron = new ByteArrayInputStream(sch);
    ByteArrayInputStream bXml = new ByteArrayInputStream(xml);
    return verifySchematron(bSchematron, bXml, properties);

  }

  public static String verifySchematron(byte[] sch, InputStream xml, Properties properties) throws RuntimeException {
    ByteArrayInputStream bSchematron = new ByteArrayInputStream(sch);
    return verifySchematron(bSchematron, xml, properties);
  }

  /**
   * Performs schematron verification with the given schematron file on the provided xml
   *
   * @param schematron
   * @param xml
   */
  public static String verifySchematron(InputStream schematron, InputStream xml, Properties properties) throws RuntimeException {
    try {
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

      return baos.toString();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
  //=========

  /**
   * =========
   */

  public static String verifySchematron(String url, byte[] xml) throws RuntimeException {
    return verifySchematron(url, xml, null);
  }

  public static String verifySchematron(String url, byte[] xml, Properties properties) throws RuntimeException {
    try {
      return verifySchematron(new URL(url), xml, properties);
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }
  }

  public static String verifySchematron(URL schematronUrl, byte[] xml, Properties properties) throws RuntimeException {
    ByteArrayInputStream bXml = new ByteArrayInputStream(xml);
    return verifySchematron(schematronUrl, bXml, properties);
  }

  /**
   * Performs schematron verification with the given schematrno file on the provided xml
   *
   * @param url
   * @param xml
   */
  public static String verifySchematron(URL url, InputStream xml, Properties properties) throws RuntimeException {
    try {
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
      return baos.toString();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
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
  //=========


  /**
   * =========
   * Performs schematron verification with the given schematron file on the provided xml
   *
   * @param schematron
   * @param xml
   */
  public static String verifySchematron(Schema schematron, byte[] xml, Properties properties) throws RuntimeException {
    return verifySchematron(schematron, new ByteArrayInputStream(xml), properties);
  }

  /**
   * Performs schematron verification with the given schematron file on the provided xml
   *
   * @param schematron
   * @param xml
   */
  public static String verifySchematron(Schema schematron, InputStream xml, Properties properties) throws RuntimeException {
    try {
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
      return baos.toString();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
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
  //=========


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
