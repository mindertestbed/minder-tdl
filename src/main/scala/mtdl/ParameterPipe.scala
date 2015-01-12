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


  /**
   * Passes the input value from two functions:
   *   evaluate and then convert
   * @return
   */
  def execute(a : Any) : Any = {
    convert(select(a))
  }

  /**
   * An selection function that can be set from the outer side.
   *
   * This is the function that decides whether the provided argument (in case of signals)
   * or the built-in value (in case of free values) will be passed through the pipe.
   *
   * The default process is noop.
   */
  var select: (Any => Any) = (a:Any) => {a}

  /**
   * A converter function (defaulting to noop).
   * Can be set by outer world to perform custom conversions on a value
   */
  var convert: (Any => Any) = (a: Any) => {a}

  def using(converter: Any => Any) = {
    this.convert = converter
    this
  }
}

