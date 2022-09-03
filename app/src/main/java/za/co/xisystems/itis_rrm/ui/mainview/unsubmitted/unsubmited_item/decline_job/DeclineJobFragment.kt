package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item.decline_job

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.github.ajalt.timberkt.Timber
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDeclineDTO
import za.co.xisystems.itis_rrm.databinding.FragmentDeclineJobBinding
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.add_items.AddItemsViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.add_items.AddItemsViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.GlideApp
import java.io.File
import java.io.IOException
import java.util.*

class DeclineJobFragment : LocationFragment() {

    private lateinit var addViewModel: AddItemsViewModel
    private val createFactory: AddItemsViewModelFactory by instance()
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder<FragmentDeclineJobBinding>>
    private lateinit var viewModel : DeclineJobViewModel
    private val factory: DeclineJobViewModelFactory by instance()
    private var _ui: FragmentDeclineJobBinding? = null
    private val ui get() = _ui!!
    private val data: DeclineJobFragmentArgs by navArgs()
    companion object {
        val TAG: String = DeclineJobFragment::class.java.canonicalName!!
        private const val CAMERA_INTENT_REQ_CODE = 4281
    }
    private var canMove: Boolean = false
    private var cameraImageUri: Uri? = null
    private var itemIdPhotoType = HashMap<String, String>()
    private var filenamePath = HashMap<String, String>()
    var photoType: PhotoType = PhotoType.START
    private var jobToDecline : JobDTO? = null
    private var declinedata : JobDeclineDTO? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Coroutines.io {
                    withContext(Dispatchers.Main.immediate) {
                        val navDirection = DeclineJobFragmentDirections
                            .actionDeclineJobFragmentToNavigationUnSubmitted()
                        Navigation.findNavController(requireView()).navigate(navDirection)
                    }
                }
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }

    private val backClickListener = View.OnClickListener {
        setBackPressed()
    }

    private fun setBackPressed() {
        Intent(requireContext(), MainActivity::class.java).also { home ->
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentDeclineJobBinding.inflate(inflater, container, false)
        _ui?.toolbar?.apply {
            setTitle(getString(R.string.decline))
            setOnBackClickListener(backClickListener)
        }
        return ui.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      Coroutines.main {
          viewModel =
              ViewModelProvider(this.requireActivity(), factory).get(DeclineJobViewModel::class.java)
          ui.saveButton.isClickable = false
          addViewModel =
              ViewModelProvider(this.requireActivity(), createFactory).get(AddItemsViewModel::class.java)
          var section = ""
          jobToDecline = viewModel.getJobforId(data.jobId)
          _ui?.jiNumb?.text = jobToDecline?.jiNo
          if (jobToDecline?.sectionId.isNullOrEmpty()){
              val projectSectionId = viewModel.getProjectSectionIdForJobId(jobToDecline?.jobId!!)
              val prjSction = viewModel.getProjectSectionForId(projectSectionId)
              section = prjSction.route+prjSction.section+prjSction.direction
              _ui?.sectionNumb?.text  = section
          }else{
              val prjSction = viewModel.getProjectSectionForId(jobToDecline?.sectionId!!)
              if (prjSction == null){
                  _ui?.sectionNumb?.text = ""
              }else{
                  section = (prjSction.route + prjSction.section + prjSction.direction) ?: ""
                  _ui?.sectionNumb?.text = section
              }
          }
          _ui?.latitudeText?.text = currentLocation?.latitude.toString()
          _ui?.longitudeText?.text = currentLocation?.longitude.toString()

          _ui?.addPhotoBtn?.setOnClickListener {
              photoType = PhotoType.START
              launchCamera(CAMERA_INTENT_REQ_CODE, photoType)
          }

          _ui?.backtolist?.setOnClickListener {
              Coroutines.io {
                  withContext(Dispatchers.Main.immediate) {
                      val navDirection = DeclineJobFragmentDirections
                          .actionDeclineJobFragmentToNavigationUnSubmitted()
                      Navigation.findNavController(requireView()).navigate(navDirection)
                  }
              }
          }

          ui.saveButton.setOnClickListener {
              Coroutines.ui {
                  validateScreenInputData(declinedata, this@DeclineJobFragment.requireView())

              }

          }

      }

    }

    private fun validateScreenInputData(declinedata: JobDeclineDTO?, requireView: View) {
        Coroutines.main {
            declinedata?.cancelComments =  ui.newComment.text.toString()

            if (declinedata?.fileName.isNullOrEmpty() || declinedata?.photoPath.isNullOrEmpty() ) {
                extensionToast(
                    message = getString(R.string.photo_missing),
                    style = ToastStyle.WARNING,
                    position = ToastGravity.BOTTOM,
                    duration = ToastDuration.LONG
                )
            } else if (declinedata?.cancelComments.isNullOrEmpty()) {
                    extensionToast(
                        message = getString(R.string.comment_required),
                        style = ToastStyle.WARNING,
                        position = ToastGravity.BOTTOM,
                        duration = ToastDuration.LONG
                    )
            } else canMove = true


            if (canMove) {
                if (this@DeclineJobFragment.requireContext().isConnected) {
                    val isSuccess  = viewModel.submitForDecline(declinedata , requireActivity())
                    if (isSuccess == "Successful") {
                        ToastUtils().toastShort(requireContext(), "Decline $isSuccess")
                        Coroutines.io {
                            withContext(Dispatchers.Main.immediate) {
                                val navDirection = DeclineJobFragmentDirections.actionDeclineJobFragmentToNavigationUnSubmitted()
                                Navigation.findNavController(requireView).navigate(navDirection)
                            }
                        }
                    }else{
                        ToastUtils().toastShort(requireContext(), isSuccess)
                    }

                } else {
                    displayConnectionWarning()
                }
            }
        }
    }


    private fun displayConnectionWarning() {
        this@DeclineJobFragment.extensionToast(
            title = "No Connectivity",
            message = "Job Decline requires an active internet connection.",
            style = ToastStyle.NO_INTERNET,
            duration = ToastDuration.LONG,
            position = ToastGravity.BOTTOM
        )
    }

    private fun launchCamera(reqCode: Int, photoType: PhotoType) {
        uiScope.launch(uiScope.coroutineContext) {

            toggleLongRunning(true)
            itemIdPhotoType["itemId"] = ""
            itemIdPhotoType["type"] = photoType.name

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                sharedViewModel.toggleLongRunning(true)
                val photoFile: File? = try {
                    viewModel.photoUtil.createImageFile()
                } catch (ex: IOException) {
                    ToastUtils().toastShort(requireContext(), ex.message)
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    cameraImageUri = viewModel.photoUtil.getUriFromPath2(
                        requireActivity() ,it.path
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                    takePictureIntent.putExtra(
                        MediaStore.EXTRA_SCREEN_ORIENTATION,
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                    takePictureIntent.putExtra("photoType", itemIdPhotoType["type"])
                    takePictureIntent.putExtra("itemId", itemIdPhotoType["itemId"])
                    startActivityForResult(
                        takePictureIntent, reqCode
                    )
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        ) // If the image capture activity was called and was successful
        if (requestCode == CAMERA_INTENT_REQ_CODE && resultCode == Activity.RESULT_OK) {
            // Process the image and set it to the TextView
            if (cameraImageUri != null) {
                uiScope.launch(uiScope.coroutineContext) {
                    processAndSetImage(
                        cameraImageUri!!, jobToDecline
                    )
                }
            } else {
                Snackbar.make(
                    _ui?.root?.rootView!!,
                    "Image Saving Error Please Re-Capture",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(getString(R.string.retry)) {
                        launchCamera(requestCode, photoType)
                    }.show()
            }
        }else { // Otherwise, delete the temporary image file
            uiScope.launch(uiScope.coroutineContext) {
                viewModel.photoUtil.deleteImageFile(filenamePath.toString())
            }

        }
        toggleLongRunning(false)
    }

    private suspend fun processAndSetImage(cameraImageUri: Uri, jobToDecline: JobDTO?) {
        try { //  Location of picture
            filenamePath = viewModel.photoUtil.saveImageToInternalStorage(
                cameraImageUri
            ) as HashMap<String, String>
            if (currentLocation == null) {
                ToastUtils().toastLong(
                    requireContext(),
                    "Location is ether Turned Off or Not Ready please check connection and try again"
                )
            } else {
                processPhoto(currentLocation!!, filenamePath, itemIdPhotoType, cameraImageUri, jobToDecline)
            }
        } catch (e: Exception) {
            toast(R.string.error_getting_image)
            Timber.e(e)
            throw e
        }
    }

    private fun processPhoto(
        photoLocation: LocationModel,
        filename_path: Map<String, String>,
        itemId_photoType: Map<String, String>,
        imageUri: Uri,
        jobToDecline: JobDTO?
    ) {
        Coroutines.main {
            declinedata = createItemEntry(jobToDecline,filename_path, photoLocation)
            GlideApp.with(this)
                .load(imageUri)
                .centerCrop()
                .into(ui.potholeImage)
            ui.saveButton.isClickable = true
            ui.saveButton.visibility = View.VISIBLE

        }
    }

    private fun createItemEntry(jobToDecline: JobDTO?, filename_path: Map<String, String>, photoLocation: LocationModel): JobDeclineDTO {
        val jobToDeclineId = DataConversion.toLittleEndian(jobToDecline?.jobId).toString()
        return JobDeclineDTO(
           "", 2, filename_path["filename"] ?: error(""), jobToDeclineId,
            DateUtil.dateToString(Date())!!, photoLocation.latitude,
            photoLocation.longitude, filename_path["path"] ?: error(""),
        )
    }


    override fun onDestroyView() {
        _ui = null
        super.onDestroyView()
    }
}
