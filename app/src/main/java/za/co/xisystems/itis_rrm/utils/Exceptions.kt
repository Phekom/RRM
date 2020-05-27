package za.co.xisystems.itis_rrm.utils

import java.io.IOException
/**
 * Created by Francis Mahlava on 2019/10/18.
 */
class ApiException(message: String) : IOException(message)
class NoInternetException(message: String) : IOException(message)
class NoConnectivityException(message: String) : IOException(message)
class NoDataException(message: String) : NullPointerException(message)
class AuthException(message: String) : SecurityException(message)
class NoResponseException(message: String) : Exception(message)
