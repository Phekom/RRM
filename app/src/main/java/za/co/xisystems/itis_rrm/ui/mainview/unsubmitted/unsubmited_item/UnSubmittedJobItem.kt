/*
 * Updated by Shaun McDonald on 2021/02/04
 * Last modified on 2021/02/04 10:39 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog.Builder
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.view.View
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration.LONG
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.databinding.UnsubmtdJobListItemBinding
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.add_items.AddItemsViewModel
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedFragment
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedFragmentDirections
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModel
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class UnSubmittedJobItem(
    private val jobDTO: JobDTO,
    private val viewModel: UnSubmittedViewModel,
    private val createModel: AddItemsViewModel,
    private val groupAdapter: GroupAdapter<GroupieViewHolder<UnsubmtdJobListItemBinding>>,
    private val fragment: UnSubmittedFragment,
//    val currentLocation: LocationModel?,
) : BindableItem<UnsubmtdJobListItemBinding>() {
    private var clickListener: ((UnSubmittedJobItem) -> Unit)? = null
    var section = ""
    var myLocation: Location? = null
    var isWithin100m : Boolean  = false
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation
    private val navigationLocationProvider = NavigationLocationProvider()
    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {
            // You're going to need this when you
            // aren't driving.
            navigationLocationProvider.changePosition(rawLocation)
//            updateCamera(rawLocation)
            myLocation = rawLocation
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )
        }
    }



    override fun bind(viewBinding: UnsubmtdJobListItemBinding, position: Int) {

        viewBinding.apply {

            iTemID.text = getItemId(position + 1).toString()
            Coroutines.main {
                val descri = viewModel.getDescForProjectId(jobDTO.projectId!!)
                unsubmittedProjectTextView.text = descri

                if (jobDTO.sectionId.isNullOrEmpty()) {
                    val projectSectionId = viewModel.getProjectSectionIdForJobId(jobDTO.jobId)
                    val prjSction = viewModel.getProjectSectionForId(projectSectionId)
                    section = prjSction.route + prjSction.section + prjSction.direction
                    unsubmittedSectionTextView.text = section
                } else {
                    val prjSction = viewModel.getProjectSectionForId(jobDTO.sectionId!!)
                    if (prjSction == null) {
                        unsubmittedSectionTextView.text = ""
                    } else {
                        section = (prjSction.route + prjSction.section + prjSction.direction) ?: ""
                        unsubmittedSectionTextView.text = section
                    }
                }
            }
            unsubmittedDescriptionTextView.text = jobDTO.descr

            deleteButton.setOnClickListener {
                buildDeleteDialog(it, position)
            }

            uploadButton.setOnClickListener {
                buildUploadDialog(it, position)
            }

            when (jobDTO.actId) {
                ActivityIdConstants.JOB_PENDING_UPLOAD -> {
                    uploadButton.visibility = View.VISIBLE
                    deleteButton.visibility = View.GONE
                }
                else -> {
                    uploadButton.visibility = View.GONE
                    deleteButton.visibility = View.VISIBLE
                }
            }

            updateItem(position)
        }

        viewBinding.root.setOnClickListener { view ->
            clickListener?.invoke(this)
            Coroutines.main {
                if (jobDTO.jobType == "Pothole"){
                    decisionAlertdialog(view, jobDTO, myLocation)
                }else{
                    sendJobToEdit(jobDTO, view)
                }

            }
        }
    }

    private fun buildUploadDialog(view: View, position: Int) {
        val itemDeleteBuilder =
            Builder(
                view.context // , android.R.style
                // .Theme_DeviceDefault_Dialog
            )
        itemDeleteBuilder.setTitle(R.string.confirm)
        itemDeleteBuilder.setIcon(R.drawable.ic_baseline_file_upload_24)
        itemDeleteBuilder.setMessage("Upload this un-submitted job?")
        // Yes button
        itemDeleteBuilder.setPositiveButton(
            R.string.yes
        ) { _, _ ->
            fragment.extensionToast(
                title = "Uploading ...",
                message = "${this.jobDTO.descr} in transit.",
                style = ToastStyle.INFO,
                position = BOTTOM,
                duration = LONG
            )
            Coroutines.main {
                try {
//                    createModel.setJobForReUpload(jobDTO.jobId)
//                    createModel.jobForReUpload.observeOnce(fragment.viewLifecycleOwner, { event ->
//                        event.getContentIfNotHandled()?.let { incompleteJob ->
//                            Coroutines.main {
//                                fragment.toggleLongRunning(true)
//                                val result = createModel.reUploadJob(incompleteJob, fragment.requireActivity())
//                                handleUploadResult(result, position, jobDTO.jobId)
//                            }
//                        }
//                    })
                } catch (t: Throwable) {
                    Timber.e("Failed to upload un-submitted job: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}")
                }
            }
        }
        // No button
        itemDeleteBuilder.setNegativeButton(
            R.string.no
        ) { dialog, _ ->
            // Do nothing but close dialog
            dialog.dismiss()
        }
        val deleteAlert = itemDeleteBuilder.create()
        deleteAlert.show()
    }

    private fun sendJobToEdit(jobData: JobDTO, view: View) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirection = UnSubmittedFragmentDirections
                    .actionNavigationUnSubmittedToNavigationAddItems(jobData.projectId!!, jobData.jobId, jobData.contractVoId)
                Navigation.findNavController(fragment.requireView()).navigate(navDirection)
            }
        }


//        Coroutines.main {
//            createModel.setJobToEdit(jobDTO.jobId)
//        }
//        val navDirection =
//            UnSubmittedFragmentDirections.actionNavUnSubmittedToAddProjectFragment(
//                jobDTO.projectId,
//                jobDTO.jobId
//            )
//
//        Navigation.findNavController(view)
//            .navigate(navDirection)
    }

    private fun handleUploadResult(result: XIResult<Boolean>, position: Int, jobId: String) {
        fragment.toggleLongRunning(false)
        when (result) {
            is XIResult.Success -> {
                fragment.extensionToast(
                    message = "Job ${jobDTO.jiNo} uploaded successfully",
                    style = ToastStyle.SUCCESS
                )
                viewModel.deleteJobFromList(jobId)
                viewModel.deleteItemList(jobId)
                groupAdapter.notifyItemRemoved(position)
                notifyChanged()
            }
            is XIResult.Error -> {
                fragment.extensionToast(
                    title = "Upload failed.",
                    message = "${result.message} - hit upload arrow to retry.",
                    style = ToastStyle.ERROR
                )
//                createModel.resetUploadState()
            }
            else -> {
                Timber.e("$result")
            }
        }
    }

    override fun getLayout() = R.layout.unsubmtd_job_list_item

    private fun updateItem(position: Int) {
        // Not interested in this
        groupAdapter.notifyItemChanged(position)
    }

    private fun buildDeleteDialog(view: View, position: Int) {
        val itemDeleteBuilder =
            Builder(
                view.context // , android.R.style
                // .Theme_DeviceDefault_Dialog
            )
        itemDeleteBuilder.setTitle(R.string.confirm)
        itemDeleteBuilder.setIcon(R.drawable.ic_warning)
        itemDeleteBuilder.setMessage("Delete this unsubmitted job?")
        // Yes button
        itemDeleteBuilder.setPositiveButton(
            R.string.yes
        ) { _, _ ->
            fragment.extensionToast(
                title = "Deleting ...",
                message = "${this.jobDTO.descr} removed.",
                style = ToastStyle.DELETE,
                position = BOTTOM,
                duration = LONG
            )
            Coroutines.main {
                try {
                    viewModel.deleteJobFromList(jobDTO.jobId)
                    viewModel.deleteItemList(jobDTO.jobId)
                    groupAdapter.notifyItemRemoved(position)
                    notifyChanged()
                } catch (t: Throwable) {
                    Timber.e("Failed to delete unsubmitted job: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}")
                }
            }
        }
        // No button
        itemDeleteBuilder.setNegativeButton(
            R.string.no
        ) { dialog, _ ->
            // Do nothing but close dialog
            dialog.dismiss()
        }
        val deleteAlert = itemDeleteBuilder.create()
        deleteAlert.show()
    }

    override fun initializeViewBinding(view: View): UnsubmtdJobListItemBinding {
        return UnsubmtdJobListItemBinding.bind(view)
    }

    private fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    private fun decisionAlertdialog(view: View, jobDTO: JobDTO, myLocation: Location?) {
        val textEntryView = fragment.layoutInflater.inflate(R.layout.unsubmitted_alert_dialog, null)
        val jiNo = textEntryView.findViewById<View>(R.id.ji_numb) as TextView
        val section = textEntryView.findViewById<View>(R.id.section_numb) as TextView
        val decline = textEntryView.findViewById<View>(R.id.decline_job_button) as Button
        val create = textEntryView.findViewById<View>(R.id.create_job_button) as Button
        val latitude = textEntryView.findViewById<View>(R.id.latitudeText) as TextView
        val longitude = textEntryView.findViewById<View>(R.id.longitudeText) as TextView
        val navigate = textEntryView.findViewById<View>(R.id.navigate_to) as Button
        val mapV = textEntryView.findViewById<View>(R.id.estimatemapview) as MapView
        val buttons_lin1 = textEntryView.findViewById<View>(R.id.buttons_lin) as LinearLayout
        val buttons_lin2 = textEntryView.findViewById<View>(R.id.buttons_lin1) as LinearLayout

        val alert: androidx.appcompat.app.AlertDialog = androidx.appcompat.app.AlertDialog.Builder(fragment.requireContext())
            .setView(textEntryView)
            .setCancelable(true)
            .setIcon(R.drawable.ic_ji_direction)
            .setTitle(R.string.ji_direction_choice)
            .setMessage("Please Note if you decline a JI you will be required to give a valid reason")
//            .setNegativeButton("decline", null)
//            .setPositiveButton("create", null)
            .create()

        alert.setOnShowListener { dialog ->
           Coroutines.ui{
               buttons_lin1.visibility = View.GONE
               buttons_lin2.visibility = View.GONE

               mapboxMap = mapV.getMapboxMap()

               mapV.location.apply {
                   setLocationProvider(navigationLocationProvider)
                   enabled = true
               }
               mapboxMap.removeOnMapClickListener( OnMapClickListener {
                   false
               })
               initMap(fragment, jobDTO, mapV, buttons_lin1, buttons_lin2)

              // val photoData = viewModel.getPotholePhoto(jobDTO.jobId)
               jiNo.text = "JI: ${jobDTO.jiNo}"
               section.text = jobDTO.pHRoute ?: ""
               latitude.text = jobDTO.pHLatitude.toString()?:""
               longitude.text = jobDTO.pHLongitude.toString()?:""

               val  selectedLocationPoint = Point.fromLngLat(
                   jobDTO.pHLongitude,jobDTO.pHLatitude
               )


               decline.setOnClickListener {
                   sendJobToDecline(jobDTO, view)
                   // make sure to stop the trip session. In this case it is being called inside `onDestroy`.
                   mapboxNavigation.stopTripSession()
                   // make sure to unregister the observer you have registered.
                   mapboxNavigation.unregisterLocationObserver(locationObserver)
                   mapboxNavigation.onDestroy()
                   dialog.dismiss()
               }

               create.setOnClickListener {
                   sendJobToEdit(jobDTO, view)
                   // make sure to stop the trip session. In this case it is being called inside `onDestroy`.
                   mapboxNavigation.stopTripSession()
                   // make sure to unregister the observer you have registered.
                   mapboxNavigation.unregisterLocationObserver(locationObserver)
                   mapboxNavigation.onDestroy()
                   dialog.dismiss()

               }

               navigate.setOnClickListener {

                   val  myLocationPoint = Point.fromLngLat(
                       myLocation?.longitude!!,myLocation?.latitude!!
                   )
                   navigateTo(selectedLocationPoint, view, myLocationPoint, jobDTO)
                   // make sure to stop the trip session. In this case it is being called inside `onDestroy`.
                   mapboxNavigation.stopTripSession()
                   // make sure to unregister the observer you have registered.
                   mapboxNavigation.unregisterLocationObserver(locationObserver)
                   mapboxNavigation.onDestroy()
                   dialog.dismiss()
               }

           }
        }
        alert.setOnCancelListener {
            // make sure to stop the trip session. In this case it is being called inside `onDestroy`.
            mapboxNavigation.stopTripSession()
            // make sure to unregister the observer you have registered.
            mapboxNavigation.unregisterLocationObserver(locationObserver)
            mapboxNavigation.onDestroy()
        }


        alert.show()
    }

    private fun checkDistance(potholJob: JobDTO, buttons_lin1: LinearLayout, buttons_lin2: LinearLayout) {
        val results = FloatArray(1)
        myLocation?.longitude?.let {
            myLocation?.latitude?.let { it1 ->
                Location.distanceBetween(
                    potholJob.pHLatitude,
                    potholJob.pHLongitude,
                    it1,
                    it,
                    results
                )
            }
        }
        val distanceInMeters = results[0]
        isWithin100m = distanceInMeters <= 150
        if (isWithin100m){
            buttons_lin1.visibility = View.VISIBLE
            buttons_lin2.visibility = View.GONE
        }else{
            buttons_lin1.visibility = View.GONE
            buttons_lin2.visibility = View.VISIBLE

        }
    }

    private fun initMap(fragment: UnSubmittedFragment, jobDTO: JobDTO, mapV: MapView, buttons_lin1: LinearLayout, buttons_lin2: LinearLayout) {
        initStyle(jobDTO, mapV)
        initNavigation(fragment, jobDTO, mapV, buttons_lin1,buttons_lin2)
    }

    private fun initStyle(jobDTO: JobDTO, mapV: MapView) {
        mapboxMap.loadStyleUri(
            Style.MAPBOX_STREETS, {
                addAnnotationToMap(it, jobDTO,mapV)
            },
            object : OnMapLoadErrorListener {
                override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
                    Timber.e("${eventData.type.name} - ${eventData.message}")
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun initNavigation(fragment: UnSubmittedFragment, jobDTO: JobDTO, mapV: MapView, buttons_lin1: LinearLayout, buttons_lin2: LinearLayout) {
        mapboxNavigation = MapboxNavigation(
            NavigationOptions.Builder(fragment.requireContext())
                .accessToken(fragment.getString(R.string.mapbox_access_token))
                .build()
        ).apply {
            if (ActivityCompat.checkSelfPermission(
                    fragment.requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    fragment.requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                fragment.extensionToast(
                    message = "Please enable location services in order to proceed",
                    style = ToastStyle.WARNING,
                    position = ToastGravity.CENTER,
                    duration = ToastDuration.LONG
                )
            }
            startTripSession()
            registerLocationObserver(locationObserver)
            updateCamera(jobDTO, mapV)
            checkDistance(jobDTO, buttons_lin1, buttons_lin2)
        }
    }

    @Suppress("MagicNumber")
    private fun updateCamera(location: JobDTO, mapV: MapView, ) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        mapV.camera?.easeTo(
            CameraOptions.Builder()
                // Centers the camera to the lng/lat specified.
                .center(Point.fromLngLat(location.pHLongitude, location.pHLatitude))
                // specifies the zoom value. Increase or decrease to zoom in or zoom out
                .zoom(12.0)
                // specify frame of reference from the center.
                .padding(EdgeInsets(100.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptions
        )
    }

    private fun addAnnotationToMap(style: Style, jobDTO: JobDTO, mapV: MapView) {
        bitmapFromDrawableRes(
            fragment.requireContext(),
            R.drawable.red_marker
        )?.let {
            val annotationApi = mapV.annotations
            val pointAnnotationManager =
                annotationApi?.createPointAnnotationManager(mapV)
            pointAnnotationManager?.addClickListener(OnPointAnnotationClickListener {
                Toast.makeText(fragment.requireContext(), "{screenPoint.coordinates()}  Marker clicked", Toast.LENGTH_SHORT).show()
                true
            })
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(
                    Point.fromLngLat(
                        jobDTO.pHLongitude,
                        jobDTO.pHLatitude
                    )
                )
                .withIconImage(it)
            pointAnnotationManager.create(pointAnnotationOptions)
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }


    private fun sendJobToDecline(jobDTO: JobDTO, view: View) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirection = UnSubmittedFragmentDirections
                    .actionNavigationUnSubmittedToDeclineJobFragment(jobDTO.jobId)
                Navigation.findNavController(view).navigate(navDirection)
            }
        }
    }

    private fun navigateTo(
        selectedLocationPoint: Point,
        view: View,
        myLocationPoint: Point,
        jobDTO: JobDTO
    ) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirection = UnSubmittedFragmentDirections
                    .actionNavigationUnSubmittedToWorkLocation(selectedLocationPoint, myLocationPoint, jobDTO, null)
                Navigation.findNavController(view).navigate(navDirection)
            }
        }
    }


}
