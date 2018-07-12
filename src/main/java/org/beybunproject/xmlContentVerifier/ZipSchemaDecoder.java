package org.beybunproject.xmlContentVerifier;

import org.beybunproject.xmlContentVerifier.utils.ExceptionUtils;
import org.beybunproject.xmlContentVerifier.utils.Utils;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: yerlibilgin
 * Date: 09/09/15.
 */
public class ZipSchemaDecoder extends ArchiveSchemaDecoder {
  private static final Logger LOGGER = LoggerFactory.getLogger(ZipSchemaDecoder.class);

  private ZipInputStream zipInputStream;

  @Override
  protected ArchiveEntry getNextEntry() {
    try {
      ZipEntry ze = zipInputStream.getNextEntry();
      if (ze != null) {
        if(LOGGER.isTraceEnabled()) {
          LOGGER.trace("Resolved zip entry " + ze.getName());
        }
        return new ArchiveEntry(!ze.isDirectory(), ze.getName(), Utils.readStream(zipInputStream));
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
      zipInputStream = new ZipInputStream(inputStream);
    } catch (Exception ex) {
      ExceptionUtils.throwAsRuntimeException(ex);
    }
  }
}
