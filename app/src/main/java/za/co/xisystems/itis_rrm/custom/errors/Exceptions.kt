package za.co.xisystems.itis_rrm.custom.errors

import java.io.IOException

/**
 * Created by Francis Mahlava on 2019/10/18.
 */
class ApiException(message: String) : IOException(message)

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
 *
 * @constructor
 */
class NoDataException(message: String) : NullPointerException(message)
class AuthException(message: String) : SecurityException(message)
class NoResponseException(message: String) : Exception(message)
class DataException(message: String) : Exception(message)
class ServiceHostUnreachableException(message: String) : IOException(message)
