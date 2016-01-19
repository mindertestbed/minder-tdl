package org.beybunproject.xmlContentVerifier;

import java.io.InputStream;

/**
 * Author: yerlibilgin
 * Date: 09/09/15.
 */
public interface SchemaDecoder {
  /**
   * @see org.w3c.dom.ls.LSResourceResolver#resolveResource(String, String, String, String, String)
   * @param type
   * @param namespaceURI
   * @param publicId
   * @param systemId
   * @param baseURI
   * @return
   */
  InputStream getStreamForResource(String type, String namespaceURI, String publicId, String systemId, String baseURI);

  void initialize(InputStream inputStream);
}
