//package za.co.xisystems.itis_rrm.data._commons.views
//
//import za.co.xisystems.itis_rrm.R
//import za.co.xisystems.itis_rrm.data._commons.ICallBack
//
//// TODO investigate the need for this abstract class?
//abstract class BaseCallBackProgressView<T> : ICallBack<T>, IProgressView {
//    constructor() {}
//    constructor(vararg messages: String?) {
//        showProgressDialog(*messages)
//    }
//
//    override fun processData(response: T?) {
//        dismissProgressDialog()
//    }
//
//    override fun processError(message: String?) {
//        if (message != null && message == "Unauthorized") {
//            toast(R.string.not_authorized)
//        } else {
//            if (message == null) toast(R.string.registration_failed_please_try_again) else toast(
//                message
//            )
//        }
//        dismissProgressDialog()
//    }
//}