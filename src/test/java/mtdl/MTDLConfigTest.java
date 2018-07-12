package mtdl;

import static mtdl.MTDLConfig.MTDL_JAR_PATH;
import static mtdl.MTDLConfig.MVN_SETTINGS_XML;
import static mtdl.MTDLConfig.TDL_CLASS_DIR;

import com.yerlibilgin.commons.FileUtils;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yerlibilgin
 */
public class MTDLConfigTest {

  static {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");

  }

  static Logger LOGGER = LoggerFactory.getLogger(MTDLConfig.class);

  @Test
  public void testMTDLConfig() {
    FileUtils.transferResourceToFile(this.getClass(), "sample-mtdl.conf", "sample-mtdl.conf");
    System.setProperty("mtdlConfig.properties", "sample-mtdl.conf");
    LOGGER.info("TDL_SOURCE_DIR: " + MTDLConfig.TDL_SOURCE_DIR);
    LOGGER.info("TDL_CLASS_DIR: " + TDL_CLASS_DIR);
    LOGGER.info("MTDL_JAR_PATH: " + MTDL_JAR_PATH);
    LOGGER.info("MVN_SETTINGS_XML: " + MVN_SETTINGS_XML);
    System.out.println(MTDLConfig.MVN_SETTINGS_XML);

    final File tdlsrc = new File("data/tdlsrc");
    tdlsrc.mkdirs();

    final File cls = new File("data/a/b/d/s/s/cc");
    final File dep = new File("data/dependencies");
    final File mdtl = new File("anotherone/mtdl.jar");
    final File set = new File("somedirectory/maven-settings.xml");

    Assert.assertEquals(tdlsrc.getAbsolutePath(), MTDLConfig.TDL_SOURCE_DIR);
    Assert.assertEquals(cls.getAbsolutePath(), MTDLConfig.TDL_CLASS_DIR);
    Assert.assertEquals(mdtl.getAbsolutePath(), MTDLConfig.MTDL_JAR_PATH);
    Assert.assertEquals(set.getAbsolutePath(), MTDLConfig.MVN_SETTINGS_XML);

  }
}