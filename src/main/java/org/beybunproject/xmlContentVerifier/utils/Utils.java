package org.beybunproject.xmlContentVerifier.utils;

import org.beybunproject.xmlContentVerifier.utils.ExceptionUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Author: yerlibilgin
 * Date: 12/09/15.
 */
public class Utils {
  public static byte[] readStream(InputStream inputStream) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int read = 0;

      while ((read = inputStream.read(buf)) > 0) {
        baos.write(buf, 0, read);
      }
      return baos.toByteArray();
    } catch (Exception ex) {
      throw ExceptionUtils.asRuntimeException(ex);
    }
  }
}