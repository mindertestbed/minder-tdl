package org.beybunproject.xmlContentVerifier;

import org.beybunproject.xmlContentVerifier.utils.ExceptionUtils;
import org.beybunproject.xmlContentVerifier.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Author: yerlibilgin
 * Date: 11/09/15.
 */
public class PlainSchemaDecoder implements SchemaDecoder {
  private byte[] buffer;

  @Override
  public InputStream getStreamForResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    return new ByteArrayInputStream(buffer);
  }

  @Override
  public void initialize(InputStream inputStream) {
    try {
      this.buffer = Utils.readStream(inputStream);
    } catch (Exception ex) {
      ExceptionUtils.throwAsRuntimeException(ex);
    }
  }
}
