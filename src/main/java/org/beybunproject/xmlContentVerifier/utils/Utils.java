package org.beybunproject.xmlContentVerifier.utils;

import org.beybunproject.xmlContentVerifier.utils.ExceptionUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

/**
 * Author: yerlibilgin
 * Date: 12/09/15.
 */
public class Utils {

  public static final String ARCH_URI = System.getProperty("ARCH_URI", "minbase://");
  public static final char SYSTEM_SEPARATOR = '/';

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

  public static String normalizePath(String path) {
    return doNormalize(path, false);
  }

  public static int getPrefixLength(String filename) {
    if (filename == null) {
      return -1;
    }
    int len = filename.length();
    if (len == 0) {
      return 0;
    }
    char ch0 = filename.charAt(0);
    if (ch0 == ':') {
      return -1;
    }
    if (len == 1) {
      if (ch0 == '~') {
        return 2;  // return a length greater than the input
      }
      return (isSeparator(ch0) ? 1 : 0);
    } else {
      if (ch0 == '~') {
        int posUnix = filename.indexOf(SYSTEM_SEPARATOR, 1);
        if (posUnix == -1) {
          return len + 1;  // return a length greater than the input
        }
        return posUnix + 1;
      }
      char ch1 = filename.charAt(1);
      if (ch1 == ':') {
        ch0 = Character.toUpperCase(ch0);
        if (ch0 >= 'A' && ch0 <= 'Z') {
          if (len == 2 || isSeparator(filename.charAt(2)) == false) {
            return 2;
          }
          return 3;
        }
        return -1;

      } else if (isSeparator(ch0) && isSeparator(ch1)) {
        int posUnix = filename.indexOf(SYSTEM_SEPARATOR, 2);
        if ((posUnix == -1) || posUnix == 2) {
          return -1;
        }
        return posUnix + 1;
      } else {
        return (isSeparator(ch0) ? 1 : 0);
      }
    }
  }

  private static boolean isSeparator(char ch0) {
    return SYSTEM_SEPARATOR == ch0;
  }


  /**
   *
   * CODE BORROWED FROM apache.commons FileNameUtils
   * Internal method to perform the normalization.
   *
   * @param filename      the filename
   * @param keepSeparator true to keep the final separator
   * @return the normalized filename
   */
  private static String doNormalize(String filename, boolean keepSeparator) {
    if (filename == null) {
      return null;
    }
    int size = filename.length();
    if (size == 0) {
      return filename;
    }
    int prefix = getPrefixLength(filename);
    if (prefix < 0) {
      return null;
    }

    char[] array = new char[size + 2];  // +1 for possible extra slash, +2 for arraycopy
    filename.getChars(0, filename.length(), array, 0);

    // add extra separator on the end to simplify code below
    boolean lastIsDirectory = true;
    if (array[size - 1] != SYSTEM_SEPARATOR) {
      array[size++] = SYSTEM_SEPARATOR;
      lastIsDirectory = false;
    }

    // adjoining slashes
    for (int i = prefix + 1; i < size; i++) {
      if (array[i] == SYSTEM_SEPARATOR && array[i - 1] == SYSTEM_SEPARATOR) {
        System.arraycopy(array, i, array, i - 1, size - i);
        size--;
        i--;
      }
    }

    // dot slash
    for (int i = prefix + 1; i < size; i++) {
      if (array[i] == SYSTEM_SEPARATOR && array[i - 1] == '.' &&
          (i == prefix + 1 || array[i - 2] == SYSTEM_SEPARATOR)) {
        if (i == size - 1) {
          lastIsDirectory = true;
        }
        System.arraycopy(array, i + 1, array, i - 1, size - i);
        size -= 2;
        i--;
      }
    }

    // double dot slash
    outer:
    for (int i = prefix + 2; i < size; i++) {
      if (array[i] == SYSTEM_SEPARATOR && array[i - 1] == '.' && array[i - 2] == '.' &&
          (i == prefix + 2 || array[i - 3] == SYSTEM_SEPARATOR)) {
        if (i == prefix + 2) {
          return null;
        }
        if (i == size - 1) {
          lastIsDirectory = true;
        }
        int j;
        for (j = i - 4; j >= prefix; j--) {
          if (array[j] == SYSTEM_SEPARATOR) {
            // remove b/../ from a/b/../c
            System.arraycopy(array, i + 1, array, j + 1, size - i);
            size -= (i - j);
            i = j + 1;
            continue outer;
          }
        }
        // remove a/../ from a/../c
        System.arraycopy(array, i + 1, array, prefix, size - i);
        size -= (i + 1 - prefix);
        i = prefix + 1;
      }
    }

    if (size <= 0) {  // should never be less than 0
      return "";
    }
    if (size <= prefix) {  // should never be less than prefix
      return new String(array, 0, size);
    }
    if (lastIsDirectory && keepSeparator) {
      return new String(array, 0, size);  // keep trailing separator
    }
    return new String(array, 0, size - 1);  // lose trailing separator
  }
}