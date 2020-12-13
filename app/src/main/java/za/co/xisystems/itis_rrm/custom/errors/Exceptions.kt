package za.co.xisystems.itis_rrm.custom.errors

import okio.IOException

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

/**
 * An errorMessage from the mobile services fires this exception
 */
class ServiceException(message: String) : Exception(message)

/**
 * No Internet connection fires this exception.
 * @constructor
 */
class NoInternetException(message: String) : IOException(message)

/**
 * Being unable to connect to key servers fires this exception.
 * @constructor
 */
class NoConnectivityException(message: String) : IOException(message)

/**
 * Returning an empty result set fires this error
 * @constructor
 */
class NoDataException(message: String) : NullPointerException(message)

/**
 * Incorrect credentials / pin entries fire this exception
 * @constructor
 */
class AuthException(message: String) : SecurityException(message)

/**
 * Returning an empty response fires this error
 * @constructor
 */
class NoResponseException(message: String) : Exception(message)

/**
 * Being unable to contact the mobile services platform fires this error
 * @constructor
 */
class ServiceHostUnreachableException(message: String) : IOException(message)

/**
 * Being unable to read or write to the local datastore fires this exception
 * @constructor
 */
class LocalDataException(message: String) : Exception(message)

/**
 * Mostly harmless, but still annoying
 * @constructor
 */
class RecoverableException(message: String) : Throwable(message)

class RestException(message: String, override val cause: Throwable?) : Throwable(message, cause)

class ConnectException(message: String) : IOException(message)
