package org.beybunproject.xmlContentVerifier;

import org.beybunproject.xmlContentVerifier.utils.ExceptionUtils;
import org.beybunproject.xmlContentVerifier.utils.Utils;

import java.net.URL;

/**
 * Author: yerlibilgin
 * Date: 11/09/15.
 */
public class SchemaFactory {
  public static Schema schemaFromByteArray(String name, byte[] source, ArchiveType type) {
    SchemaResourceResolver archiveResourceResolver = new SchemaResourceResolver(source, type);
    Schema schema = new Schema(archiveResourceResolver, SchemaDecoder.MINDER_DUMMY_PROTOCOL + name);
    return schema;
  }

  /**
   * Read the stream of the url and create a memory resource resolver
   * @param name
   * @param url
   * @param archiveType
   * @return
   */
  public static Schema schemaFromUrl(String name, String url, ArchiveType archiveType) {
    try {
      if(archiveType == ArchiveType.PLAIN){
        //we need to make sure that if the xsd references others, we provide them
        //URLXsdResolver xsdResourceResolver = new URLXsdResolver(urlO.toURI().get);

      }else {
        final URL urlO = new URL(url);
        byte[] bytes = Utils.readStream(urlO.openStream());
        SchemaResourceResolver schemaResourceResolver = new SchemaResourceResolver(bytes, archiveType);
        return new Schema(schemaResourceResolver, SchemaDecoder.MINDER_DUMMY_PROTOCOL + name);
      }
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }

    return null;
  }
}
