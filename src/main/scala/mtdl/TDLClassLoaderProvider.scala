package mtdl

import java.net.{URL, URLClassLoader}
import java.util

import dependencyutils.{DependencyClassLoader, DependencyService}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

object TDLClassLoaderProvider {
  /**
   * A list of externally supplied class loaders for class resolution
   */
  val externalClassLoaders = new util.ArrayList[ClassLoader];

  /**
   *
   * @param clzL
   */
  def appendExternalClassLoader(clzL: ClassLoader): Unit = {
    externalClassLoaders.add(clzL)
  }

  val dir = new java.io.File("tdlcls/");
  dir.mkdirs()

  implicit def str2URL(s: String) = new URL(s)

  def loadClass(name: String, dependencyClassLoader: DependencyClassLoader): Class[_] = {
    var dependecyBufferList = new ListBuffer[URL]()
    dependecyBufferList += dir.toURI.toURL
    val cl = new DefaultTDLClassLoader(dependecyBufferList.toArray, dependencyClassLoader)
    cl.loadClass(name)
  }
}
