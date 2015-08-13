package mtdl

import java.net.{URLClassLoader, URL}

import scala.collection.JavaConversions._
import dependencyutils.DependencyClassLoader

/*
 * @author: yerlibilgin
 * @date:   06/08/15.
 */
class DefaultTDLClassLoader(urls: Array[URL], dependencyClassLoader: DependencyClassLoader) extends URLClassLoader(urls) {
  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    if (name.startsWith(TdlCompiler.MINDERTDL_PACKAGE_NAME)) {
      println(name + " superload")
      super.loadClass(name, resolve)
    } else {
      println(name + " defaultload")

      import scala.util.control.Breaks._

      var clz: Class[_] = null
      breakable {
        for (cl: ClassLoader <- TDLClassLoaderProvider.externalClassLoaders) {
          println("Try " + cl.getClass().getName() + " for " + name)
          try {
            clz = Class.forName(name, true, cl);
          } catch {
            case th: Throwable => {}
          }
          if (clz != null) {
            println(name + " hit " + cl)
            break;
          }
        }
      }

      if (clz == null) {
        //try me one last time
        println("Probably this is a group dependency, Try dependency loader")
        clz = dependencyClassLoader.loadClass(name)
      }
      clz
    }
  }
}