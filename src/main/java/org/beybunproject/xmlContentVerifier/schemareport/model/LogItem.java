package org.beybunproject.xmlContentVerifier.schemareport.model;

import javax.xml.bind.annotation.*;

/**
 * Created by ozgurmelis on 17/02/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "logItem", propOrder = {
    "columnnumber",
    "linenumber",
    "detailmessage"
})
@XmlRootElement(name = "logItem")
public class LogItem {

  @XmlElement(required = true)
  public String columnnumber;

  @XmlElement(required = true)
  public String linenumber;

  @XmlElement(required = true)
  public String detailmessage;
}
