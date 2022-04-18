/*
 * Updated by Shaun McDonald on 2021/02/04
 * Last modified on 2021/02/04 12:13 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.add_items

import android.annotation.SuppressLint
import android.app.AlertDialog.Builder
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.stfalcon.imageviewer.StfalconImageViewer
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration.LONG
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.NewJobItemBinding
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.JobUtils
import za.co.xisystems.itis_rrm.utils.image_capture.zooming.Poster
import za.co.xisystems.itis_rrm.utils.image_capture.zooming.PosterOverlayView
import za.co.xisystems.itis_rrm.utils.image_capture.zooming.StylingOptions
import java.io.File

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

open class ProjectItem(
    private val fragment: AddProjectItemsFragment,
    private val tempItem : ItemDTOTemp,
    private val addViewModel: AddItemsViewModel,
    private val contractID: String?,
    private var job: JobDTO?
) : BindableItem<NewJobItemBinding>() {

    init {
        extras[INSET_TYPE_KEY] = INSET
    }
    private var options = StylingOptions()
    private var overlayView: PosterOverlayView? = null
    private var viewer: StfalconImageViewer<Poster>? = null
    private var clickListener: ((ProjectItem) -> Unit)? = null
    override fun getLayout() = R.layout.new_job_item
    private var theJobItemEstimate: JobItemEstimateDTO? = null
    @SuppressLint("SetTextI18n")
    override fun bind(viewBinding: NewJobItemBinding, position: Int) {
        viewBinding.apply {
            textViewItem.text = "${tempItem.itemCode}  ${tempItem.descr}"
            selectedJobType.visibility = View.GONE
            jbtypLabel.visibility = View.GONE
            detailsImageView.visibility = View.GONE
            Coroutines.main {
                getJobItemEstimate(tempItem.itemId, job!!.jobId).also { jobItemEstimate ->
                    theJobItemEstimate = jobItemEstimate
                     processEstimate(jobItemEstimate, viewBinding)
                }
                if (theJobItemEstimate?.jobItemEstimateSize.isNullOrEmpty()){
                    selectedJobType.visibility = View.GONE
                    jbtypLabel.visibility = View.GONE
                    detailsImageView.visibility = View.GONE
                } else  {
                    selectedJobType.visibility = View.VISIBLE
                    jbtypLabel.visibility = View.VISIBLE
                    detailsImageView.visibility = View.VISIBLE
                    selectedJobType.text = theJobItemEstimate!!.jobItemEstimateSize
                }

                subTextView.setOnClickListener {
                    Coroutines.main {
                    addViewModel.backupJob(job!!)
                    addViewModel.backupProjectItem(tempItem)
                    addViewModel.tempProjectItem.value = XIEvent(tempItem)

                    sendSelectedItem(tempItem, contractID, job, theJobItemEstimate, it)
                    clickListener?.invoke(this@ProjectItem)
                    }
                }

                textViewItem.setOnClickListener {
                    Coroutines.main {
                        addViewModel.backupJob(job!!)
                        addViewModel.backupProjectItem(tempItem)
                        addViewModel.tempProjectItem.value = XIEvent(tempItem)

                        sendSelectedItem(tempItem, contractID, job, theJobItemEstimate, it)
                        clickListener?.invoke(this@ProjectItem)
                    }
                }

                detailsImageView.setOnClickListener {
                    detailsAlertdialog(fragment, tempItem, theJobItemEstimate, addViewModel)
                }

                root.setOnLongClickListener {
                    Coroutines.main {
                        buildDeleteDialog(it)
                    }
                    it.isLongClickable
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun NewJobItemBinding.processEstimate(
        jobItemEstimate: JobItemEstimateDTO?,
        viewBinding: NewJobItemBinding
    ) {
        if (jobItemEstimate != null) {
            addViewModel.estimateComplete(jobItemEstimate).also { complete ->
                if (complete) {
                    val lineRate: Double = jobItemEstimate.lineRate
                    val tenderRate: Double = tempItem.tenderRate
                    val qty: Double = jobItemEstimate.qty
                    costTextView.text =
                        "$qty  *   R $lineRate = ${JobUtils.formatCost(qty * tenderRate)}"
                    subTextView.visibility = View.GONE
                } else {
                    costTextView.text = viewBinding.root.context.getString(R.string.incomplete_estimate)
                    subTextView.visibility = View.VISIBLE
                }

                val notLocated = ContextCompat.getDrawable(
                    root.context,
                    R.drawable.ic_baseline_wrong_location_24
                )
                if (!jobItemEstimate.geoCoded && jobItemEstimate.size() == 2) {
                    textViewItem.setCompoundDrawablesWithIntrinsicBounds(
                        notLocated, null, null, null
                    )
                } else {
                    textViewItem.setCompoundDrawablesWithIntrinsicBounds(
                        null, null, null, null
                    )
                }
            }
            theJobItemEstimate = jobItemEstimate
//            bindItem(viewBinding)
        }
    }

    private fun sendSelectedItem(
        item: ItemDTOTemp,
        contractID: String?,
        newJob: JobDTO?,
        jobItemEstimate: JobItemEstimateDTO?,
        view: View
    ) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirections = AddProjectItemsFragmentDirections
                        .actionNavigationAddItemsToEstimatePhotoFragment(newJob?.jobId, item.itemId, jobItemEstimate?.estimateId,newJob?.contractVoId,null)
                Navigation.findNavController(view)
                    .navigate(navDirections)
            }
        }


    }

    private suspend fun getJobItemEstimate(itemId: String, jobId: String):
        JobItemEstimateDTO? = withContext(Dispatchers.Main) {
        val updatedEstimate = addViewModel.getJobEstimateIndexByItemAndJobId(itemId, jobId)
        return@withContext if (updatedEstimate != null) {
            job?.insertOrUpdateJobItemEstimate(updatedEstimate)
            job?.getJobEstimateByItemId(itemId)
        } else {
            null
        }
    }

    private fun buildDeleteDialog(view: View) {
        val itemDeleteBuilder =
            Builder(
                view.context // , android.R.style
                // .Theme_DeviceDefault_Dialog
            )
        itemDeleteBuilder.setTitle(R.string.confirm)
        itemDeleteBuilder.setIcon(R.drawable.ic_warning)
        itemDeleteBuilder.setMessage("Delete this project item?")
        // Yes button
        itemDeleteBuilder.setPositiveButton(
            R.string.yes
        ) { _, _ ->
            fragment.extensionToast(
                title = "Deleting ...",
                message = "${this.tempItem.descr} removed.",
                style = ToastStyle.DELETE,
                position = BOTTOM,
                duration = LONG
            )

            Coroutines.main {
                // Get the JobEstimate
                val jobItemEstimate = getJobItemEstimate(tempItem.itemId, job!!.jobId)

                // Delete the project item.
                addViewModel.deleteItemFromList(tempItem.itemId, estimateId = jobItemEstimate?.estimateId)
//                addViewModel.deleteItemTempFromList()
                // Set updated job and recalculate costs if applicable
                job?.let {
                    it.removeJobEstimateByItemId(tempItem.itemId)
                    addViewModel.backupJob(it)
                    addViewModel.setJobToEdit(tempItem.jobId)
                    fragment.uiUpdate()
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


    @SuppressLint("SetTextI18n")
    private fun detailsAlertdialog(fragment: AddProjectItemsFragment, tempItem: ItemDTOTemp,
                                   theJobItemEstimate: JobItemEstimateDTO?, addViewModel: AddItemsViewModel) {
        val textEntryView = fragment.layoutInflater.inflate(R.layout.details_alert_dialog, null)
        val end = textEntryView.findViewById<View>(R.id.end) as LinearLayout
        val itemName = textEntryView.findViewById<View>(R.id.itemName) as TextView
        val selectedJobType = textEntryView.findViewById<View>(R.id.selectedJobType) as TextView
        val selectedRate = textEntryView.findViewById<View>(R.id.selectedRate) as TextView
        val selectedQty = textEntryView.findViewById<View>(R.id.selectedQty) as TextView
        val photoPreviewStart = textEntryView.findViewById<View>(R.id.photoPreviewStart) as ImageView
        val photoPreviewEnd = textEntryView.findViewById<View>(R.id.photoPreviewEnd) as ImageView
        val startlat = textEntryView.findViewById<View>(R.id.startlat) as TextView
        val startlong = textEntryView.findViewById<View>(R.id.startlong) as TextView
        val sectionSt = textEntryView.findViewById<View>(R.id.sectionSt) as TextView

        val endlat = textEntryView.findViewById<View>(R.id.endlat) as TextView
        val endlong = textEntryView.findViewById<View>(R.id.endlong) as TextView
        val sectionEnd = textEntryView.findViewById<View>(R.id.sectionEnd) as TextView
        var clickListener = ::onImageClickAction

        val uriString = ArrayList<Poster>()



        val alert: AlertDialog = AlertDialog.Builder(fragment.requireContext())
            .setView(textEntryView)
            .setIcon(R.drawable.ic_measure)
            .setTitle(R.string.estimate_item_details)
            .setMessage("This is what you've captured for ${tempItem.itemCode}  ${tempItem.descr}")
            .setPositiveButton("OK", null).create()

        alert.setOnShowListener { dialog ->
            Coroutines.main {
                end.visibility = View.GONE
                if(theJobItemEstimate?.jobItemEstimateSize.isNullOrEmpty() || theJobItemEstimate?.jobItemEstimateSize.equals("Point")){
                    end.visibility = View.GONE
                }else{
                    end.visibility = View.VISIBLE
                }
               val estimatePhotosList = addViewModel.getJobItemEstimatePhotosForEstimateId(theJobItemEstimate?.estimateId!!)

                estimatePhotosList.forEach { photo ->
                    uriString.add(Poster(photo.photoPath, "${photo.photoLatitude}", "${photo.photoLongitude}", "${photo.sectionMarker}"))
                }

                val enter = alert.getButton(AlertDialog.BUTTON_POSITIVE)
                itemName.text = "${tempItem.itemCode}  ${tempItem.descr}"
                selectedJobType.text = theJobItemEstimate.jobItemEstimateSize
                selectedRate.text = theJobItemEstimate.lineRate.toString()
                selectedQty.text = theJobItemEstimate.qty.toString()
                val startPhotoDetails =
                    addViewModel.getJobEstimationItemsPhotoStart(theJobItemEstimate?.estimateId!!)
                startlat.text = startPhotoDetails.photoLatitude.toString()
                startlong.text = startPhotoDetails.photoLongitude.toString()
                sectionSt.text = startPhotoDetails.sectionMarker
                val startPhoto =
                    addViewModel.getJobEstimationItemsPhotoStartPath(theJobItemEstimate?.estimateId!!)
                GlideApp.with(fragment.requireActivity())
                    .load(Uri.fromFile(File(startPhoto)))
                    .centerCrop()
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.logo_new_medium)
                    .into(photoPreviewStart)

                val endPhoto =
                    addViewModel.getJobEstimationItemsPhotoEndPath(theJobItemEstimate.estimateId)?:""
                val endPhotoDetails =
                    addViewModel.getJobEstimationItemsPhotoEnd(theJobItemEstimate.estimateId)?:null
                endlat.text = endPhotoDetails?.photoLatitude.toString()?:"No Data"
                endlong.text = endPhotoDetails?.photoLongitude.toString()?:"No Data"
                sectionEnd.text = endPhotoDetails?.sectionMarker
                GlideApp.with(fragment.requireActivity())
                    .load(Uri.fromFile(File(endPhoto)))
                    .centerCrop()
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.logo_new_medium)
                    .into(photoPreviewEnd)

                photoPreviewStart.setOnClickListener {
                    clickListener.invoke(photoPreviewEnd, uriString, 0)
                }
                photoPreviewEnd.setOnClickListener {
                    clickListener.invoke(photoPreviewEnd, uriString, 0)
                }
                    enter.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }

        alert.show()
    }



    private fun onImageClickAction(
        imageView: ImageView,
        uriString: ArrayList<Poster>,
        startPosition: Int,
    ) {
        val posters = uriString.toMutableList()
        val builder = StfalconImageViewer.Builder<Poster>(fragment.requireContext(), posters, ::loadPosterImage)
            .withStartPosition(startPosition)
            .withImageChangeListener { position ->
                if (options.isPropertyEnabled(StylingOptions.Property.SHOW_TRANSITION)) {
                    // updateTransitionImage(imageView[position])
                }
                overlayView?.update(posters[position], fragment.requireContext())
            }
        // .withDismissListener { showShortToast(R.string.message_on_dismiss) }

        builder.withHiddenStatusBar(options.isPropertyEnabled(StylingOptions.Property.HIDE_STATUS_BAR))

        if (options.isPropertyEnabled(StylingOptions.Property.IMAGES_MARGIN)) {
            builder.withImagesMargin(R.dimen.image_margin)
        }

        if (options.isPropertyEnabled(StylingOptions.Property.CONTAINER_PADDING)) {
            builder.withContainerPadding(R.dimen.image_margin)
        }

        if (options.isPropertyEnabled(StylingOptions.Property.SHOW_TRANSITION)) {
            builder.withTransitionFrom(imageView)
        }

        builder.allowSwipeToDismiss(options.isPropertyEnabled(StylingOptions.Property.SWIPE_TO_DISMISS))
        builder.allowZooming(options.isPropertyEnabled(StylingOptions.Property.ZOOMING))

        if (options.isPropertyEnabled(StylingOptions.Property.SHOW_OVERLAY)) {
            setupOverlayView(posters, startPosition)
            builder.withOverlayView(overlayView!!)
        }

        viewer = builder.show()
    }

    private fun setupOverlayView(posters: MutableList<Poster>, startPosition: Int) {
        overlayView = PosterOverlayView(fragment.requireContext()).apply {
            update(posters[startPosition], fragment.requireContext())

            onDeleteClick = {
                val currentPosition = viewer?.currentPosition() ?: 0
                //deletePhoto(inspection.photoId)
                posters.removeAt(currentPosition)
                viewer?.updateImages(posters)

                posters.getOrNull(currentPosition)
                    ?.let { poster -> update(poster, fragment.requireContext()) }
            }
        }
    }

    protected fun loadPosterImage(imageView: ImageView, poster: Poster?) {
        loadImage(imageView, poster?.url)
    }

    protected fun loadImage(imageView: ImageView, url: String?) {
        GlideApp.with(fragment.requireContext().applicationContext)
            .load(url)
            .into(imageView).clearOnDetach()
    }


    override fun initializeViewBinding(view: View): NewJobItemBinding {
        return NewJobItemBinding.bind(view)
    }


}
