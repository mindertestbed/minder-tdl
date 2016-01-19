package org.beybunproject.xmlContentVerifier;

import org.beybunproject.xmlContentVerifier.utils.ExceptionUtils;
import org.beybunproject.xmlContentVerifier.utils.Utils;

import java.io.InputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * Author: yerlibilgin
 * Date: 09/09/15.
 */
public class JarSchemaDecoder extends ArchiveSchemaDecoder {

  JarInputStream jarInputStream;

  @Override
  protected ArchiveEntry getNextEntry() {
    try {
      ZipEntry ze = jarInputStream.getNextEntry();
      if (ze != null) {
        return new ArchiveEntry(!ze.isDirectory(), ze.getName(), Utils.readStream(jarInputStream));
      }

      return null;
    } catch (Exception ex) {
      ExceptionUtils.throwAsRuntimeException(ex);
      return null;
    }
  }

  @Override
  protected void start(InputStream inputStream) {
    try {
      jarInputStream = new JarInputStream(inputStream);
    } catch (Exception ex) {
      ExceptionUtils.throwAsRuntimeException(ex);
    }
  }
}
