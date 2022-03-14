package za.co.xisystems.itis_rrm.data.network

import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Created by Francis Mahlava on 2020/02/20.
 */
class AppExecutor {

    private val LOCK = Any()
    private var sInstance: AppExecutor? = null
    private var diskIO: Executor? = null
    private var mainThread: Executor? = null
    private var networkIO: Executor? = null

    fun AppExecutor(
        diskIO: Executor?,
        mainThread: Executor?,
        networkIO: Executor?
    ): AppExecutor? {
        this.diskIO = diskIO
        this.mainThread = mainThread
        this.networkIO = networkIO
        return sInstance
    }

    fun getInstance(): AppExecutor? {
        if (sInstance == null) {
            synchronized(
                LOCK
            ) {
                sInstance = AppExecutor(
                    Executors.newSingleThreadExecutor(),
                    Executors.newFixedThreadPool(3),
                    MainThreadExecutor()
                )
            }
        }
        return sInstance
    }

    fun diskIO(): Executor? {
        return diskIO
    }

    fun mainThread(): Executor? {
        return mainThread
    }

    fun networkIO(): Executor? {
        return networkIO
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler =
            Handler(Looper.getMainLooper())

        override fun execute(@NonNull runnable: Runnable?) {}
    }
}
