package za.co.xisystems.itis_rrm.ui.mainview.work.goto_work_location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.delegates.listeners.eventdata.MapLoadErrorType
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.ui.tripprogress.model.PercentDistanceTraveledFormatter
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import com.mapbox.navigation.ui.voice.model.SpeechVolume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.FragmentGotoBinding
import za.co.xisystems.itis_rrm.extensions.displayPromptForEnablingGPS
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import java.util.Locale

@Suppress("MagicNumber")
class GoToFragment: LocationFragment(), PermissionsListener {

    override val di by closestDI()
    private val factory: GoToViewModelFactory by instance()
    private lateinit var goToViewModel: GoToViewModel
    private lateinit var workViewModel: WorkViewModel
    private val workFactory: WorkViewModelFactory by instance()
    private var _binding: FragmentGotoBinding? = null
    private val binding get() = _binding!!
    private val permissionsManager = PermissionsManager(this)
    private var myWorkLocationPoint: Point? = null
    private var distanceLeft = 0.0

    private companion object {
        private const val BUTTON_ANIMATION_DURATION = 1500L
        private const val REQUEST_STORAGE_PERMISSION = 1010
    }

    private lateinit var itemEstimate: JobItemEstimateDTO
    private lateinit var itemEstimateJob: JobDTO
    private lateinit var lm: LocationManager
    private var gpsEnabled = false
    private var networkEnabled = false

