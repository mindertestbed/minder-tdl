package org.beybunproject.xmlContentVerifier;

import java.io.InputStream;

/**
 * Author: yerlibilgin
 * Date: 09/09/15.
 */
public class TarGzDecoder extends ArchiveSchemaDecoder {
  @Override
  protected ArchiveEntry getNextEntry() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  protected void start(InputStream inputStream) {
    throw new UnsupportedOperationException("Not implemented");
  }
}

