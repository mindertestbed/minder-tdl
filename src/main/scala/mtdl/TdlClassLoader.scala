package mtdl

import java.net.{URL, URLClassLoader}

import scala.collection.mutable

object TdlClassLoader {
  val dir = new java.io.File("tdlcls/");
  dir.mkdirs()

  var clsLoader = Thread.currentThread().getContextClassLoader

  var sysList = new mutable.MutableList[URL]
  var clzz = clsLoader

  do {
    if (clzz.isInstanceOf[URLClassLoader]) {
      val urlcsz = clzz.asInstanceOf[URLClassLoader]
      for (url <- urlcsz.getURLs) {
        sysList += url
      }
    }
    clzz = clzz.getParent
  } while (clzz != null)
  val urls = Array(dir.toURI.toURL);

  val lst = List

  def loadClass(name: String): Class[_] = {
    val cl = new Ldr(urls)
    cl.loadClass(name)
  }

  class Ldr(urls: Array[URL]) extends URLClassLoader(urls: Array[URL]) {
    override def loadClass(name: String): Class[_] = {
      if (name.startsWith(TdlCompiler.MINDERTDL_PACKAGE_NAME)) {
        super.loadClass(name)
      } else {
        TdlClassLoader.getClass.getClassLoader.loadClass(name)
      }
    }

    override def loadClass(name: String, resolve: Boolean): Class[_] = {
      if (name.startsWith(TdlCompiler.MINDERTDL_PACKAGE_NAME)) {
        super.loadClass(name, resolve)
      } else {
        TdlClassLoader.getClass.getClassLoader.loadClass(name)
      }
    }
  }

}