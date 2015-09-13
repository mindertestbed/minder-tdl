package org.beybunproject.xmlContentVerifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.HashMap;

/**
 * Author: yerlibilgin
 * Date: 12/09/15.
 */
public abstract class ArchiveSchemaDecoder extends HashMap<String, byte[]> implements SchemaDecoder {
  @Override
  public InputStream getStreamForResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    /*
    System.out.println("Resolve");
    System.out.println("TYPE: " + type);
    System.out.println("NamespaceURI: " + namespaceURI);
    System.out.println("SYSTEMID: " + systemId);
    System.out.println("BASEURI: " + baseURI);
*/
    String actualPath;
    if (baseURI == null)
      actualPath = normalizePath(systemId);
    else {
      baseURI = baseURI.substring(0, baseURI.lastIndexOf('/'));
      actualPath = normalizePath(baseURI + "/" + systemId);
    }
/*
    System.out.println("ACTUAL PATH: " + actualPath);
    System.out.println("--------------");
*/
    if (this.containsKey(actualPath)) {
      return new ByteArrayInputStream(get(actualPath));
    }

    throw new RuntimeException("Entry [" + actualPath + "] not found in the archive archive");
  }

  @Override
  public void initialize(InputStream inputStream) {
    readEntries(inputStream);
  }

  public static String normalizePath(String s) {
    return FileSystems.getDefault().getPath(s).normalize().toString();
  }

  protected void readEntries(InputStream inputStream) {
    this.start(inputStream);
    ArchiveEntry entry;
    while ((entry = this.getNextEntry()) != null) {
      if (entry.isFile) {
        this.put(normalizePath(MINDER_DUMMY_PROTOCOL + entry.name), entry.bytes);
      }
    }
  }

  /**
   * @return the next entry or null if all entries are exhausted
   */
  protected abstract ArchiveEntry getNextEntry();

  /**
   * Initialize your internal structure and get ready for repetitive <code>getNextEntry</code> calls
   *
   * @param inputStream
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
