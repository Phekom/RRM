//package za.co.xisystems.itis_rrm.data._commons.views
//
//import za.co.xisystems.itis_rrm.R
//import za.co.xisystems.itis_rrm.data._commons.ICallBack
//
//abstract class BaseCallBackView<T> : ICallBack<T> {
//    var view: IProgressView?
//
//    constructor(progressView: IProgressView?) {
//        view = progressView
//    }
//
//
//
//    override fun processError(message: String?) { // TODO this need work
//        if (view != null) {
//            if (message != null && message == "Unauthorized") {
//                view!!.toast(R.string.not_authorized)
//            } else {
//                if (message == null) view!!.toast(R.string.registration_failed_please_try_again) else view!!.toast(
//                    message
//                )
//            }
//            view!!.dismissProgressDialog()
//        }
//    }
//
//}