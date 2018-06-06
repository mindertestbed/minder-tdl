package com.yerlibilgin.dependencyutils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 06/08/15.
 */
public class DependencyClassLoader extends URLClassLoader {

  private String classPathString;

  public DependencyClassLoader(List<URL> dependencyList) {
    super(dependencyList.toArray(new URL[]{}));

    StringBuilder stringBuilder = new StringBuilder();
    for (URL url : dependencyList) {
      stringBuilder.append(url.toString()).append(File.pathSeparator);
    }

    //remove the last file separator
    if (stringBuilder.length() > 0) {
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    }

    classPathString = stringBuilder.toString();
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    return super.loadClass(name, resolve);
  }

  public String getClassPathString() {
    return classPathString;
  }
}
