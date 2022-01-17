/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfConversion
import com.mapbox.turf.TurfMeasurement
import com.xwray.groupie.viewbinding.BindableItem
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.databinding.WorkListItemBinding
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkFragmentDirections
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

open class CardItem(
    val activity: FragmentActivity?,
    val desc: String,
    val qty: String,
    val rate: String,
    val estimateId: String,
    val workViewModel: WorkViewModel,
    val jobItemEstimate: JobItemEstimateDTO,
    val job: JobDTO,
    val myLocation: LocationModel?,
) : BindableItem<WorkListItemBinding>() {
    var selectedLocationPoint: Point? = null
    var estimatePhotoStart: JobItemEstimatesPhotoDTO? = null

    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    override fun getLayout() = R.layout.work_list_item

    private fun sendJobToWork(
        workViewModel: WorkViewModel,
        estimate: JobItemEstimateDTO,
        view: View?,
        job: JobDTO
    ) {
        Coroutines.io {
            workViewModel.setWorkItemJob(job.jobId)
            workViewModel.setWorkItem(estimate.estimateId)
            withContext(Dispatchers.Main.immediate) {
                val navDirection = WorkFragmentDirections.actionNavWorkToCaptureWorkFragment(
                    jobId = job.jobId,
                    estimateId = estimate.estimateId
                )
                Navigation.findNavController(view!!).navigate(navDirection)
            }
        }
    }

    private fun alertdialog(
        activity: FragmentActivity?,
        drive: String,
        selectedLocationPoint: Point,
        view: View,
        workViewModel: WorkViewModel,
        estimate: JobItemEstimateDTO,
        job: JobDTO
    ) {
        val alert = AlertDialog.Builder(activity!!)
        alert.run {

            setTitle(R.string.not_allowed)
            // setCancelable(false)
            setIcon(R.drawable.ic_error)
            setMessage(drive)
            // Yes button
            setPositiveButton(R.string.ok) { _, _ ->

                if (this.context.isConnected) {
                    Coroutines.main {
                        workViewModel.goToWorkLocation(selectedLocationPoint)
                        workViewModel.setWorkItemJob(job.jobId)
                        workViewModel.setWorkItem(estimate.estimateId)
                        Navigation.findNavController(view).navigate(R.id.action_nav_work_to_work_location)
                    }
                } else {
                    ToastUtils().toastLong(activity, activity.getString(R.string.no_connection_detected))
                }
            }
            // Cancel Button
            setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                // Just close the dialog
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    /**
     * Perform any actions required to set up the view for display.
     *
     * @param viewBinding The ViewBinding to bind
     * @param position The adapter position
     */
    override fun bind(viewBinding: WorkListItemBinding, position: Int) {

        viewBinding.apply {

            Coroutines.main {
                estimatePhotoStart = workViewModel.getEstimateStartPhotoForId(jobItemEstimate.estimateId)
                expandableChildTextView.text = desc
                qtyTextView.text = qty
                lineAmountTextView.text = rate
                when {

                    myLocation == null -> {
                        activity!!.extensionToast(
                            title = "Missing location",
                            message = "Please turn on your location services and try again",
                            style = ToastStyle.WARNING,
                            position = ToastGravity.CENTER,
                            duration = ToastDuration.LONG
                        )
                    }
                    estimatePhotoStart == null ||
                        estimatePhotoStart?.photoLongitude == null ||
                        estimatePhotoStart?.photoLatitude == null -> {
                        activity!!.extensionToast(
                            title = "Missing photographs",
                            message = "Please download this job again. Pull down to refresh",
                            style = ToastStyle.WARNING,
                            position = ToastGravity.CENTER,
                            duration = ToastDuration.LONG
                        )
                    }

                    else -> navigateOrWork(myLocation)
                }
            }
        }
    }

    private fun WorkListItemBinding.navigateOrWork(
        myLocation: LocationModel
    ) {
        val myCurrentLocation = Point.fromLngLat(
            myLocation.longitude,
            myLocation.latitude
        )

        val destination = Point.fromLngLat(
            estimatePhotoStart?.photoLongitude!!,
            estimatePhotoStart?.photoLatitude!!
        )

        val straightDistanceBetweenDeviceAndTarget =
            String.format(
                Locale.US, "%.1f",
                TurfConversion.convertLength(
                    TurfMeasurement.distance(
                        myCurrentLocation,
                        destination, TurfConstants.UNIT_METERS
                    ),
                    TurfConstants.UNIT_METERS,
                    TurfConstants.UNIT_KILOMETERS
                )
            )

        startWorkBtn.setOnClickListener { _ ->
            if (straightDistanceBetweenDeviceAndTarget.isEmpty()) {
                ToastUtils().toastLong(activity, activity?.getString(R.string.distance_misiing))
            } else {
                val drive =
                    activity?.getString(R.string.action_not_permitted).plus(System.lineSeparator())
                        .plus("Please drive $straightDistanceBetweenDeviceAndTarget KM to the location first.")
                selectedLocationPoint = Point.fromLngLat(
                    estimatePhotoStart?.photoLongitude!!, estimatePhotoStart?.photoLatitude!!
                )

                if (straightDistanceBetweenDeviceAndTarget.toDouble() <= 1.0) {
                    sendJobToWork(workViewModel, jobItemEstimate, root, job)
                } else {
                    alertdialog(
                        activity = activity,
                        drive = drive,
                        selectedLocationPoint =
                        selectedLocationPoint!!,
                        view = root,
                        workViewModel = workViewModel,
                        estimate = jobItemEstimate,
                        job = job
                    )
                }
            }
        }
    }

    override fun initializeViewBinding(view: View): WorkListItemBinding {
        return WorkListItemBinding.bind(view)
    }
}
