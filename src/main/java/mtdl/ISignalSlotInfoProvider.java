package mtdl;

/**
 *
 * A class implementing this interface has to be providerd to the
 * Signleton SiganalSlotInfoProvider in order to
 * provide the information about the current signals and slots
 * that are refered to from tdl.
 *
 * minder will provide this information from the Database
 *
 * Created by yerlibilgin on 09/12/14.
 */
public interface ISignalSlotInfoProvider {
  public SignalSlot getSignalSlot(String wrapperId, String signature);
}
