package org.beybunproject.xmlContentVerifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Author: yerlibilgin
 * Date: 09/09/15.
 */
public class SchemaDecoderFactory {
  public static SchemaDecoder createDecoderFor(InputStream inputStream, ArchiveType archiveType) {
    SchemaDecoder target = null;
    switch (archiveType) {
      case PLAIN:
        target = new PlainSchemaDecoder();
        break;
      case RAR:
      case LZMA:
      case LZMA2:
        throw new UnsupportedOperationException("Archive Type " + archiveType + " is not supported yet");

      case ZIP:
        target = new ZipSchemaDecoder();
        break;

      case JAR:
        target = new JarSchemaDecoder();
        break;

      case TARGZ:
        target = new TarGzDecoder();
        break;
    }

    target.initialize(inputStream);
    return target;
  }

  public static SchemaDecoder createDecoderFor(byte[] archiveBytes, ArchiveType archiveType) {
    return createDecoderFor(new ByteArrayInputStream(archiveBytes), archiveType);
  }
}
