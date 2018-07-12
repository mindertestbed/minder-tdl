package com.yerlibilgin.dependencyutils;

import java.io.InputStream;
import mtdl.MTDLConfig;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a singleton that manages a local maven repository for dynamic discovery of artifacts.
 *
 * @author ozgurmelis
 * @author yerlibilgin
 */
public class RepositoryManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryManager.class);

  private static RepositoryManager instance = new RepositoryManager();
  /**
   * Singleton method
   * @return
   */
  public static RepositoryManager getInstance() {
    return instance;
  }

  private RepositorySystem repositorySystem;
  private List<RemoteRepository> repositoryList;
  private DefaultRepositorySystemSession session;

  private RepositoryManager() {
    initializeRepositorySystem();
  }



  /**
   * Creates a local maven repository system using a possibly existing maven settings.xml
   * (or using a default one otherwise).
   * <p>
   * The maven settings xml file must be defined in {@link MTDLConfig#MVN_SETTINGS_XML}.
   */
  public void initializeRepositorySystem() {

    //check the maven settings xml, if it exists, process with respect to it
    //add more repositories if they are configured in a mvn.repositories file
    File settingsFile;
    if (MTDLConfig.MVN_SETTINGS_XML != null && (settingsFile = new File(MTDLConfig.MVN_SETTINGS_XML)).exists()) {
      LOGGER.debug("maven settings file " + MTDLConfig.MVN_SETTINGS_XML + " exists. Processing it...");
      MavenSettingsFile mavenSettingsFile = MavenSettingsReader.readSettingsFile(settingsFile.getAbsolutePath());

      //define the local m2 dir
      if (mavenSettingsFile.localRepository != null) {
        LOGGER.debug("Local repository discovered: " + mavenSettingsFile.localRepository + ". Create repository system on it");
        createRepositorySystem(mavenSettingsFile.localRepository + File.separator);
      } else {
        //no local repository defined, use the local ~/.m2
        LOGGER.debug("No local repository definition found, try to create a repo at ~/.m2");
        createRepositorySystemOnLocalM2();
      }

      for (String[] repository : mavenSettingsFile.repositoryList) {
        addRepository(repository[0], repository[1], repository[2]);
      }
    } else {
      //we don't have a settings file, try to discover the ~/.m2/repository with http://central.maven.org/maven2/
      LOGGER.debug("mvn settings file doesn't exist.");
      tryToResolveLocalM2WithDefaultXML();
    }
  }

  private void tryToResolveLocalM2WithDefaultXML() {
    LOGGER.debug("Try to resolve ~/.m2 with default XML settings resource");
    createRepositorySystemOnLocalM2();

    //Read the deafult settings resource
    final InputStream stream = this.getClass().getResourceAsStream("/" + MTDLConfig.MVN_SETTINGS_XML);
    if (stream != null) {
      MavenSettingsFile mavenSettingsFile = MavenSettingsReader.readSettings(stream);
      for (String[] repository : mavenSettingsFile.repositoryList) {
        addRepository(repository[0], repository[1], repository[2]);
      }
    } else {
      //the last resort, add central manually
      LOGGER.warn("All repo resolution efforts failed. Use http://central.maven.org/maven2/ as default");
      addRepository("central", "default", "http://central.maven.org/maven2/");
    }
  }

  /**
   * Create ~/.m2/repository if it doesn't exist and use that as the maven local repo
   *
   * @throws IllegalStateException
   *     if the ~/.m2 doesn't exist and cannot be created
   */
  private void createRepositorySystemOnLocalM2() {
    final File m2Dir = new File(System.getProperty("user.home") + "/.m2/repository/");
    if (!m2Dir.exists() && !m2Dir.mkdirs()) {
      throw new IllegalStateException("Cannot create ~/.m2/repository");
    }

    createRepositorySystem(m2Dir.getAbsolutePath() + File.separator);
  }


  private void createRepositorySystem(String localRepoDirectory) {
    /**
     * Creates a repository system that can maintain many repositories
     */
    repositorySystem = Booter.newRepositorySystem();

    /**
     * Session properties (such as where to download etc.)
     */
    session = Booter.newRepositorySystemSession(repositorySystem, localRepoDirectory);
    session.setConfigProperty(ConflictResolver.CONFIG_PROP_VERBOSE, true);
    session.setConfigProperty(DependencyManagerUtils.CONFIG_PROP_VERBOSE, true);

    repositoryList = new ArrayList<>();
  }


  public void addRepository(String id, String type, String url) {
    repositoryList.add(Booter.addRepository(id, type, url));
  }

  public List<ArtifactResult> resolveDependencies(String artifactInfo) {
    try {
      Artifact artifact = new DefaultArtifact(artifactInfo);
      DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
      CollectRequest collectRequest = new CollectRequest();
      collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
      collectRequest.setRepositories(repositoryList);
      DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);
      return repositorySystem.resolveDependencies(session, dependencyRequest).getArtifactResults();
    } catch (DependencyResolutionException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }
}