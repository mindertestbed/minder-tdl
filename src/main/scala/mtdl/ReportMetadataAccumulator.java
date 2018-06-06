package mtdl;

/**
 * A utility class for accumulating metadata for further reporting
 * @author yerlibilgin
 */
public class ReportMetadataAccumulator {
  private StringBuffer metadata = new StringBuffer();

  /**
   * Clear the contents of the metadata
   */
  public void resetMetadata(){
    metadata = new StringBuffer();
  }


  /**
   * Append extra text to the metadata
   * @param text
   */
  public void appendMetadata(String text){
    metadata.append(text);
  }

  public StringBuffer getMetadata() {
    return metadata;
  }
}
