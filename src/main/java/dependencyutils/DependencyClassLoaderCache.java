package dependencyutils;

import mtdl.Param;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * This cache maps the dependency service instances WRT their dependency strings
 * to save memory and speed
 *
 * @author: yerlibilgin
 * @date: 06/08/15.
 */
public class DependencyClassLoaderCache {
  private static HashMap<String, DependencyClassLoader> cache = new HashMap<>();

  public static DependencyClassLoader getDependencyClassLoader(String dependencyString) {
    if (!cache.containsKey(dependencyString)) {
      DependencyService dependencyService = new DependencyService(dependencyString);
      List<URL> dependecyBufferList = new ArrayList<>();
      dependencyService.getClassPathString();
      List<String> additionalJars = dependencyService.allResolvedDependencies;
      if (additionalJars != null) {
        for (String currentJar : additionalJars) {
          String fileJar = "file://" + currentJar;
          try {
            dependecyBufferList.add(new URL(fileJar));
          } catch (Throwable th) {
            throw new RuntimeException("Invalid URL " + fileJar);
          }
        }
      }
      final DependencyClassLoader loader = new DependencyClassLoader(dependecyBufferList.toArray(new URL[]{}));
      loader.setClassPathString(dependencyService.getClassPathString());

      cache.put(dependencyString, loader);
    }
    return cache.get(dependencyString);
  }


  @Override
  public String toString() {
    return "MTDL Extra Dependency Class Loader";
  }
}
