/**
 * Updated by Shaun McDonald on 2021/05/18
 * Last modified on 2021/05/18, 10:26
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog.Builder
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.lifecycle.whenResumed
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ColorToast
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.databinding.FragmentPhotoEstimateBinding
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.io.File
import java.text.DecimalFormat
import kotlin.collections.set

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

class EstimatePhotoFragment : LocationFragment(), DIAware {

    private var sectionId: String? = null
    override val di by closestDI()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()
    private var mAppExecutor: AppExecutor? = null
    private lateinit var lm: LocationManager
    private var gpsEnabled = false
    private var networkEnabled = false
    private var isEstimateDone: Boolean = false
    private var disableGlide: Boolean = false
    private var locationWarning: Boolean = false
    private var _ui: FragmentPhotoEstimateBinding? = null
    private val ui get() = _ui!!
    private var photoType: PhotoType = PhotoType.START
    private var itemIdPhotoType: HashMap<String, String> = HashMap()
    private var filenamePath: HashMap<String, String> = HashMap()
    private var item: ItemDTOTemp? = null
    private var newJob: JobDTO? = null
    internal var estimate: JobItemEstimateDTO? = null
    var direction: String? = null
    private var newJobItemEstimate: JobItemEstimateDTO? = null
    var quantity: Double = 1.0
    private var estimateId: String? = null
    private lateinit var newJobItemEstimatesPhotosList: ArrayList<JobItemEstimatesPhotoDTO>
    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    internal var description: String? = null
    private var currentUser: Int = -1
    private var startImageUri: Uri? = null
    private var endImageUri: Uri? = null
    private var imageUri: Uri? = null
    private val uiScope = UiLifecycleScope()
    private lateinit var photoUtil: PhotoUtil
    private var changesToPreserve: Boolean = false
    private var tenderRate: Double? = null

    /**
     * ActivityResultContract for taking a photograph
     */
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved ->
        if (isSaved) {
            Coroutines.io {
                imageUri?.let { realUri ->
                    processAndSetImage(realUri)
                }
            }
        } else {
            imageUri?.let { failedUri ->
                Coroutines.io {
                    val filenamePath = File(failedUri.path!!)
                    photoUtil.deleteImageFile(filenamePath.toString())
                    withContext(Dispatchers.Main.immediate) {
                        haltAnimation()
                        ui.startImageView.visibility = View.VISIBLE
                        ui.endImageView.visibility = View.VISIBLE
                    }
                }
            }
        }
        this@EstimatePhotoFragment.photosDone()
    }

    init {
        System.setProperty("kotlinx.coroutines.debug", if (BuildConfig.DEBUG) "on" else "off")
        lifecycleScope.launch {

            whenCreated {
                uiScope.onCreate()
                createViewModel = activity?.run {
                    ViewModelProvider(this, factory).get(CreateViewModel::class.java)
                } ?: throw Exception("Invalid Activity")
            }

            whenStarted {
                viewLifecycleOwner.lifecycle.addObserver(uiScope)
            }

            whenResumed {
                if (newJob == null) readNavArgs()
            }
        }
    }

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 505
    }

    private fun pullData() = uiScope.launch(uiScope.coroutineContext) {

        withContext(uiScope.coroutineContext) {
            var myUserId: Int?
            createViewModel.loggedUser.distinctUntilChanged().observe(
                viewLifecycleOwner,
                { user ->
                    user?.let {
                        myUserId = it
                        onUserFound(myUserId)
                    }
                })
        }

        withContext(uiScope.coroutineContext) {
            var myJobDTO: JobDTO?
            createViewModel.itemJob.distinctUntilChanged().observe(
                viewLifecycleOwner,
                { jobEvent ->
                    jobEvent.getContentIfNotHandled()?.let { jobDTO ->
                        myJobDTO = jobDTO
                        onJobFound(myJobDTO!!)
                    }
                })
        }

        withContext(uiScope.coroutineContext) {
            createViewModel.tempProjectItem.distinctUntilChanged().observe(
                viewLifecycleOwner,
                { itemEvent ->
                    itemEvent.getContentIfNotHandled()?.let { item ->
                        onItemFound(item)
                    }
                })
        }

        withContext(uiScope.coroutineContext) {
            createViewModel.currentEstimate.distinctUntilChanged().observe(
                viewLifecycleOwner,
                { estimateEvent ->
                    estimateEvent.getContentIfNotHandled()?.let { estimateItem ->
                        onEstimateFound(estimateItem)
                    }
                })
        }
    }

    private fun readNavArgs() = uiScope.launch(uiScope.coroutineContext) {
        val args: EstimatePhotoFragmentArgs? by navArgs()
        args?.let { navArgs ->

            if (!navArgs.jobId.isNullOrEmpty()) {
                createViewModel.setJobToEdit(navArgs.jobId.toString())
            }
            if (!navArgs.itemId.isNullOrEmpty()) {
                createViewModel.setCurrentProjectItem(navArgs.itemId.toString())
            }
            if (!navArgs.estimateId.isNullOrEmpty()) {
                estimateId = navArgs.estimateId
                estimateId?.let { realEstimateId ->
                    createViewModel.setEstimateToEdit(realEstimateId)
                }
            } else {
                createViewModel.currentEstimate = MutableLiveData()
            }
        }
    }

    private fun onUserFound(foundUserId: Int?): Int {
        if (foundUserId != null) {
            currentUser = foundUserId
        }
        return currentUser
    }

    private fun onJobFound(foundJobDTO: JobDTO?): JobDTO {
        newJob = foundJobDTO!!
        if (estimateId != null) {
            val estimateDTO = newJob?.jobItemEstimates?.find { itemEstimate ->
                itemEstimate.estimateId == estimateId
            }
            if (estimateDTO != null) {
                newJobItemEstimate = estimateDTO
            }
        }
        return newJob as JobDTO
    }

    private fun onItemFound(itemDTO: ItemDTOTemp?): ItemDTOTemp? {
        if (itemDTO != null) {
            item = itemDTO
            ui.titleTextView.text =
                getString(R.string.pair, item!!.itemCode, item!!.descr)
            tenderRate = item!!.tenderRate
        } else {
            toast(
                "item is null in " + javaClass.simpleName
            )
            return item
        }

        if (newJob != null) {
            if (newJobItemEstimate == null && item != null) {
                newJobItemEstimate = newJob?.getJobEstimateByItemId(item?.itemId)
                if (newJobItemEstimate != null) {
                    newJobItemEstimate?.lineRate = item?.tenderRate!!
                }
            }
            restoreEstimateViewState()
        }
        return item
    }

    private fun onEstimateFound(estimateDTO: JobItemEstimateDTO) {
        newJobItemEstimate = estimateDTO
        quantity = newJobItemEstimate?.qty ?: 1.0
        if (newJob != null) {
            restoreEstimateViewState()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentPhotoEstimateBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.edit_estimate)
        locationWarning = false
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navToAddProject(this@EstimatePhotoFragment.requireView())
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this@EstimatePhotoFragment, callback)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // no options menu
        return false
        // To change body of created functions use File | Settings | File Templates.
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_settings)
        val item1 = menu.findItem(R.id.action_logout)
        val item2 = menu.findItem(R.id.action_search)
        if (item != null) item.isVisible = false
        if (item1 != null) item1.isVisible = false
        if (item2 != null) item2.isVisible = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.edit_estimate)
        newJobItemEstimatesList = ArrayList()
        newJobItemEstimatesPhotosList = ArrayList()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        photoUtil = PhotoUtil.getInstance(this.requireContext().applicationContext)

        var stateRestored = false
        val args: EstimatePhotoFragmentArgs by navArgs()
        if (args.jobId?.isNotBlank() == true) {
            readNavArgs()
            stateRestored = true
        }
        if (savedInstanceState != null && !stateRestored) {
            onRestoreInstanceState(savedInstanceState)
        }
    }

    override fun onStart() {
        super.onStart()
        ui.group13Loading.visibility = View.GONE
        mAppExecutor = AppExecutor()
        lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setButtonClicks()

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            Timber.e(e)
        }

        if (!gpsEnabled) { // notify user && !network_enabled
            displayPromptForEnablingGPS(requireActivity())
        }

        setValueEditText(getStoredValue())

        ui.valueEditText.doOnTextChanged { text, _, _, _ ->
            changesToPreserve = true
            try {
                val quantity = text.toString().toDouble()
                newJobItemEstimate?.qty = quantity
                createViewModel.setEstimateQuantity(quantity)
                setCost()
            } catch (ex: java.lang.NumberFormatException) {
                Timber.e(" ")
            }
        }
    }

    private fun setButtonClicks() {

        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {

                R.id.startPhotoButton -> {
                    locationWarning = false
                    ui.startImageView.visibility = View.GONE
                    ui.startAnimationView.visibility = View.VISIBLE
                    takePhoto(PhotoType.START)
                    this@EstimatePhotoFragment.takingPhotos()
                }
                R.id.endPhotoButton -> {
                    locationWarning = false
                    ui.endImageView.visibility = View.GONE
                    ui.endAnimationView.visibility = View.VISIBLE
                    takePhoto(PhotoType.END)
                    this@EstimatePhotoFragment.takingPhotos()
                }

                R.id.cancelButton -> {
                    Coroutines.main {
                        buildDeleteDialog(this@EstimatePhotoFragment.requireView())
                    }
                }

                R.id.updateButton -> {
                    validateAndUpdateEstimate(view)
                }
            }
        }

        ui.startPhotoButton.setOnClickListener(myClickListener)
        ui.endPhotoButton.setOnClickListener(myClickListener)
        ui.cancelButton.setOnClickListener(myClickListener)
        ui.updateButton.setOnClickListener(myClickListener)

        // If the user hits the enter key on the costing field,
        // hide the keypad.

        ui.valueEditText.setOnEditorActionListener { _, _, event ->
            if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                requireActivity().hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun validateAndUpdateEstimate(view: View) {
        if (ui.costTextView.text.isNullOrEmpty() || newJobItemEstimate!!.size() != 2) {
            toast("Please Make Sure you have Captured Both Images")
            ui.labelTextView.startAnimation(animations!!.shake_long)
        } else {
            Coroutines.main {
                createViewModel.isEstimateComplete(newJobItemEstimate!!).also { result ->
                    if (result) {
                        calculateCost()
                        this@EstimatePhotoFragment.toggleLongRunning(true)
                        saveValidEstimate(view)
                    }
                }
            }
        }
    }

    private fun navToAddProject(view: View) {
        val directions = EstimatePhotoFragmentDirections.actionEstimatePhotoFragmentToAddProjectFragment2(
            newJob?.projectId!!,
            newJob?.jobId!!
        )
        Navigation.findNavController(view).navigate(directions)
    }

    private suspend fun saveValidEstimate(view: View) = uiScope.launch(uiScope.coroutineContext) {

        item!!.quantity = ui.valueEditText.text.toString().toDouble()
        if (item!!.quantity > 0 && item!!.tenderRate > 0.0 && changesToPreserve) {

            val saveValidEstimate = newJobItemEstimate!!.copy(
                qty = item!!.quantity,
                lineRate = item!!.tenderRate
            )
            Coroutines.io {
                createViewModel.backupProjectItem(item!!)
                createViewModel.setTempProjectItem(item!!)
                createViewModel.backupEstimate(saveValidEstimate)
                newJob!!.insertOrUpdateJobItemEstimate(saveValidEstimate)
                createViewModel.saveNewJob(newJob!!)
                withContext(Dispatchers.Main.immediate) {
                    changesToPreserve = false
                    createViewModel.setJobToEdit(newJob!!.jobId)
                    createViewModel.setEstimateQuantity(saveValidEstimate.qty)
                    createViewModel.setEstimateLineRate(saveValidEstimate.lineRate)
                    createViewModel.setEstimateToEdit(saveValidEstimate.estimateId)
                    updateData(view)
                }
            }
        }
    }

    private fun updateData(view: View) {
        this.toggleLongRunning(false)
        createViewModel.unbindEstimateView()
        newJob?.let {
            navToAddProject(view)
        }
    }

    private fun takePhoto(picType: PhotoType) {

        photoType = picType
        initLaunchCamera()
    }

    private fun initLaunchCamera() {
        if (ContextCompat.checkSelfPermission(
                requireActivity().applicationContext,
                permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    permission.CAMERA,
                    permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            launchCamera()
        }
    }

    private fun displayPromptForEnablingGPS(
        activity: Activity
    ) {

        val builder = Builder(activity)
        builder.setCancelable(false)
        val action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        val message = ("Your GPS seems to be disabled, Please enable it to continue")
        builder.setMessage(message)
            .setPositiveButton("OK") { d, _ ->
                activity.startActivity(Intent(action))
                d.dismiss()
            }
        builder.create().show()
    }

    private fun launchCamera() {
        // type is "start" or "end"
        if (item != null) {
            itemIdPhotoType["itemId"] = item!!.itemId
            itemIdPhotoType["type"] = photoType.name
        }
        Coroutines.main {
            imageUri = photoUtil.getUri()
            withContext(Dispatchers.Main.immediate) {
                this@EstimatePhotoFragment.takingPhotos()
                takePicture.launch(imageUri)
            }
        }
    }

    @Synchronized
    private suspend fun restoreEstimatePhoto(
        // jobItemEstimate: JobItemEstimateDTO,
        photo: JobItemEstimatesPhotoDTO,
        animate: Boolean = false
    ) = Coroutines.main {

        var targetImageView = ui.startImageView
        var targetTextView = ui.startSectionTextView

        if (!photo.isPhotostart) {
            targetImageView = ui.endImageView
            targetTextView = ui.endSectionTextView
        }

        val targetUri = null ?: extractImageUri(photo)

        when (photo.isPhotostart) {
            true -> {
                startImageUri = targetUri
                photoType = PhotoType.START
            }
            else -> {
                endImageUri = targetUri
                photoType = PhotoType.END
            }
        }
        if (targetUri != null) {
            photo.sectionMarker?.let { mark ->
                targetTextView.text = mark
            }
            loadEstimateItemPhoto(targetUri, targetImageView, animate)
        }
    }

    private suspend fun extractImageUri(
        jobItemEstimatePhoto: JobItemEstimatesPhotoDTO?
    ): Uri? = withContext(Dispatchers.IO) {
        if (jobItemEstimatePhoto != null) {
            val path: String = jobItemEstimatePhoto.photoPath
            Timber.d("x -> photo $path")

            if (path.isNotBlank()) {
                return@withContext photoUtil.getUri(path)
            } else {
                return@withContext null
            }
        }
        return@withContext null
    }

    @SuppressLint("RestrictedApi")
    private fun processAndSetImage(
        imageUri: Uri
        // item: ItemDTOTemp?,
        // newJobDTO: JobDTO?,
        // estimate: JobItemEstimateDTO?
    ) {
        Coroutines.main {
            try { //  Location of picture
                val estimateLocation: LocationModel? = this.getCurrentLocation()
                Timber.d("x -> $estimateLocation")
                if (estimateLocation != null) {

                    //  Save Image to Internal Storage
                    withContext(Dispatchers.IO) {
                        filenamePath = photoUtil.saveImageToInternalStorage(
                            imageUri
                        ) as HashMap<String, String>
                    }

                    processPhotoEstimate(
                        estimateLocation = estimateLocation,
                        filePath = filenamePath,
                        itemidPhototype = itemIdPhotoType
                    )
                } else {
                    resetPhotos()
                    displayPromptForEnablingGPS(this@EstimatePhotoFragment.requireActivity())
                }
            } catch (e: Exception) {
                toast(R.string.error_getting_image)
                Timber.e(e)
                throw e
            }
        }
    }

    private fun processPhotoEstimate(
        estimateLocation: LocationModel,
        filePath: Map<String, String>,
        itemidPhototype: Map<String, String>
    ) = uiScope.launch(uiScope.coroutineContext) {

        val itemId = item?.itemId ?: itemidPhototype["itemId"]

        if (newJobItemEstimate == null) {
            newJobItemEstimate = newJob?.getJobEstimateByItemId(itemId)
        }

        if (newJobItemEstimate == null) {
            newJobItemEstimate = createViewModel.createItemEstimate(
                itemId = itemId,
                newJob = newJob,
                item = item
            )

            item = item!!.copy(estimateId = newJobItemEstimate!!.estimateId)

            if (newJob?.jobItemEstimates == null) {
                newJob?.jobItemEstimates = ArrayList()
            }

            newJob?.insertOrUpdateJobItemEstimate(newJobItemEstimate!!)
        }

        newJobItemEstimate?.let {
            uiScope.launch(context = uiScope.coroutineContext) {
                processPhotoLocation(estimateLocation, filePath, itemidPhototype)
            }
        }
    }

    // TODO: polygon verification for offline photography
    private suspend fun processPhotoLocation(
        estimateLocation: LocationModel,
        filePath: Map<String, String>,
        itemidPhototype: Map<String, String>
    ) {
        try {
            toggleLongRunning(true)
            if (!this@EstimatePhotoFragment.disableGlide) {
                placeProvisionalPhoto(
                    filePath,
                    estimateLocation,
                    itemidPhototype
                )
            }
        } catch (t: Throwable) {
            disableGlide = true
            val message = "Failed to capture photo: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(t, message)
            val xiErr = XIResult.Error(t, message)
            crashGuard(
                throwable = xiErr,
                refreshAction = {
                    this.retryProcessPhotoLocation(
                        estimateLocation,
                        filePath,
                        itemidPhototype
                    )
                })
        } finally {
            toggleLongRunning(false)
            resetPhotos()
        }
    }

    private fun retryProcessPhotoLocation(
        estimateLocation: LocationModel,
        filePath: Map<String, String>,
        itemidPhototype: Map<String, String>
    ) {
        IndefiniteSnackbar.hide()
        Coroutines.main {
            disableGlide = false
            processPhotoLocation(estimateLocation, filePath, itemidPhototype)
        }
    }

    private fun resetPhotos() {
        haltAnimation()
        ui.startImageView.visibility = View.VISIBLE
        ui.endImageView.visibility = View.VISIBLE
        disableGlide = false
    }

    private suspend fun placeProvisionalPhoto(
        filePath: Map<String, String>,
        currentLocation: LocationModel,
        itemidPhototype: Map<String, String>
    ) = Coroutines.main {
        val photo = createViewModel.createItemEstimatePhoto(
            itemEst = newJobItemEstimate!!,
            filePath = filePath,
            currentLocation = currentLocation,
            itemIdPhotoType = itemidPhototype,
            pointLocation = -1.0
        )

        val savedPhoto = createViewModel.backupEstimatePhoto(photo)

        this@EstimatePhotoFragment.newJobItemEstimate!!.setJobItemEstimatePhoto(
            savedPhoto
        )
        createViewModel.backupEstimate(newJobItemEstimate!!)
        newJobItemEstimate = createViewModel.updateEstimatePhotos(
            newJobItemEstimate!!.estimateId,
            newJobItemEstimate!!.jobItemEstimatePhotos
        )
        newJob?.insertOrUpdateJobItemEstimate(newJobItemEstimate!!)

        this@EstimatePhotoFragment.disableGlide = false

        val targetAnimation: LottieAnimationView = when (photo.isPhotostart) {
            true -> ui.startAnimationView
            else -> ui.endAnimationView
        }

        targetAnimation.cancelAnimation()
        targetAnimation.visibility = View.GONE
        restoreEstimatePhoto(savedPhoto, true)
    }

    private fun showZoomedImage(imageUrl: Uri?) {
        val dialog = Dialog(requireContext(), R.style.dialog_full_screen)
        dialog.setContentView(R.layout.new_job_photo)
        val zoomageView =
            dialog.findViewById<ZoomageView>(R.id.zoomedImage)
        GlideApp.with(this.requireActivity())
            .load(imageUrl)
            .into(zoomageView)
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (newJob == null) {
            readNavArgs()
        } else {
            setCost()
        }
    }

    private fun loadEstimateItemPhoto(
        imageUri: Uri?,
        imageView: ImageView,
        animate: Boolean
    ) = try {
        GlideApp.with(this)
            .load(imageUri)
            .centerCrop()
            .error(R.drawable.no_image)
            .into(imageView)

        if (animate) imageView.startAnimation(bounce_1000)

        imageView.setOnClickListener {
            showZoomedImage(imageUri)
        }
    } catch (e: Exception) {
        Timber.e(e)
    } finally {
        Coroutines.main {
            this.isEstimateDone = createViewModel.estimateComplete(newJobItemEstimate)

            if (isEstimateDone) {
                ui.costCard.visibility = View.VISIBLE
                ui.updateButton.visibility = View.VISIBLE
                setCost()
            } else {
                extensionToast(
                    message = "Please take both photographs ...",
                    style = ToastStyle.INFO,
                    position = ToastGravity.BOTTOM
                )
                hideCostCard()
            }
        }
    }

    private fun setValueEditText(qty: Double) {
        when (item?.uom) {
            "M2", "M3", "M" -> {
                val decQty = "" + qty
                ui.valueEditText.setText(decQty)
            }
            else -> {
                val intQty: String = "" + qty.toInt()
                ui.valueEditText.setText(intQty)
            }
        }
    }

    private fun setCost() {
        if (newJobItemEstimate?.size() == 2) {
            ui.valueEditText.visibility = View.VISIBLE
            ui.costTextView.visibility = View.VISIBLE
            ui.costTextView.startAnimation(animations!!.bounce_soft)
            ui.labelTextView.text = "Quantity"
            calculateCost()
        } else {
            ui.labelTextView.text = getString(R.string.warning_estimate_incomplete)
            ui.labelTextView.startAnimation(animations!!.shake_long)
            ui.valueEditText.visibility = View.GONE
            ui.costTextView.visibility = View.GONE
        }
    }

    private fun haltAnimation() {
        ui.startAnimationView.visibility = View.GONE
        ui.endAnimationView.visibility = View.GONE
    }

    private fun hideCostCard() {
        ui.costCard.visibility = View.GONE
        ui.updateButton.visibility = View.GONE
    }

    private fun calculateCost() {
        val item: ItemDTOTemp? = item

        val value = ui.valueEditText.text.toString()
        //  Lose focus on fields
        //  valueEditText.clearFocus()

        var lineAmount: Double?
        var tenderRate = newJobItemEstimate?.lineRate ?: item?.tenderRate ?: 0.0

        var qty = item?.quantity ?: value.toDouble()

        try {
            qty = value.toDouble()
        } catch (e: NumberFormatException) {
            Timber.d(e)
        }

        when (item?.uom) {
            "NO", "QTY", null -> {
                lineAmount = validateNumberCosting(qty, tenderRate)
            }
            "M2" -> {
                lineAmount = validateAreaCosting(qty, tenderRate)
            }
            "M3" -> {
                lineAmount = validateVolumeCosting(qty, tenderRate)
            }
            "PROV SUM" -> {
                lineAmount = validateProvSumCosting(qty, tenderRate)
            }
            "M" -> {
                lineAmount = validateLengthCosting(qty = qty, tenderRate = tenderRate)
            }
            else -> {
                ui.labelTextView.text = getString(R.string.label_quantity)
                try { //  Default Calculation
                    lineAmount = qty * tenderRate
                } catch (e: NumberFormatException) {
                    lineAmount = null
                    requireActivity().hideKeyboard()
                    Timber.d(e)
                    toast("Please enter the Quantity.")
                }
            }
        }

        val displayAmount = lineAmount ?: tenderRate * qty

        when (displayAmount > 0.0) {
            true -> {
                // Display pricing information
                ui.costTextView.text =
                    (" * R $tenderRate =  R ${DecimalFormat("#0.00").format(displayAmount)}")
                newJobItemEstimate?.qty = qty
                newJobItemEstimate?.lineRate = tenderRate
                createViewModel.setEstimateQuantity(qty)
                createViewModel.setEstimateLineRate(tenderRate)
                changesToPreserve = true
            }
        }
    }

    private fun validateLengthCosting(
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inLineAmount: Double? = null
        when (ui.labelTextView.text) {
            getString(R.string.label_length_m) ->
                try { //  Set the Area to the QTY
                    inLineAmount = qty * tenderRate!!
                } catch (e: NumberFormatException) {
                    requireActivity().hideKeyboard()
                    Timber.d(e)
                    toast("Please enter the m.")
                }
        }
        return inLineAmount
    }

    private fun validateProvSumCosting(
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inlineAmount: Double?
        ui.labelTextView.text = getString(R.string.label_amount)
        try {
            inlineAmount = qty * tenderRate!!
        } catch (e: NumberFormatException) {
            inlineAmount = null
            requireActivity().hideKeyboard()
            toast(getString(R.string.warning_estimate_enter_prov_sum))
        }
        return inlineAmount
    }

    private fun validateVolumeCosting(
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inlineAmount: Double?
        ui.labelTextView.text = getString(R.string.label_volume_m3)
        try {
            inlineAmount = qty * tenderRate!!
        } catch (e: NumberFormatException) {
            requireActivity().hideKeyboard()
            inlineAmount = null
            toast(getString(R.string.warning_estimate_enter_volume))
        }
        return inlineAmount
    }

    private fun validateAreaCosting(
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inLineAmount: Double?
        ui.labelTextView.text = getString(R.string.label_area_m2)
        try {
            inLineAmount = qty * tenderRate!!
        } catch (e: NumberFormatException) {
            inLineAmount = null
            requireActivity().hideKeyboard()
            toast("Please place the Area.")
        }
        return inLineAmount
    }

    private fun validateNumberCosting(
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inLineAmount: Double?
        ui.labelTextView.text = getString(R.string.label_quantity)
        try { //  make the change in the array and update view
            inLineAmount = qty * tenderRate!!
        } catch (e: NumberFormatException) {
            inLineAmount = null
            requireActivity().hideKeyboard()
            toast("Please place the Quantity.")
        }
        return inLineAmount
    }

    private fun getStoredValue(): Double {
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        return jobItemEstimate?.qty ?: quantity
    }

    private fun getJobItemEstimate(): JobItemEstimateDTO? {
        return newJobItemEstimate
    }

    private fun getCurrentLocation(): LocationModel? {
        return super.getLocation()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            outState.putString("jobId", newJob?.jobId)
            outState.putString("itemId", item?.itemId)
            newJobItemEstimate?.let { realEstimate ->
                outState.putString("estimateId", realEstimate.estimateId)
            }
        }
        super.onSaveInstanceState(outState)
        Timber.i("$outState")
    }

    private fun onRestoreInstanceState(inState: Bundle?) {

        val jobId = inState?.getString("jobId")
        estimateId = inState?.getString("estimateId")
        val itemId = inState?.getString("itemId")
        val locationErrorMessage = inState?.getString("locationErrorMessage")
        Coroutines.io {
            if (jobId != null) {
                createViewModel.setJobToEdit(jobId)
            }
            if (itemId != null) {
                createViewModel.setCurrentProjectItem(itemId)
            }
            if (estimateId != null) {
                createViewModel.setEstimateToEdit(estimateId!!)
            }
        }

        if (!locationErrorMessage.isNullOrBlank()) {
            val locationErrorToast = ColorToast(
                title = "Location Error",
                message = locationErrorMessage,
                style = ToastStyle.ERROR,
                gravity = ToastGravity.CENTER,
                duration = ToastDuration.LONG
            )
            this@EstimatePhotoFragment.extensionToast(locationErrorToast)
        }
    }

    fun restoreEstimateViewState() {

        sectionId = newJob?.sectionId
        if (sectionId != null) {
            createViewModel.setSectionId(sectionId!!)
        }
        // Load Photographs
        if (newJobItemEstimate != null) {
            uiScope.launch(uiScope.coroutineContext) {
                try {
                    quantity = newJobItemEstimate!!.qty
                    createViewModel.setEstimateQuantity(quantity)
                    isEstimateDone = createViewModel.estimateComplete(newJobItemEstimate)
                    newJobItemEstimate?.jobItemEstimatePhotos?.forEach { photo ->
                        restoreEstimatePhoto(
                            photo
                        )
                    }

                    if (isEstimateDone) {
                        ui.costCard.visibility = View.VISIBLE
                        ui.updateButton.visibility = View.VISIBLE
                        setValueEditText(quantity)
                        setCost()
                    }
                } catch (t: Throwable) {
                    Timber.e(t, "Failed to restore estimate view-state.")
                    val estError = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                    crashGuard(
                        throwable = estError,
                        refreshAction = { this@EstimatePhotoFragment.retryEstimateViewState() })
                }
            }
        }
    }

    fun retryEstimateViewState() {
        IndefiniteSnackbar.hide()
        restoreEstimateViewState()
    }

    /**
     * Called when the view previously created by [.onCreateView] has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after [.onStop] and before [.onDestroy].  It is called
     * *regardless* of whether [.onCreateView] returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        uiScope.destroy()
        createViewModel.itemJob.removeObservers(viewLifecycleOwner)
        createViewModel.tempProjectItem.removeObservers(viewLifecycleOwner)
        createViewModel.currentEstimate.removeObservers(viewLifecycleOwner)
        createViewModel.loggedUser.removeObservers(viewLifecycleOwner)
        _ui = null
    }

    private suspend fun buildDeleteDialog(view: View) {
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
            Coroutines.main {
                item?.let {
                    extensionToast(
                        title = "Deleting ...",
                        message = "${it.descr} removed.",
                        style = ToastStyle.DELETE,
                        position = ToastGravity.BOTTOM,
                        duration = ToastDuration.LONG
                    )
                    Coroutines.io {
                        createViewModel.deleteItemFromList(it.itemId, it.estimateId)
                        newJob?.removeJobEstimateByItemId(it.itemId)
                        createViewModel.backupJob(newJob!!)
                        createViewModel.setJobToEdit(newJob?.jobId!!)
                        changesToPreserve = false
                        withContext(Dispatchers.Main.immediate) {
                            parentFragmentManager.beginTransaction().remove(this@EstimatePhotoFragment).commit()
                            parentFragmentManager.beginTransaction().detach(this@EstimatePhotoFragment).commit()
                            navToAddProject(view)
                        }
                    }
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

    override fun onStop() {
        super.onStop()

        Coroutines.main {
            if (changesToPreserve) {
                item?.let {
                    createViewModel.backupProjectItem(it)
                }
                newJobItemEstimate?.let {
                    createViewModel.backupEstimate(it)
                    createViewModel.updateEstimatePhotos(it.estimateId, it.jobItemEstimatePhotos)
                    newJob?.insertOrUpdateJobItemEstimate(it)
                    createViewModel.setEstimateToEdit(it.estimateId)
                }
                newJob?.let {
                    createViewModel.backupJob(it)
                    createViewModel.setJobToEdit(it.jobId)
                }

                changesToPreserve = false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            readNavArgs()
        } else {
            onRestoreInstanceState(savedInstanceState)
        }
        pullData()
    }
}
