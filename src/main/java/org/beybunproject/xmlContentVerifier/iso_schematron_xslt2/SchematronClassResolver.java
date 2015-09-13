package org.beybunproject.xmlContentVerifier.iso_schematron_xslt2;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

/**
 * Created by yerlibilgin on 12/12/14.
 */
public class SchematronClassResolver implements URIResolver {
  @Override
  public Source resolve(String href, String base) throws TransformerException {
    //System.out.println("Resolve " + href + " ->>>>>> " + base);
    final InputStream asStream = this.getClass().getResourceAsStream(href);
    if (asStream == null)
      throw new RuntimeException(href + " not found");

    return new StreamSource(asStream);
  }

  public static InputStream rstrm(String s) {
    return SchematronClassResolver.class.getResourceAsStream(s);
  }
}
