package mtdl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.yerlibilgin.commons.ConfigurationReader;
import com.yerlibilgin.commons.FileUtils;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yerlibilgin
 */
public class MTDLConfig extends ConfigurationReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(MTDLConfig.class);

  public static String MINDER_DATA_DIR;
  public static String TDL_SOURCE_DIR;
  public static String TDL_CLASS_DIR;
  public static String ASSET_DIR;
  public static String MVN_SETTINGS_XML;

  public static String MTDL_JAR_PATH;

  static {
    try {

      String mtdlConfig = System.getProperty("mtdl.config.file", "./conf/application.conf");

      Config config;
      final File configFile = new File(mtdlConfig);
      if (configFile.exists()) {
        config = ConfigFactory.parseFile(configFile);
      } else {
        //try to load from resource
        mtdlConfig = System.getProperty("mtdl.config.resource", "application.conf");

        final InputStream resourceStream = MTDLConfig.class.getResourceAsStream(mtdlConfig);
        if (resourceStream != null) {
          config = ConfigFactory.parseReader(new InputStreamReader(resourceStream));
        } else {
          throw new IllegalStateException("Coudln't get configuration");
        }
      }

      config = config.resolve();

      MINDER_DATA_DIR = getStringOrDefault(config.getString("minder.data.dir"), "./data");
      new File(MINDER_DATA_DIR).mkdirs();

      MVN_SETTINGS_XML = getStringOrDefault(config.getString("minder.maven.xml"), MINDER_DATA_DIR + "/maven-settings.xml");
      MTDL_JAR_PATH = getStringOrDefault(config.getString("mtdl.jar.path"), "./mtdl.jar");
      TDL_SOURCE_DIR = getStringOrDefault(config.getString("minder.tdlsrc.dir"), MINDER_DATA_DIR + "/tdlsrc");
      TDL_CLASS_DIR = getStringOrDefault(config.getString("minder.tdlcs.dir"), MINDER_DATA_DIR + "/tdlcls");
      ASSET_DIR = getStringOrDefault(config.getString("minder.assets.dir"), MINDER_DATA_DIR + "/assets");

      //convert paths to absolute path
      TDL_SOURCE_DIR = new File(TDL_SOURCE_DIR).getAbsolutePath();
      TDL_CLASS_DIR = new File(TDL_CLASS_DIR).getAbsolutePath();
      ASSET_DIR = new File(ASSET_DIR).getAbsolutePath();
      MVN_SETTINGS_XML = new File(MVN_SETTINGS_XML).getAbsolutePath();
      MTDL_JAR_PATH = new File(MTDL_JAR_PATH).getAbsolutePath();

      LOGGER.info("TDL_SOURCE_DIR: " + TDL_SOURCE_DIR);
      LOGGER.info("TDL_CLASS_DIR: " + TDL_CLASS_DIR);
      LOGGER.info("MTDL_JAR_PATH: " + MTDL_JAR_PATH);
      LOGGER.info("MVN_SETTINGS_XML: " + MVN_SETTINGS_XML);
      LOGGER.info("ASSET_DIR: " + ASSET_DIR);

    } catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
      throw new IllegalStateException(ex);
    }
  }

  private static String getStringOrDefault(String string, String _default) {
    if (string != null) {
      return string;
    }

    return _default;
  }
}
