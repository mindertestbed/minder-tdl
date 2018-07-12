package org.beybunproject.xmlContentVerifier;

import static org.junit.Assert.*;

import com.yerlibilgin.commons.FileUtils;
import com.yerlibilgin.commons.io.StreamUtils;
import java.io.IOException;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author yerlibilgin
 */
public class XmlContentVerifierTest {

  @Test
  public void verifyXsdZipFromURL() throws IOException, SAXException {
    final Schema schema = XmlContentVerifier.schemaFromURL("etsi119442.xsd", this.getClass().getResource("/valsschema2.zip").toString(), ArchiveType.ZIP);
    byte[] xml = StreamUtils.consumeStream(this.getClass().getResourceAsStream("/verify-response.xml"));

    final String s = XmlContentVerifier.verifyXsd(schema, xml);

    System.out.println(s);
  }

  @Test
  public void verifyXsdZipFromByteArray() throws IOException, SAXException {
    byte[] schemaBytes = StreamUtils.consumeStream(this.getClass().getResourceAsStream("/valsschema2.zip"));
    final Schema schema = XmlContentVerifier.schemaFromByteArray("etsi119442.xsd", schemaBytes, ArchiveType.ZIP);
    byte[] xml = StreamUtils.consumeStream(this.getClass().getResourceAsStream("/verify-response.xml"));

    final String s = XmlContentVerifier.verifyXsd(schema, xml);

    System.out.println(s);
  }

  @Test
  public void verifyXsdZipFromURLArchiveWithFolder() throws IOException, SAXException {
    final Schema schema = XmlContentVerifier.schemaFromURL("valsschema/etsi119442.xsd", this.getClass().getResource("/valsschema.zip").toString(), ArchiveType.ZIP);
    byte[] xml = StreamUtils.consumeStream(this.getClass().getResourceAsStream("/verify-response.xml"));

    final String s = XmlContentVerifier.verifyXsd(schema, xml);

    System.out.println(s);
  }

  @Test
  public void verifyXsdZipFromByteArrayArchiveWithFolder() throws IOException, SAXException {
    byte[] schemaBytes = StreamUtils.consumeStream(this.getClass().getResourceAsStream("/valsschema.zip"));
    final Schema schema = XmlContentVerifier.schemaFromByteArray("valsschema/etsi119442.xsd", schemaBytes, ArchiveType.ZIP);
    byte[] xml = StreamUtils.consumeStream(this.getClass().getResourceAsStream("/verify-response.xml"));

    final String s = XmlContentVerifier.verifyXsd(schema, xml);

    System.out.println(s);
  }
}