package dependencyutils;

import com.yerlibilgin.commons.FileUtils;
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
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ozgurmelis on 25/05/15.
 */
public class RepositoryManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryManager.class);
  private static RepositoryManager instance = new RepositoryManager();


  private RepositorySystem repositorySystem;
  private List<RemoteRepository> repositoryList;
  private DefaultRepositorySystemSession session;

  private RepositoryManager() {
    initializeRepositorySystem();
  }

  public void initializeRepositorySystem() {
    //check if we have a local maven repository and use it as a base,
    final File m2Dir = new File(System.getProperty("user.home") + "/.m2/repository/");
    if (m2Dir.exists()) {
      LOGGER.warn("Using the default " + m2Dir.getAbsolutePath() + " as local repo");
      createRepositorySystem(m2Dir.getAbsolutePath() + File.separator);
    } else {
      LOGGER.warn("Using the default " + MTDLConfig.TDL_DEPENDENCY_DIR + " as local repo");
      //we don't have an .m2 in home. So use the configured dependency dir
      createRepositorySystem(MTDLConfig.TDL_DEPENDENCY_DIR + File.separator);
    }

    //add more repositories if they are configured in a mvn.repositories file
    if (MTDLConfig.MVN_SETTINGS_XML != null) {
      final File mvnSettingsXMLFile = new File(MTDLConfig.MVN_SETTINGS_XML);
      if (mvnSettingsXMLFile.exists()) {
        List<String[]> repositoryList = MavenSettingsReader.readSettingsFile(mvnSettingsXMLFile.getAbsolutePath());
        for(String []repository : repositoryList){
          addRepository(repository[0], repository[1], repository[2]);
        }
      } else {
        //there is no maven settings file. Go for the default one
        FileUtils.transferResourceToFile(this.getClass(), "/" + MTDLConfig.DEFAULT_MVN_SETTINGS_XML, MTDLConfig.DEFAULT_MVN_SETTINGS_XML);
        List<String[]> repositoryList = MavenSettingsReader.readSettingsFile(MTDLConfig.DEFAULT_MVN_SETTINGS_XML);
        for(String []repository : repositoryList){
          addRepository(repository[0], repository[1], repository[2]);
        }
      }
    }
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

  private void downloadArtifactWithAllDependencies(String artifactInfo) throws DependencyResolutionException {
    /**
     * Specify the artifact eg. "org.apache.maven:maven-aether-provider:3.1.0"
     */
    //Artifact artifact = new DefaultArtifact( groupID + ":" + artifactID + ":" + version);
    Artifact artifact = new DefaultArtifact(artifactInfo);

    DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);

    CollectRequest collectRequest = new CollectRequest();
    collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
    collectRequest.setRepositories(repositoryList);

    DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);

    List<ArtifactResult> artifactResults = repositorySystem.resolveDependencies(session, dependencyRequest).getArtifactResults();

    for (ArtifactResult artifactResult : artifactResults) {
      //System.out.println( artifactResult.getArtifact() + " resolved to " + artifactResult.getArtifact().getFile() );
      allResolvedDependencies.add(artifactResult.getArtifact().getFile() + "");
    }
  }

  /**
   * Generates the classpath for updating dependencies dynamically.
   */
  private String generateClassPathStringForArtifact() {
    String classPathString = "";
    for (Iterator<String> i = allResolvedDependencies.iterator(); i.hasNext(); ) {
      String item = i.next();
      classPathString += item + File.pathSeparator;
    }

    return classPathString;
  }

  public static RepositoryManager getInstance() {
    return instance;
  }

  public DependencyResult resolveDependencies(DependencyRequest dependencyRequest) {
    session
  }
}