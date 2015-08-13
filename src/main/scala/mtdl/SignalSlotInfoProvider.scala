package mtdl

/**
 *
 * A singleton object that provides signal-slot information using the underlying provider
 * that is registered by the referring client (minder-server will provide
 * a db implementation)
 *
 *
 * In order to resolve a signal or a slot from a wrapper, the wrapper must first have
 * registered its services via the minder-client - minder-server xoola integration
 *
 * Created by yerlibilgin on 05/12/14.
 */
object SignalSlotInfoProvider {
  var provider: ISignalSlotInfoProvider = null

  /**
   * Delegate the signal-slot search to the actual underlying signal-slot info provider implementation
   * @param wrapperId
   *   the wrapper id (either WrapperName|Version or WrapperName)
   * @param signature
   * @return
   */
  def getSignalSlot(wrapperId: String, signature: String): SignalSlot = {
    if (provider == null)
      throw new IllegalStateException("No signal-slot info provider registered")


    provider.getSignalSlot(wrapperId, signature)
  }

  def setSignalSlotInfoProvider(provider: ISignalSlotInfoProvider): Unit = {
    this.provider = provider
  }


}
