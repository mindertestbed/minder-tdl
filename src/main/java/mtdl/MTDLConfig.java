package mtdl;

import com.yerlibilgin.commons.ConfigurationReader;
import java.io.File;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yerlibilgin
 */
public class MTDLConfig extends ConfigurationReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(MTDLConfig.class);

  public static final String MTDL_CONFIG_KEY = "mtdlConfig.properties";

  public static String TDL_SOURCE_DIR = "data/tdlsrc";
  public static String TDL_CLASS_DIR = "data/tdlcls";
  public static String TDL_DEPENDENCY_DIR = "data/dependencies";
  public static String MTDL_JAR_PATH = "data/mtdl.jar";
  public static String MVN_SETTINGS_XML = "conf/maven-settings.xml";
  public static final String DEFAULT_MVN_SETTINGS_XML = "default-maven-settings.xml";

  static {
    try {
      //discover a "mtdlConfig.properties" as either a key to a file or a resource
      //or directly a file or a resource
      final String configFileName = MTDL_CONFIG_KEY;
      Properties properties = discoverConfigFileOrResource(configFileName);

      TDL_SOURCE_DIR = convertToAbsolutePathOrNull(getSystemPropertyOrDefault("tdl.source.dir", properties));
      TDL_CLASS_DIR = convertToAbsolutePathOrNull(getSystemPropertyOrDefault("tdl.class.dir", properties));
      TDL_DEPENDENCY_DIR = convertToAbsolutePathOrNull(getSystemPropertyOrDefault("tdl.dependency.dir", properties));
      MTDL_JAR_PATH = convertToAbsolutePathOrNull(getSystemPropertyOrDefault("mtdl.jar.path", properties));
      MVN_SETTINGS_XML = convertToAbsolutePathOrNull(getSystemPropertyOrDefault("mvn.settings.xml", properties));

      if (TDL_SOURCE_DIR == null) {
        TDL_SOURCE_DIR = new File("data/tdlsrc").getAbsolutePath();
      }

      if (TDL_CLASS_DIR == null) {
        TDL_CLASS_DIR = new File("data/tdlcls").getAbsolutePath();
      }

      if (TDL_DEPENDENCY_DIR == null) {
        TDL_DEPENDENCY_DIR = new File("data/dependencies").getAbsolutePath();
      }

      if (MTDL_JAR_PATH == null) {
        MTDL_JAR_PATH = new File("conf/mtdl.jar").getAbsolutePath();
      }

      if (MVN_SETTINGS_XML == null) {
        MVN_SETTINGS_XML = new File("conf/maven-settings.xml").getAbsolutePath();
      }

      TDL_SOURCE_DIR = removeQuotes(TDL_SOURCE_DIR);
      TDL_CLASS_DIR = removeQuotes(TDL_CLASS_DIR);
      TDL_DEPENDENCY_DIR = removeQuotes(TDL_DEPENDENCY_DIR);
      MTDL_JAR_PATH = removeQuotes(MTDL_JAR_PATH);
      MVN_SETTINGS_XML = removeQuotes(MVN_SETTINGS_XML);

      LOGGER.info("TDL_SOURCE_DIR: " + TDL_SOURCE_DIR);
      LOGGER.info("TDL_CLASS_DIR: " + TDL_CLASS_DIR);
      LOGGER.info("TDL_DEPENDENCY_DIR: " + TDL_DEPENDENCY_DIR);
      LOGGER.info("MTDL_JAR_PATH: " + MTDL_JAR_PATH);
      LOGGER.info("MVN_SETTINGS_XML: " + MVN_SETTINGS_XML);
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
      throw new IllegalStateException(ex);
    }
  }
}
