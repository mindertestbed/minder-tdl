package org.beybunproject.xmlContentVerifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.beybunproject.xmlContentVerifier.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: yerlibilgin
 * Date: 12/09/15.
 */
public abstract class ArchiveSchemaDecoder extends HashMap<String, byte[]> implements SchemaDecoder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveSchemaDecoder.class);

  @Override
  public InputStream getStreamForResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    String normalizedResourcePath;
    if (baseURI == null) {
      normalizedResourcePath = systemId;
    } else if(systemId.startsWith("http://") || systemId.startsWith("https://") || systemId.startsWith("file://")){
      normalizedResourcePath = systemId;
    } else{
      normalizedResourcePath = baseURI + "/../" + systemId;
    }

    try {
      normalizedResourcePath = new URI(normalizedResourcePath).normalize().toString();
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage(), e);
    }

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Resolve");
      LOGGER.trace("       TYPE: " + type);
      LOGGER.trace("       NamespaceURI: " + namespaceURI);
      LOGGER.trace("       SYSTEMID: " + systemId);
      LOGGER.trace("       BASEURI: " + baseURI);
      LOGGER.trace("       ACTUAL PATH: " + normalizedResourcePath);
    }

    if (this.containsKey(normalizedResourcePath)) {
      return new ByteArrayInputStream(get(normalizedResourcePath));
    }

    //check if the a

    if (normalizedResourcePath.startsWith("file://")) {
      File file = new File(normalizedResourcePath);
      if (!file.exists()) {
        throw new IllegalStateException("File " + normalizedResourcePath + " not found");
      }

      try {
        return new FileInputStream(systemId);
      } catch (FileNotFoundException e) {

      }
    }

    if (normalizedResourcePath.startsWith("http://") || normalizedResourcePath.startsWith("https://")) {
      try {
        URL url = new URL(normalizedResourcePath);
        return url.openConnection().getInputStream();
      } catch (MalformedURLException e) {
        throw new IllegalStateException("Malformed URL " + normalizedResourcePath);
      } catch (IOException e) {
        LOGGER.trace(e.getMessage(), e);
        throw new IllegalStateException("Cannot open URL " + normalizedResourcePath);
      }
    }

    throw new IllegalStateException("Entry [" + normalizedResourcePath + "] not found in the archive archive");
  }

  @Override
  public void initialize(InputStream inputStream) {
    readEntries(inputStream);
  }

  protected void readEntries(InputStream inputStream) {
    this.start(inputStream);
    ArchiveEntry entry;
    while ((entry = this.getNextEntry()) != null) {
      if (entry.isFile) {
        this.put(Utils.ARCH_URI + entry.name, entry.bytes);
      }
    }
  }

  /**
   * @return the next entry or null if all entries are exhausted
   */
  protected abstract ArchiveEntry getNextEntry();

  /**
   * Initialize your internal structure and get ready for repetitive <code>getNextEntry</code> calls
   */
  protected abstract void start(InputStream inputStream);

  protected class ArchiveEntry {

    public boolean isFile;
    public String name;
    public byte[] bytes;

    public ArchiveEntry() {
    }

    public ArchiveEntry(boolean isFile, String name, byte[] bytes) {
      this.isFile = isFile;
      this.name = name;
      this.bytes = bytes;
    }
  }

}
