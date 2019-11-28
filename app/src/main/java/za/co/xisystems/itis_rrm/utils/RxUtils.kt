package za.co.xisystems.itis_rrm.utils

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object RxUtils {
    @JvmStatic fun <T> schedule(o: Observable<T>): Observable<T> {
        return o.subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }
}
