package org.beybunproject.xmlContentVerifier.schemareport.model;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by ozgurmelis on 17/02/16.
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testCase", propOrder = {
        "result",
        "warnings",
        "errors",
        "fatalerror"
})
@XmlRootElement(name = "schemaValidationReport")
public class SchemaValidationReport {

    @XmlElement(required = true)
    public String result;

    @XmlElementWrapper(name="warning")
    @XmlElement(required = false, nillable = true)
    public List<LogItem> warnings;

    @XmlElementWrapper(name="error")
    @XmlElement(required = false, nillable = true)
    public List<LogItem> errors;

    @XmlElement(required = false)
    public LogItem fatalerror;

    public String getResult() {
        return result;
    }

    public List<LogItem> getWarnings() {
        return warnings;
    }

    public List<LogItem> getErrors() {
        return errors;
    }

    public LogItem getFatalerror() {
        return fatalerror;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setWarnings(List<LogItem> warnings) {
        this.warnings = warnings;
    }

    public void setErrors(List<LogItem> errors) {
        this.errors = errors;
    }

    public void setFatalerror(LogItem fatalerror) {
        this.fatalerror = fatalerror;
    }
}

