package org.beybunproject.xmlContentVerifier;

import java.io.InputStream;

/**
 * Author: yerlibilgin
 * Date: 11/09/15.
 */
public class SchemaResourceResolver {
  private final SchemaDecoder decoder;

  public SchemaResourceResolver(byte[] source, ArchiveType type) {
    this.decoder = SchemaDecoderFactory.createDecoderFor(source, type);
  }

  public InputStream getStreamForResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    return this.decoder.getStreamForResource(type, namespaceURI, publicId, systemId, baseURI);
  }
}
