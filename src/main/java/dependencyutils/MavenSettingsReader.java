package dependencyutils;

import com.yerlibilgin.XMLUtils;
import com.yerlibilgin.XPathUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.xml.crypto.NodeSetData;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author yerlibilgin
 */
public class MavenSettingsReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(MavenSettingsReader.class);

  /**
   * <p>
   * Parse the provided maven settings.xml and return the repositories that have <activeByDefault>true</activeByDefault>
   * or are active with respect to the system property -P&lt;profile&gt;
   * </p>
   * <p>
   * if there is an <b>&lt;offline&gt</b> declaration, then the online repositories won't be listed
   * </p>
   * <p>
   * if there is a <b>&lt;localRepository&gt</b> declaration, it will be added to the list
   * </p>
   *
   * @param mvnSettingsXmlFileName
   *     the path to the settings.xml file
   * @return the list of active repository URLS (including the local repository if declared). <br/>
   * Each element is an array of three Strings, where the first element is the repository id, the second is type and the third is the URL.
   */
  public static List<String[]> readSettingsFile(String mvnSettingsXmlFileName) {
    LOGGER.debug("Read maven settings file " + mvnSettingsXmlFileName);
    List<String[]> repoList = new ArrayList<>();
    try {
      final Document document = XMLUtils.parseXmlFile(mvnSettingsXmlFileName);

      //try to parse local repository
      //<localRepository>/path/to/local/repo</localRepository>
      Node localRepoNode = XPathUtils.findSingleNode(document, "/settings/localRepository");
      if (localRepoNode != null) {
        LOGGER.debug("Add local repo " + localRepoNode.getTextContent());
        repoList.add(new String[]{"localrepo", "default", localRepoNode.getTextContent()});
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

    return repoList;
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
}
