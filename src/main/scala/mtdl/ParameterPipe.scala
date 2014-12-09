package mtdl

/**
 * A pipe that is built in the TDL that defines which parameter will be map to which one.
 *
 * All the parameter references and signal-slot references are hold here.
 *
 * This is the main locigal unit of a Rivet.
 *
 * Created by yerlibilgin on 05/12/14.
 */
case class ParameterPipe(in: Int, out: Int) {
  /**
   * An actual reference to the optional in parameter.
   */
  private var _inRef: Param = null;

  def inRef = _inRef;

  def inRef_=(inRef: Param) {
    _inRef = inRef
  }

  /**
   * The actual reference to the mandatory out parameter
   */
  private var _outRef: Param = null;

  def outRef = _outRef;

  def outRef_=(outRef: Param) {
    _outRef = outRef
  }


  var converter: (AnyRef => AnyRef) = (a: AnyRef) => {
    a
  }

  def using(converter: AnyRef => AnyRef) = {
    this.converter = converter
    this
  }
}

