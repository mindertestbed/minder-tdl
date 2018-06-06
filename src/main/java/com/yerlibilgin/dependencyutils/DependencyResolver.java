package com.yerlibilgin.dependencyutils;

import com.yerlibilgin.ValueChecker;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * @author yerlibilgin
 */
public class DependencyResolver {

  /**
   * Resolves the given <code>dependencyString</code> that has the form
   * <br/>
   * <b>groupid:artifactId:version</b><br/>
   * <b>groupid:artifactId:version</b><br/>
   * <b>...</b><br/>
   * <b>...</b><br/>
   * <br/>
   *
   * @param dependencyString
   *     the list of dependencies delimited per lines
   * @return The list of resolved dependencies, or empty List
   * @throws NullPointerException
   *     with a proper message if the dependencyString is null
   */
  public List<URL> resolve(String dependencyString) {
    ValueChecker.notNull(dependencyString, "dependencyString");
    String[] dependencies = dependencyString.split("\\n");

    if (dependencies.length == 0) {
      return Collections.EMPTY_LIST;
    }

    List<URL> resolvedDepedencies = new ArrayList<>();
    for (String dependency : dependencies) {
      List<ArtifactResult> artifactResults = RepositoryManager.getInstance().resolveDependencies(dependency);
      for (ArtifactResult artifactResult : artifactResults) {
        try {
          resolvedDepedencies.add(artifactResult.getArtifact().getFile().toURI().toURL());
        } catch (MalformedURLException e) {
        }
      }
    }
    return resolvedDepedencies;
  }
}