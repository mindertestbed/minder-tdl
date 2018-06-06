package dependencyutils;

import com.yerlibilgin.commons.FileUtils;
import com.yerlibilgin.dependencyutils.MavenSettingsFile;
import com.yerlibilgin.dependencyutils.MavenSettingsReader;
import java.io.File;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author yerlibilgin
 */
public class MavenSettingsReaderTest {

  private static final String defaultFile = "default-maven-settings.xml";
  private static final String settings0 = "maven.settings.0.xml";
  private static final String settings1 = "maven.settings.1.xml";
  private static final String settings2 = "maven.settings.2.xml";
  private static final String settings3 = "maven.settings.3.xml";
  private static final String settings4 = "maven.settings.4.xml";

  @BeforeClass
  public static void init() {
    final Class<MavenSettingsReaderTest> clazz = MavenSettingsReaderTest.class;
    FileUtils.transferResourceToFile(clazz, MavenSettingsReaderTest.defaultFile, MavenSettingsReaderTest.defaultFile);
    FileUtils.transferResourceToFile(clazz, MavenSettingsReaderTest.settings0, MavenSettingsReaderTest.settings0);
    FileUtils.transferResourceToFile(clazz, MavenSettingsReaderTest.settings1, MavenSettingsReaderTest.settings1);
    FileUtils.transferResourceToFile(clazz, MavenSettingsReaderTest.settings2, MavenSettingsReaderTest.settings2);
    FileUtils.transferResourceToFile(clazz, MavenSettingsReaderTest.settings3, MavenSettingsReaderTest.settings3);
    FileUtils.transferResourceToFile(clazz, MavenSettingsReaderTest.settings4, MavenSettingsReaderTest.settings4);
  }


  @AfterClass
  public static void destroy() {
    new File(defaultFile).delete();
    new File(settings0).delete();
    new File(settings1).delete();
    new File(settings2).delete();
    new File(settings3).delete();
    new File(settings4).delete();
  }

  @Test
  public void readDefaultSettings() {
    final MavenSettingsFile mavenSettingsFile = MavenSettingsReader.readSettingsFile(defaultFile);
    final List<String[]> strings = mavenSettingsFile.repositoryList;
    Assert.assertEquals(2, strings.size());
  }

  @Test
  public void readSettingsWithoutLocalRepoOffline() {
    final MavenSettingsFile mavenSettingsFile = MavenSettingsReader.readSettingsFile(settings0);
    final List<String[]> strings = mavenSettingsFile.repositoryList;
    Assert.assertEquals(0, strings.size());
  }

  @Test
  public void readSettingsWithLocalRepoOffline() {
    final MavenSettingsFile mavenSettingsFile = MavenSettingsReader.readSettingsFile(settings1);
    System.out.println(mavenSettingsFile.localRepository);
    final List<String[]> strings = mavenSettingsFile.repositoryList;
    Assert.assertEquals(0, strings.size());
  }

  @Test
  public void readSettingsWithLocalRepo() {
    final MavenSettingsFile mavenSettingsFile = MavenSettingsReader.readSettingsFile(settings2);
    final List<String[]> strings = mavenSettingsFile.repositoryList;
    Assert.assertEquals(1, strings.size());
    for (String[] repo : strings) {
      System.out.println(repo[0]);
      System.out.println(repo[1]);
      System.out.println(repo[2]);
    }
  }

  @Test
  public void readSettingsWithActiveProfilesTag() {
    final MavenSettingsFile mavenSettingsFile = MavenSettingsReader.readSettingsFile(settings3);
    final List<String[]> strings = mavenSettingsFile.repositoryList;
    Assert.assertEquals(2, strings.size());
    for (String[] repo : strings) {
      System.out.println(repo[0]);
      System.out.println(repo[1]);
      System.out.println(repo[2]);
    }
  }

  @Test
  public void readSettingsWithActiveByDefault() {
    final MavenSettingsFile mavenSettingsFile = MavenSettingsReader.readSettingsFile(settings4);
    final List<String[]> strings = mavenSettingsFile.repositoryList;
    Assert.assertEquals(3, strings.size());
    for (String[] repo : strings) {
      System.out.println(repo[0]);
      System.out.println(repo[1]);
      System.out.println(repo[2]);
    }
  }
}