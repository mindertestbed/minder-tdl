package mtdl

import java.net.{URL, URLClassLoader}

import com.yerlibilgin.dependencyutils.DependencyClassLoader
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

/*
 * @author: yerlibilgin
 * @date:   06/08/15.
 */
class DefaultTDLClassLoader(urls: Array[URL], dependencyClassLoader: DependencyClassLoader) extends URLClassLoader(urls) {

  val LOGGER = LoggerFactory.getLogger(getClass)

  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    if (name.startsWith(TdlCompiler.MINDERTDL_PACKAGE_NAME)) {
      LOGGER.trace(name + " superload")
      super.loadClass(name, resolve)
    } else {
      LOGGER.trace(name + " defaultload")

      import scala.util.control.Breaks._

      var clz: Class[_] = null

      if (clz == null) {
        breakable {
          LOGGER.trace("Try external class loaders: " + TDLClassLoaderProvider.externalClassLoaders.size())
          for (cl: ClassLoader <- TDLClassLoaderProvider.externalClassLoaders) {
            LOGGER.trace("Try " + cl.getClass().getName() + " for " + name)
            try {
              clz = Class.forName(name, true, cl);
            } catch {
              case th: Throwable => {}
            }
            if (clz != null) {
              LOGGER.trace(name + " hit " + cl)
              break;
            }
          }
        }
      }

      if (clz == null && dependencyClassLoader != null) {
        //try me one last time
        LOGGER.trace("Probably this is a group dependency, Try dependency loader")
        try {
          clz = dependencyClassLoader.loadClass(name)
        }catch {case  _ => {}}
      }

      if (clz == null){
        throw new NoClassDefFoundError(name);
      }

      clz
    }
  }
}