package mtdl.utils;

/**
 * @author: yerlibilgin
 * @date: 08/09/15.
 */
public class MinderXsd {
}


/**
 * Requirement 1:
 *  Resolve an XSD schema and its dependencies from an asset plain xsd
 *  example:
 *
 *  val xsd = xsdFromAsset(getAsset("asset name"))
 *  xml-content-verifier.verifySchema(xsd, xml)
 *
 * Requirement 2:
 *  Resolve an XSD schema and its dependencies from an asset archive
 *  example:
 *
 *  val xsd = xsdFromAsset(getAsset("asset name"), ArchiveType..ZIP|GZIP|PLAIN|RAR?|7z?)
 *  xml-content-verifier.verifySchema(xsd, xml)
 *
 * Requirement 3:
 *  Resolve an XSD schema and its dependencies from a URL
 *  example:
 *
 *  val xsd = xsdFromUrl(new URL("http://www.xyz.com/schemas/xyzSchema.xsd"))
 *  xml-content-verifier.verifySchema(xsd, xml)
 *
 * Requirement 4:
 *  Resolve and XSD schema and its dependencies from a URL that contains an archive file
 *
 *  Example:
 *  val xsd = xsdFromUrl(new URL(http://www.xyz.com/schemas/xyzSchema.zip"), ArchiveType.ZIP|GZIP|PLAIN|RAR?|7z?)
 *  xml-content-verifier.verifySchema(xsd, xml)
 *
 *  INFO:
 *
 *
 *  SchemaFactory schemaFactory = SchemaFactory
 .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 LSResourceResolver lress = schemaFactory.getResourceResolver();
 schemaFactory.setResourceResolver(new LSResourceResolver() {
@Override
public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
System.out.println(type + " " + namespaceURI + " " + publicId + " " + systemId + " " + baseURI);
LSInput lsInput = new DOMInputImpl(type, namespaceURI, publicId, systemId, baseURI);
try {
lsInput.setByteStream(new FileInputStream("sampleXsd/" + systemId));
}catch (Exception ex){
throw new RuntimeException(ex);
}
return lsInput;
}
});
 */