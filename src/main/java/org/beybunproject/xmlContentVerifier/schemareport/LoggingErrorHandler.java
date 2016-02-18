package org.beybunproject.xmlContentVerifier.schemareport;

import org.beybunproject.xmlContentVerifier.schemareport.model.LogItem;
import org.beybunproject.xmlContentVerifier.schemareport.model.SchemaValidationReport;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;

/**
 * Created by ozgurmelis on 17/02/16.
 */
public class LoggingErrorHandler implements ErrorHandler {

  private SchemaValidationReport report;

  public LoggingErrorHandler() {
    report = new SchemaValidationReport();
  }

  @Override
  public void warning(SAXParseException exception) throws SAXException {
    if (null == report.getWarnings()) {
      report.setWarnings(new ArrayList<>());
    }

    report.getWarnings().add(prepareLogItem(exception));
  }

  @Override
  public void error(SAXParseException exception) throws SAXException {
    if (null == report.getErrors()) {
      report.setErrors(new ArrayList<>());
    }

    report.getErrors().add(prepareLogItem(exception));
  }

  @Override
  public void fatalError(SAXParseException exception) throws SAXException {
    report.setErrors(new ArrayList<>());
    report.setFatalerror(prepareLogItem(exception));
  }

  private LogItem prepareLogItem(SAXParseException exception) {
    LogItem logItem = new LogItem();
    logItem.columnnumber = String.valueOf(exception.getColumnNumber());
    logItem.linenumber = String.valueOf(exception.getLineNumber());
    logItem.detailmessage = exception.getMessage();

    return logItem;
  }


  public SchemaValidationReport getReport() {
    return report;
  }
}


