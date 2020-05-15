package za.co.xisystems.itis_rrm.custom.errors

import java.io.IOException

/**
 * Created by Francis Mahlava on 2019/10/18.
 * Extended by Shaun McDonald 2020/04/11
 */
class ApiException(message: String) : IOException(message)
class NoInternetException(message: String) : IOException(message)
class NoConnectivityException(message: String) : IOException(message)
class ServiceHostUnreachableException(message: String) : IOException(message)
class NoDataException(message: String) : Exception(message)
class AuthException(message: String) : SecurityException(message)
class NoResponseException(message: String) : Exception(message)

