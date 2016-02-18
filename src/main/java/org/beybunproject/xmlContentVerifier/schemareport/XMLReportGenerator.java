package org.beybunproject.xmlContentVerifier.schemareport;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;

/**
 * Created by ozgurmelis on 17/02/16.
 */
public class XMLReportGenerator {

  public static String generateXML(String className, Object o) throws ParseException {
    try {
      Class clazz = Class.forName(className);

      JAXBContext jaxbContext = JAXBContext.newInstance(clazz.getPackage().getName());
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      marshaller.marshal(clazz.cast(o), byteArrayOutputStream);

      return byteArrayOutputStream.toString();

    } catch (JAXBException e) {
      throw new ParseException("Error occured during the creation of XML. The details of the exception " + e.toString(), 0);
    } catch (ClassNotFoundException e) {
      throw new ParseException("Error occured during the creation of XML. The details of the exception " + e.toString(), 0);
    }

  }

}

