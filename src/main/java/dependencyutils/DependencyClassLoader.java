package dependencyutils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 06/08/15.
 */
public class DependencyClassLoader extends URLClassLoader{
  private String classPathString;

  public DependencyClassLoader(URL[] urls) {
    super(urls);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    return super.loadClass(name, resolve);
  }

  public void setClassPathString(String classPathString) {
    this.classPathString = classPathString;
  }

  public String getClassPathString() {
    return classPathString;
  }
}
