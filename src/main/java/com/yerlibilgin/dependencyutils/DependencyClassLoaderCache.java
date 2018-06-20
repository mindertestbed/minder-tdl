package com.yerlibilgin.dependencyutils;

import java.net.URL;
import java.util.*;

/**
 * This cache maps the dependency service instances WRT their dependency strings to save memory and speed
 *
 * @author: yerlibilgin
 * @date: 06/08/15.
 */
public class DependencyClassLoaderCache {

  /**
   * A map that is used for caching the previous resolutions. <br/>
   * For each dependency request the created classloaded is put into this map with the
   * hashCode() of the dependency string it was requested for.
   *
   * This might be bad if some dependencies are to be resolved later.
   * Maybe an expiry system might be added
   */
  private static HashMap<Integer, DependencyClassLoader> cache = new HashMap<>();

  /**
   * Return the class loader for that dependency string by resolving the necessary artifacts from
   * the underlying maven system
   *
   * @param dependencyString the new line list of
   * @return
   */
  public static DependencyClassLoader getDependencyClassLoader(String dependencyString) {
    if (!cache.containsKey(dependencyString.hashCode())) {
      DependencyResolver dependencyResolver = new DependencyResolver();
      List<URL> dependencyList = dependencyResolver.resolve(dependencyString);
      final DependencyClassLoader loader = new DependencyClassLoader(dependencyList);
      cache.put(dependencyString.hashCode(), loader);
    }
    return cache.get(dependencyString.hashCode());
  }


  @Override
  public String toString() {
    return "MTDL-maven-dependency-class-loader";
  }
}
