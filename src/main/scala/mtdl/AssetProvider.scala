package mtdl

import java.io.{FileInputStream, BufferedInputStream}

/**
 * Created by yerlibilgin on 18/05/15.
 */
class AssetProvider(val source: String) {
  def getValue(): Array[Byte] = {
    val bis = new BufferedInputStream(new FileInputStream(MTDLConfig.ASSET_DIR + "/" + source))
    val bArray = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
    bArray
  }

  def apply() : Array[Byte] = {
    getValue()
  }

  def onto(out: Int) = {
    val p = ParameterPipe(-1, out - 1);
    //whatever happens, return the value.
    p.select = (a: Any) => getValue()
    p
  }
}
