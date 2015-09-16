package org.beybunproject.xmlContentVerifier;

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import org.beybunproject.xmlContentVerifier.utils.Utils;
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
      baseURI = Utils.normalizePath(href);
    else {
      baseURI = base.substring(0, base.lastIndexOf('/'));
      baseURI = Utils.normalizePath(baseURI + "/" + href);
    }
    return new StreamSource(resolver.getStreamForResource(null, null, null, href, base), baseURI);
  }
}
