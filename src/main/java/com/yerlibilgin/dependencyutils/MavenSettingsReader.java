package com.yerlibilgin.dependencyutils;

import com.yerlibilgin.ValueChecker;
import com.yerlibilgin.XMLUtils;
import com.yerlibilgin.XPathUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author yerlibilgin
 */
public class MavenSettingsReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(MavenSettingsReader.class);

  /**
   * A wrapper method around the {@link MavenSettingsReader#readSettings} method
   *
   * @param mvnSettingsXmlFileName the file to be read as the maven settings.xml (Not Null)
   *
   * @return the parsed maven settings
   */
  public static MavenSettingsFile readSettingsFile(String mvnSettingsXmlFileName) {
    ValueChecker.notNull(mvnSettingsXmlFileName, "mvnSettingsXmlFileName");
    LOGGER.debug("Read maven settings file " + mvnSettingsXmlFileName);
    File file = new File(mvnSettingsXmlFileName);
    if (file.exists()) {
      try {
        return readSettings(new FileInputStream(mvnSettingsXmlFileName));
      } catch (FileNotFoundException e) {
        throw new IllegalStateException(e);
      }
    } else {
      throw new IllegalArgumentException(mvnSettingsXmlFileName + " does not exist");
    }
  }

  /**
   * Parse the /settings/activeProfiles tag to get the names of the active profiles, and
   * add the profiles that have the tag activeByDefault set to true.
   * <p>
   * and finally check the -P system property to get the active profile from there,
   * create a set of Nodes from this process.
   */
  private static Set<Node> listActiveProfiles(Document document) {
    //TODO: check more complicated activation scenarios in the future
    final HashSet<Node> activeProfileSet = new HashSet<>();

    final List<Node> activeProfileNodeNames = XPathUtils.listNodes(document, "/settings/activeProfiles//activeProfile");

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Active profiles enumerated using xpath /settings/activeProfiles//activeProfile");
      for (Node activeProfileNodeName : activeProfileNodeNames) {
        LOGGER.trace(activeProfileNodeName.getTextContent());
      }
    }

    for (Node node : activeProfileNodeNames) {
      final String xpath = "//profile/id[text() = '" + node.getTextContent() + "']/ancestor::profile";
      final List<Node> activeProfileNodes = XPathUtils.listNodes(document, xpath);
      activeProfileSet.addAll(activeProfileNodes);

      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Active profiles enumerated using xpath " + xpath);
        for (Node node1 : activeProfileNodes) {
          LOGGER.trace(node1.getTextContent());
        }
      }

    }

    //add the nodes that have an <activeByDefault>true</activeByDefault> setting
    final String xpath = "//profile/activation/activeByDefault[text() = 'true']/ancestor::profile";
    final List<Node> activeByDefaultProfiles = XPathUtils.listNodes(document, xpath);
    activeProfileSet.addAll(activeByDefaultProfiles);

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Active profiles enumerated using xpath " + xpath);
      for (Node node : activeByDefaultProfiles) {
        LOGGER.trace(node.getTextContent());
      }
    }

    return activeProfileSet;
  }
  /**
   * <p>
   * Parse the provided stream as a maven settings.xml and return the repositories that have <activeByDefault>true</activeByDefault>
   * or are active with respect to the system property -P&lt;profile&gt;
   * </p>
   * <p>
   * if there is an <b>&lt;offline&gt</b> declaration, then the online repositories won't be listed
   * </p>
   * <p>
   * if there is a <b>&lt;localRepository&gt</b> declaration, it will be added to the list
   * </p>
   *
   * @param settingsStream
   *     the stream that contains the data as settings.xml file, not null.
   * @return the list of active repository URLS (including the local repository if declared). <br/>
   * Each element is an array of three Strings, where the first element is the repository id, the second is type and the third is the URL.
   */
  public static MavenSettingsFile readSettings(InputStream settingsStream) {
    ValueChecker.notNull(settingsStream, "settings file/resource input stream");
    MavenSettingsFile mavenSettingsFile = new MavenSettingsFile();
    List<String[]> repoList = new ArrayList<>();
    try {
      final Document document = XMLUtils.parse(settingsStream);

      //try to parse local repository
      //<localRepository>/path/to/local/repo</localRepository>
      Node localRepoNode = XPathUtils.findSingleNode(document, "/settings/localRepository");
      if (localRepoNode != null) {
        LOGGER.debug("A local repo is defined. Check if it exists");
        final String textContent = localRepoNode.getTextContent();
        if (textContent != null && textContent.length() != 0) {
          File localRepository = new File(textContent.trim());
          if (localRepository.exists()) {
            mavenSettingsFile.localRepository = localRepository;
          } else {
            LOGGER.warn("Local repository " + textContent + " defined but does not exist. Try to create it");
            if(!localRepository.mkdirs()){
              throw new IllegalStateException("Couldn't create repository for " + localRepository.getAbsolutePath());
            }


            mavenSettingsFile.localRepository = localRepository;
          }
        }
      }

      //if we are not offline, the read the rest
      Node offline = XPathUtils.findSingleNode(document, "/settings/offline");
      if (offline == null || !offline.getTextContent().equalsIgnoreCase("true")) {

        //check and possibly get the names of active profiles
        Set<Node> activeProfiles = listActiveProfiles(document);

        //get the repository elements from those active profiles
        for (Node activeProfileNode : activeProfiles) {
          //TODO: check releases-snapshots in the future
          final List<Node> repositoryNodes = XPathUtils.listNodes(activeProfileNode, "repositories//repository");

          for (Node repositoryNode : repositoryNodes) {
            Element repositoryElement = (Element) repositoryNode;

            try {
              final String id = repositoryElement.getElementsByTagName("id").item(0).getTextContent();
              final String url = repositoryElement.getElementsByTagName("url").item(0).getTextContent();
              repoList.add(new String[]{id, "default", url});
            } catch (Exception ex) {
              LOGGER.warn("Problem reading " + repositoryNode.getTextContent());
              //skip that. don't quit
            }
          }
        }

      } else {
        LOGGER.debug("the maven settings are in offline mode. So no online repo will be added to the list");
      }

    } catch (Exception ex) {
      throw new IllegalArgumentException(ex);
    }

    mavenSettingsFile.repositoryList = repoList;
    return mavenSettingsFile;
  }
}