    private val mapboxReplayer = MapboxReplayer()
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private val pixelDensity = Resources.getSystem().displayMetrics.density

    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            20.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private lateinit var maneuverApi: MapboxManeuverApi
    private lateinit var tripProgressApi: MapboxTripProgressApi
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView
    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()
    private lateinit var routeArrowView: MapboxRouteArrowView
    private var isVoiceInstructionsMuted = false
        set(value) {
            field = value
            if (value) {
                binding.soundButton.muteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                binding.soundButton.unmuteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(1f))
            }
        }
    private lateinit var speechApi: MapboxSpeechApi
    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer
    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
    }
    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
            expected.fold(
                { error ->
                    // play the instruction via fallback text-to-speech engine
                    voiceInstructionsPlayer.play(
                        error.fallback,
                        voiceInstructionsPlayerCallback
                    )
                },
                { value ->
                    // play the sound file from the external generator
                    voiceInstructionsPlayer.play(
                        value.announcement,
                        voiceInstructionsPlayerCallback
                    )
                }
            )
        }
    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> { value ->
            // remove already consumed file to free-up space
            speechApi.clean(value)
        }
    private val navigationLocationProvider = NavigationLocationProvider()

    private val locationObserver = object: LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            // update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )

            // update camera position to account for new location
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            // if this is the first location update the activity has received,
            // it's best to immediately move the camera to the current user location
            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                navigationCamera.requestNavigationCameraToOverview(
                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                        .maxDuration(0) // instant transition
                        .build()
                )
            }
        }
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        // draw the upcoming maneuver arrow on the map
        val style = mapboxMap.getStyle()
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        // update top banner with maneuver instructions
        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Toast.makeText(
                    requireContext(),
                    error.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            {
                binding.maneuverView.visibility = View.VISIBLE
                binding.maneuverView.renderManeuvers(maneuvers)
            }
        )
        distanceLeft = tripProgressApi.getTripProgress(routeProgress).distanceRemaining
        if (distanceLeft.equals(0.0)) {
            binding.startWorkBTN.visibility = View.VISIBLE
        }
        // update bottom trip progress summary
        binding.tripProgressView.render(
            tripProgressApi.getTripProgress(routeProgress)
        )
    }

    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.routes.isNotEmpty()) {
            // generate route geometries asynchronously and render them
            val routeLines = routeUpdateResult.routes.map { RouteLine(it, null) }

            routeLineApi.setRoutes(
                routeLines
            ) { value ->
                mapboxMap.getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }

            // update the camera position to account for the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.routes.first())
            viewportDataSource.evaluate()
        } else {
            // remove the route line and route arrow from the map
            val style = mapboxMap.getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }

            // remove the route reference from camera position evaluations
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }

    private val arrivalObserver: ArrivalObserver = object: ArrivalObserver {
        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
            // buildingApi.queryBuildingOnFinalDestination(routeProgress, callback)
            if (routeProgress.distanceRemaining < 70.0) {
            }
        }

        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {
//            mapboxMap.getStyle { style ->
//                //buildingView.removeBuildingHighlight(style)
//            }
        }

        override fun onWaypointArrival(routeProgress: RouteProgress) {
            // buildingApi.queryBuildingOnWaypoint(routeProgress, callback)
//            if (routeProgress.distanceRemaining < 70.0){
//                closeApp()
//            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGotoBinding.inflate(inflater, container, false)
        goToViewModel =
            ViewModelProvider(this.requireActivity(), factory).get(GoToViewModel::class.java)
        workViewModel =
            ViewModelProvider(this.requireActivity(), workFactory).get(WorkViewModel::class.java)
        binding.startWorkBTN.visibility = View.GONE

        workViewModel.myWorkItem.observe(
            viewLifecycleOwner,
            {
                myWorkLocationPoint = it

                mapboxMap = binding.mymapView.getMapboxMap()
                // initialize the location puck
                binding.mymapView.location.apply {
                    this.locationPuck = LocationPuck2D(
                        bearingImage = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.mapbox_navigation_puck_icon
                        )
                    )
                    setLocationProvider(navigationLocationProvider)
                    enabled = true
                }

                generateRoot(myWorkLocationPoint)
            }
        )
        workViewModel.workItemJob.observe(viewLifecycleOwner, { estimateJob ->
            estimateJob?.let {
                itemEstimateJob = it
            }
        })

        workViewModel.workItem.observe(viewLifecycleOwner, { estimate ->
            estimate?.let {
                itemEstimate = it
            }
        })

        binding.startWorkBTN.setOnClickListener {
            Coroutines.io {
                workViewModel.setWorkItemJob(itemEstimateJob.jobId)
                workViewModel.setWorkItem(itemEstimate.estimateId)
                withContext(Dispatchers.Main.immediate) {
                    val navDirection = GoToFragmentDirections.actionWorkLocationToCaptureWorkFragment(
                        jobId = itemEstimateJob.jobId,
                        estimateId = itemEstimate.estimateId
                    )
                    Navigation.findNavController(requireView()).navigate(navDirection)
                }
            }
        }

        return binding.root
    }

    private fun getCurrentLocation(): LocationModel? {
        return super.getLocation()
    }

    private fun generateRoot(structure: Point?) {
        initStyle()
        initNavigation(structure)
    }

    private fun initStyle() {
        mapboxMap.loadStyleUri(
            Style.MAPBOX_STREETS, {},
            object: OnMapLoadErrorListener {
                @SuppressLint("LogNotTimber")
                override fun onMapLoadError(mapLoadErrorType: MapLoadErrorType, message: String) {
                    Log.e(
                        GoToFragment::class.java.simpleName,
                        "Error loading map: " + mapLoadErrorType.name
                    )
                }
            }
        )
    }

    @SuppressLint("MagicNumber")
    private fun initNavigation(structure: Point?) {
        // initialize Mapbox Navigation
        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(this.requireContext())
                    .accessToken(getString(R.string.mapbox_access_token))
                    // comment out the location engine setting block to disable simulation
                    .locationEngine(replayLocationEngine)
                    .build()

            ).apply {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    this@GoToFragment.extensionToast(
                        message = "Please enable location services in order to proceed",
                        style = ToastStyle.WARNING,
                        position = ToastGravity.CENTER,
                        duration = ToastDuration.LONG
                    )
                } else {
                    startTripSession()
                    // register event listeners
                    registerRoutesObserver(routesObserver)
                    registerArrivalObserver(arrivalObserver)
                    registerRouteProgressObserver(routeProgressObserver)
                    registerLocationObserver(locationObserver)
                    registerVoiceInstructionsObserver(voiceInstructionsObserver)
                    registerRouteProgressObserver(replayProgressObserver)
                }
            }
        }

        if (mapboxNavigation.getRoutes().isEmpty()) {
            if (structure != null) {
                val destination = Point.fromLngLat(structure.longitude(), structure.latitude())

                Coroutines.main {
                    // val originLocation = navigationLocationProvider.lastLocation
                    val myOriginLocation: LocationModel? = this.getCurrentLocation()
                    val originLocation = Location("rrm").apply {
                        longitude = myOriginLocation?.longitude!!
                        latitude = myOriginLocation.latitude
                        bearing = 10f
                    }
//                    Toast.makeText(requireContext(),"${estimateLocation}", Toast.LENGTH_SHORT).show()
//                    ToastUtils().toastShort(requireContext()," $originLocation     AND $destination")

                    findRoute(originLocation, destination)
                }
            } else {
                ToastUtils().toastShort(requireContext(), "Error loading Route is not Found")
            }
        }

        // initialize Navigation Camera
        viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
        navigationCamera = NavigationCamera(
            mapboxMap,
            binding.mymapView.camera,
            viewportDataSource
        )
        binding.mymapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )

        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
            // shows/hide the recenter button depending on the camera state
            when (navigationCameraState) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING,
                NavigationCameraState.FOLLOWING -> binding.recenter.visibility = View.INVISIBLE
                NavigationCameraState.TRANSITION_TO_OVERVIEW,
                NavigationCameraState.OVERVIEW,
                NavigationCameraState.IDLE -> binding.recenter.visibility = View.VISIBLE
            }
        }
        // set the padding values depending on screen orientation and visible view layout
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.overviewPadding = landscapeOverviewPadding
        } else {
            viewportDataSource.overviewPadding = overviewPadding
        }
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.followingPadding = landscapeFollowingPadding
        } else {
            viewportDataSource.followingPadding = followingPadding
        }
        // make sure to use the same DistanceFormatterOptions across different features
        val distanceFormatterOptions = mapboxNavigation.navigationOptions.distanceFormatterOptions

        // initialize maneuver api that feeds the data to the top banner maneuver view
        maneuverApi = MapboxManeuverApi(
            MapboxDistanceFormatter(distanceFormatterOptions)
        )

        // initialize bottom progress view
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(requireContext())
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(requireContext())
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(requireContext(), TimeFormat.NONE_SPECIFIED)
                )
                .build()
        )

        // initialize voice instructions api and the voice instruction player
        speechApi = MapboxSpeechApi(
            requireContext(),
            getString(R.string.mapbox_access_token),
            Locale.ENGLISH.language
        )
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
            requireContext(),
            getString(R.string.mapbox_access_token),
            Locale.ENGLISH.language
        )

        // initialize route line, the withRouteLineBelowLayerId is specified
        // to place the route line below road labels layer on the map
        // the value of this option will depend on the style that you are using
        // and under which layer the route line should be placed on the map layers stack
        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(requireContext())
            .withRouteLineBelowLayerId("road-label")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(requireContext()).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)

        // initialize view interactions
        binding.stop.setOnClickListener {
            clearRouteAndStopNavigation()
        }
        binding.recenter.setOnClickListener {
            navigationCamera.requestNavigationCameraToFollowing()
            binding.routeOverview.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.routeOverview.setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
            binding.recenter.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.soundButton.setOnClickListener {
            // mute/ un-mute voice instructions
            isVoiceInstructionsMuted = !isVoiceInstructionsMuted
        }

        // set initial sounds button state
        binding.soundButton.unmute()
    }

    private fun findRoute(originLocation: Location?, destination: Point) { // destination: Point
        // val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

        // execute a route request
        // it's recommended to use the
        // applyDefaultNavigationOptions and applyLanguageAndVoiceUnitOptions
        // that make sure the route request is optimized
        // to allow for support of all of the Navigation SDK features
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(requireContext())
                .voiceUnits(DirectionsCriteria.METRIC)
                .coordinatesList(listOf(originPoint, destination))
                // provide the bearing for the origin of the request to ensure
                // that the returned route faces in the direction of the current user movement
                .bearingsList(
                    listOf(
                        Bearing.builder()
                            .angle(originLocation.bearing.toDouble())
                            .degrees(45.0)
                            .build(),
                        null
                    )
                )
                .build(),
            object: RouterCallback {
                override fun onRoutesReady(
                    routes: List<DirectionsRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    setRouteAndStartNavigation(routes)
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    // no impl
                    ToastUtils().toastShort(requireContext(), " $reasons")
                }

                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    // no impl
                }
            }
        )
    }

    private fun setRouteAndStartNavigation(routes: List<DirectionsRoute>) {
        // set routes, where the first route in the list is the primary route that
        // will be used for active guidance
        mapboxNavigation.setRoutes(routes)

        // start location simulation along the primary route
        startSimulation(routes.first())

        // show UI elements
        binding.soundButton.visibility = View.VISIBLE
        binding.routeOverview.visibility = View.VISIBLE
        binding.tripProgressCard.visibility = View.VISIBLE

        // move the camera to overview when new route is available
        navigationCamera.requestNavigationCameraToOverview()
    }

    private fun clearRouteAndStopNavigation() {
        // clear
        mapboxNavigation.setRoutes(listOf())

        // stop simulation
        mapboxReplayer.stop()

        // hide UI elements
        binding.soundButton.visibility = View.INVISIBLE
        binding.maneuverView.visibility = View.INVISIBLE
        binding.routeOverview.visibility = View.INVISIBLE
        binding.tripProgressCard.visibility = View.INVISIBLE
    }

    private fun startSimulation(route: DirectionsRoute) {
        mapboxReplayer.run {
            stop()
            clearEvents()
            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
            pushEvents(replayEvents)
            seekTo(replayEvents.first())
            play()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().hideKeyboard()
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            requestStoragePermission()
        } else {
            permissionsManager.requestLocationPermissions(requireActivity())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        ToastUtils().toastShort(
            requireContext(),
            "This app needs location and storage permissions in order to perform its core functionality."
        )
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            requestStoragePermission()
        } else {
            ToastUtils().toastShort(requireContext(), "You didn't grant the permissions required to use the app")
        }
    }

    private fun requestStoragePermission() {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val permissionsNeeded: MutableList<String> = ArrayList()
        if (
            ContextCompat.checkSelfPermission(requireContext(), permission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(permission)
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsNeeded.toTypedArray(),
                10
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapboxNavigation.run {
            // make sure to unregister the routes observer you have registered.
            unregisterRoutesObserver(routesObserver)
            // make sure to unregister the arrival observer you have registered.
            unregisterArrivalObserver(arrivalObserver)
            // make sure to unregister the location observer you have registered.
            unregisterLocationObserver(locationObserver)
            // make sure to unregister the route progress observer you have registered.
            unregisterRouteProgressObserver(routeProgressObserver)
            unregisterRouteProgressObserver(replayProgressObserver)
        }
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
        MapboxNavigationProvider.destroy()
        mapboxNavigation.onDestroy()
        _binding = null
    }

    override fun onStop() {
        super.onStop()

        // unregister event listeners to prevent leaks or unnecessary resource consumption
        mapboxNavigation.run {
            unregisterRoutesObserver(routesObserver)
            unregisterRouteProgressObserver(routeProgressObserver)
            unregisterLocationObserver(locationObserver)
            unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
            unregisterRouteProgressObserver(replayProgressObserver)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override fun onStart() {
        super.onStart()
        lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            Timber.e(e)
        }

        if (!gpsEnabled) { // notify user && !network_enabled
            displayPromptForEnablingGPS(requireActivity())
        }
    }
}
