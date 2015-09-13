package org.beybunproject.xmlContentVerifier;

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.nio.file.FileSystems;

/**
 * Aauthor: yerlibilgin
 * Date: 08/09/15.
 */
public class Schema implements LSResourceResolver, URIResolver {
  private SchemaResourceResolver resolver;
  private String systemId;

  public Schema(SchemaResourceResolver resolver, String systemId) {
    this.resolver = resolver;
    this.systemId = systemId;
  }

  public InputStream getInputStream() {
    return resolver.getStreamForResource(null, null, null, systemId, null);
  }

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    LSInput lsInput = new DOMInputImpl();
    lsInput.setBaseURI(baseURI);
    lsInput.setSystemId(systemId);
    lsInput.setByteStream(resolver.getStreamForResource(type, namespaceURI, publicId, systemId, baseURI));
    return lsInput;
  }

  public String getSystemId() {
    return systemId;
  }

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    String baseURI;
    String actualPath;
    if (base == null || base.isEmpty())
      baseURI = FileSystems.getDefault().getPath(href).normalize().toString();
    else {
      baseURI = base.substring(0, base.lastIndexOf('/'));
      baseURI = FileSystems.getDefault().getPath(baseURI + "/" + href).normalize().toString();
    }
    return new StreamSource(resolver.getStreamForResource(null, null, null, href, base), baseURI);
  }
}


/**
 * Requirement 1:
 * Resolve an XSD schema and its dependencies from an asset plain xsd
 * example:
 * <p>
 * val xsd = schemaFromByteArray(getAsset("asset name"))
 * xml-content-verifier.verifySchema(xsd, xml)
 * <p>
 * Requirement 2:
 * Resolve an XSD schema and its dependencies from an asset archive
 * example:
 * <p>
 * val xsd = schemaFromByteArray(getAsset("asset name"), ArchiveType..ZIP|GZIP|PLAIN|RAR?|7z?)
 * xml-content-verifier.verifySchema(xsd, xml)
 * <p>
 * Requirement 3:
 * Resolve an XSD schema and its dependencies from a URL
 * example:
 * <p>
 * val xsd = xsdFromUrl(new URL("http://www.xyz.com/schemas/xyzSchema.xsd"))
 * xml-content-verifier.verifySchema(xsd, xml)
 * <p>
 * Requirement 4:
 * Resolve and XSD schema and its dependencies from a URL that contains an archive file
 * <p>
 * Example:
 * val xsd = xsdFromUrl(new URL(http://www.xyz.com/schemas/xyzSchema.zip"), ArchiveType.ZIP|GZIP|PLAIN|RAR?|7z?)
 * xml-content-verifier.verifySchema(xsd, xml)
 * <p>
 * INFO:
 * <p>
 * <p>
 * SchemaFactory schemaFactory = SchemaFactory
 * .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 * LSResourceResolver lress = schemaFactory.getResourceResolver();
 * schemaFactory.setResourceResolver(new LSResourceResolver() {
 *
 * @Override public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
 * System.out.println(type + " " + namespaceURI + " " + publicId + " " + systemId + " " + baseURI);
 * LSInput lsInput = new DOMInputImpl(type, namespaceURI, publicId, systemId, baseURI);
 * try {
 * lsInput.setByteStream(new FileInputStream("sampleXsd/" + systemId));
 * }catch (Exception ex){
 * throw new RuntimeException(ex);
 * }
 * return lsInput;
 * }
 * });
 */