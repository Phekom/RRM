package za.co.xisystems.itis_rrm.ui.mainview.work.goto_work_location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.databinding.FragmentGotoBinding
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.work.goto_work_location.navigation.NavigationFragmentViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.goto_work_location.navigation.NavigationFragmentViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines


class GoToFragment : BaseFragment(), DIAware
//    OnMapReadyCallback,
//    PermissionsListener,
//    LocationEngineCallback<LocationEngineResult>
{

    override val di by closestDI()

    private val factory: GoToViewModelFactory by instance()
    private lateinit var goToViewModel: GoToViewModel
    private lateinit var workViewModel: WorkViewModel
    private val myfactory: WorkViewModelFactory by instance()
    private lateinit var navViewModel: NavigationFragmentViewModel
    private val navfactory: NavigationFragmentViewModelFactory by instance()
    private var _ui: FragmentGotoBinding? = null
    private val ui get() = _ui!!

    private var mapView: MapView? = null
    private var drivingRoute: DirectionsRoute? = null
    private val walkingRoute: DirectionsRoute? = null
    private val cyclingRoute: DirectionsRoute? = null
    private val lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_DRIVING
    private var client: MapboxDirections? = null
    private var origin: Point? = null
    private var destination: Point? = null
    private val route by lazy { getDirectionsRoute() }


    override fun onAttach(context: Context) {
        Mapbox.getInstance(requireActivity(), requireActivity().getString(R.string.token))
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.goto_work_location)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Mapbox.getInstance(requireActivity(), requireActivity().getString(R.string.token))
        super.onCreate(savedInstanceState)
    }

    private var myWorkLocationPoint : Point? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentGotoBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        goToViewModel = activity?.run {
            ViewModelProvider(requireActivity(), factory).get(GoToViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        navViewModel = activity?.run {
            ViewModelProvider(this, navfactory).get(NavigationFragmentViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        workViewModel = activity?.run {
            ViewModelProvider(requireActivity(), myfactory).get(WorkViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        requireActivity().hideKeyboard()
        Coroutines.main {

        mapView = _ui?.mapbox
        mapView?.onCreate(savedInstanceState)


            mapView?.getMapAsync(OnMapReadyCallback { mapboxMap ->
                mapboxMap.setStyle(Style.MAPBOX_STREETS) { style -> // Set the origin location to the Alhambra landmark in Granada, Spain.
                    origin = Point.fromLngLat(currentLocation?.longitude!!, currentLocation?.latitude!!)
                    destination = myWorkLocationPoint
                    initSource(style)
                    initLayers(style)
                    val routeLineSource = style.getSourceAs<GeoJsonSource>(GEOJSON_SOURCE_ID)
//                    if (routeLineSource != null) {
//                        when (lastSelectedDirectionsProfile) {
//                            DirectionsCriteria.PROFILE_DRIVING -> routeLineSource.setGeoJson(
//                                LineString.fromPolyline(
//                                    drivingRoute?.geometry()!!,
//                                    PRECISION_6
//                                )
//                            )
//                            DirectionsCriteria.PROFILE_WALKING -> routeLineSource.setGeoJson(
//                                LineString.fromPolyline(
//                                    walkingRoute?.geometry()!!,
//                                    PRECISION_6
//                                )
//                            )
//                            DirectionsCriteria.PROFILE_CYCLING -> routeLineSource.setGeoJson(
//                                LineString.fromPolyline(
//                                    cyclingRoute?.geometry()!!,
//                                    PRECISION_6
//                                )
//                            )
//                            else -> {
//                            }
//                        }
//                    }
                    // Get the directions route from the Mapbox Directions API
                    getRoute(style, origin!!, destination!!,mapboxMap,routeLineSource)
                }
            })

            _ui?.startnavigating?.setOnClickListener {
            Coroutines.main {
                navViewModel.createNewRoute(drivingRoute)
                Navigation.findNavController(it) .navigate(R.id.action_work_location_to_work_navigation)
            }
//                val options = NavigationLauncherOptions.builder()
//                    .directionsRoute(route)
//                    .shouldSimulateRoute(true)
//                    .build()
////                ToastUtils().toastShort(requireContext(), options.toString())
//                NavigationLauncher.startNavigation(ACTIVITY, options)

        }

          _ui?.startnavigating?.hide()
            workViewModel.myWorkItem.observe(
                viewLifecycleOwner,
                {
                    myWorkLocationPoint = it

                }
            )


        }
    }

    private fun getDirectionsRoute(): DirectionsRoute {
        val directionsRouteAsJson =   "{\"routeIndex\":\"0\",\"distance\":1988.6,\"duration\":433.9,\"geometry\":\"}oragA|dnmhFtO{SvC_ExCtEvLjPfSrXbCdDeCdDmI~KkLzOyGbJqCzD}B`Bc\\\\xc@wBvES^Sb@cAtByApCu@v@_An@wGx@aSdCkCZZxE`Cn^x@zLt@bLF|@XdEXlEx@dMvDbn@TrDXvElClb@`@|G^|ElAlR|@~Mf@|H`@bGfHjgAv@vMX|EtAlRxA`UnCdb@jAvQdJtvAZvETlDXjE~Cdf@nEfr@JxADp@Bf@Dd@Dv@N|BdKv~AzAnUdInoAgEh@wn@pH}C^YD\\\\`FzAjUtGnbATtD`@EvC]j@Ir_@oE\",\"weight\":774.2,\"weight_name\":\"routability\",\"legs\":[{\"distance\":1988.6,\"duration\":433.9,\"summary\":\"Fremont Street, Pine Street\",\"steps\":[{\"distance\":53.7,\"duration\":10.3,\"geometry\":\"}oragA|dnmhFtO{SvC_E\",\"name\":\"Beale Street\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.396255,37.791503],\"bearing_before\":0.0,\"bearing_after\":135.0,\"instruction\":\"Head southeast on Beale Street\",\"type\":\"depart\",\"modifier\":\"right\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":53.7,\"announcement\":\"Head southeast on Beale Street, then turn right onto Mission Street\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eHead southeast on Beale Street, then turn right onto Mission Street\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":53.7,\"primary\":{\"text\":\"Mission Street\",\"components\":[{\"text\":\"Mission Street\",\"type\":\"text\",\"abbr\":\"Mission St\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"right\"},\"sub\":{\"text\":\"Fremont Street\",\"components\":[{\"text\":\"Fremont Street\",\"type\":\"text\",\"abbr\":\"Fremont St\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"right\"}}],\"driving_side\":\"right\",\"weight\":34.9,\"intersections\":[{\"location\":[-122.396255,37.791503],\"bearings\":[135],\"entry\":[true],\"out\":0}]},{\"distance\":108.6,\"duration\":36.7,\"geometry\":\"ozqagA`jmmhFxCtEvLjPfSrXbCdD\",\"name\":\"Mission Street\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.395825,37.79116],\"bearing_before\":135.0,\"bearing_after\":225.0,\"instruction\":\"Turn right onto Mission Street\",\"type\":\"turn\",\"modifier\":\"right\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":44.4,\"announcement\":\"Turn right onto Fremont Street\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eTurn right onto Fremont Street\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":108.6,\"primary\":{\"text\":\"Fremont Street\",\"components\":[{\"text\":\"Fremont Street\",\"type\":\"text\",\"abbr\":\"Fremont St\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"right\"}}],\"driving_side\":\"right\",\"weight\":61.5,\"intersections\":[{\"location\":[-122.395825,37.79116],\"bearings\":[45,135,225,315],\"entry\":[true,true,true,false],\"in\":3,\"out\":2}]},{\"distance\":283.1,\"duration\":76.4,\"geometry\":\"qopagA|`omhFeCdDmI~KkLzOyGbJqCzD}B`Bc\\\\xc@wBvES^Sb@cAtByApCu@v@_An@wGx@aSdCkCZ\",\"name\":\"Fremont Street\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.396703,37.790473],\"bearing_before\":223.0,\"bearing_after\":315.0,\"instruction\":\"Turn right onto Fremont Street\",\"type\":\"turn\",\"modifier\":\"right\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":263.1,\"announcement\":\"In 900 feet, turn left onto Pine Street\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eIn 900 feet, turn left onto Pine Street\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"},{\"distanceAlongGeometry\":55.6,\"announcement\":\"Turn left onto Pine Street\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eTurn left onto Pine Street\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":283.1,\"primary\":{\"text\":\"Pine Street\",\"components\":[{\"text\":\"Pine Street\",\"type\":\"text\",\"abbr\":\"Pine St\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"left\"}}],\"driving_side\":\"right\",\"weight\":132.8,\"intersections\":[{\"location\":[-122.396703,37.790473],\"bearings\":[45,135,225,315],\"entry\":[false,false,true,true],\"in\":0,\"out\":3},{\"location\":[-122.398298,37.791734],\"bearings\":[135,300],\"entry\":[false,true],\"in\":0,\"out\":1}]},{\"distance\":1217.6,\"duration\":218.89999999999998,\"geometry\":\"yhtagAbxrmhFZxE`Cn^x@zLt@bLF|@XdEXlEx@dMvDbn@TrDXvElClb@`@|G^|ElAlR|@~Mf@|H`@bGfHjgAv@vMX|EtAlRxA`UnCdb@jAvQdJtvAZvETlDXjE~Cdf@nEfr@JxADp@Bf@Dd@Dv@N|BdKv~AzAnUdInoA\",\"name\":\"Pine Street\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.39861,37.792413],\"bearing_before\":350.0,\"bearing_after\":260.0,\"instruction\":\"Turn left onto Pine Street\",\"type\":\"turn\",\"modifier\":\"left\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":1197.6,\"announcement\":\"Continue on Pine Street for a half mile\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eContinue on Pine Street for a half mile\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"},{\"distanceAlongGeometry\":389.4,\"announcement\":\"In a quarter mile, turn right onto Taylor Street\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eIn a quarter mile, turn right onto Taylor Street\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"},{\"distanceAlongGeometry\":83.4,\"announcement\":\"Turn right onto Taylor Street, then turn left onto California Street\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eTurn right onto Taylor Street, then turn left onto California Street\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":1217.6,\"primary\":{\"text\":\"Taylor Street\",\"components\":[{\"text\":\"Taylor Street\",\"type\":\"text\",\"abbr\":\"Taylor St\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"right\"}},{\"distanceAlongGeometry\":83.4,\"primary\":{\"text\":\"Taylor Street\",\"components\":[{\"text\":\"Taylor Street\",\"type\":\"text\",\"abbr\":\"Taylor St\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"right\"},\"sub\":{\"text\":\"California Street\",\"components\":[{\"text\":\"California Street\",\"type\":\"text\",\"abbr\":\"California St\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"left\"}}],\"driving_side\":\"right\",\"weight\":364.5,\"intersections\":[{\"location\":[-122.39861,37.792413],\"bearings\":[75,165,255,345],\"entry\":[false,false,true,true],\"in\":1,\"out\":2},{\"location\":[-122.399785,37.792261],\"bearings\":[75,165,255,345],\"entry\":[false,true,true,false],\"in\":0,\"out\":2},{\"location\":[-122.400959,37.792116],\"bearings\":[75,165,255,345],\"entry\":[false,false,true,true],\"in\":0,\"out\":2},{\"location\":[-122.402598,37.791909],\"bearings\":[75,165,255,345],\"entry\":[false,true,true,false],\"in\":0,\"out\":2},{\"location\":[-122.404233,37.791703],\"bearings\":[75,180,255,345],\"entry\":[false,false,true,true],\"in\":0,\"out\":2},{\"location\":[-122.40576,37.791505],\"bearings\":[75,165,255,345],\"entry\":[false,false,true,true],\"in\":0,\"out\":2},{\"location\":[-122.407358,37.791301],\"bearings\":[75,165,255,345],\"entry\":[false,true,true,true],\"in\":0,\"out\":2},{\"location\":[-122.408952,37.791098],\"bearings\":[75,165,255,345],\"entry\":[false,false,true,true],\"in\":0,\"out\":2},{\"location\":[-122.409044,37.791087],\"bearings\":[75,165,255,345],\"entry\":[false,true,true,false],\"in\":0,\"out\":2},{\"location\":[-122.410639,37.790884],\"bearings\":[75,165,255,345],\"entry\":[false,true,true,false],\"in\":0,\"out\":2}]},{\"distance\":107.7,\"duration\":25.8,\"geometry\":\"e|pagA|nmnhFgEh@wn@pH}C^YD\",\"name\":\"Taylor Street\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.412287,37.790675],\"bearing_before\":260.0,\"bearing_after\":350.0,\"instruction\":\"Turn right onto Taylor Street\",\"type\":\"turn\",\"modifier\":\"right\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":62.6,\"announcement\":\"Turn left onto California Street, then turn left onto Jones Street\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eTurn left onto California Street, then turn left onto Jones Street\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":107.7,\"primary\":{\"text\":\"California Street\",\"components\":[{\"text\":\"California Street\",\"type\":\"text\",\"abbr\":\"California St\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"left\"}},{\"distanceAlongGeometry\":62.6,\"primary\":{\"text\":\"California Street\",\"components\":[{\"text\":\"California Street\",\"type\":\"text\",\"abbr\":\"California St\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"left\"},\"sub\":{\"text\":\"Jones Street\",\"components\":[{\"text\":\"Jones Street\",\"type\":\"text\",\"abbr\":\"Jones St\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"left\"}}],\"driving_side\":\"right\",\"weight\":70.3,\"intersections\":[{\"location\":[-122.412287,37.790675],\"bearings\":[75,165,255,345],\"entry\":[false,false,true,true],\"in\":0,\"out\":3}]},{\"distance\":146.2,\"duration\":42.4,\"geometry\":\"}wragA~zmnhF\\\\`FzAjUtGnbATtD\",\"name\":\"California Street\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.41248,37.791631],\"bearing_before\":350.0,\"bearing_after\":260.0,\"instruction\":\"Turn left onto California Street\",\"type\":\"turn\",\"modifier\":\"left\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":51.7,\"announcement\":\"Turn left onto Jones Street, then you will arrive at your destination\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eTurn left onto Jones Street, then you will arrive at your destination\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":146.2,\"primary\":{\"text\":\"Jones Street\",\"components\":[{\"text\":\"Jones Street\",\"type\":\"text\",\"abbr\":\"Jones St\",\"abbr_priority\":0}],\"type\":\"turn\",\"modifier\":\"left\"}}],\"driving_side\":\"right\",\"weight\":86.8,\"intersections\":[{\"location\":[-122.41248,37.791631],\"bearings\":[75,165,255,345],\"entry\":[true,false,true,true],\"in\":1,\"out\":2}]},{\"distance\":71.7,\"duration\":23.4,\"geometry\":\"wjragAraqnhF`@EvC]j@Ir_@oE\",\"name\":\"Jones Street\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.414122,37.79142],\"bearing_before\":260.0,\"bearing_after\":170.0,\"instruction\":\"Turn left onto Jones Street\",\"type\":\"turn\",\"modifier\":\"left\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":15.3,\"announcement\":\"You have arrived at your destination, on the right\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eYou have arrived at your destination, on the right\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":71.7,\"primary\":{\"text\":\"You will arrive\",\"components\":[{\"text\":\"You will arrive\",\"type\":\"text\"}],\"type\":\"arrive\",\"modifier\":\"right\"}},{\"distanceAlongGeometry\":15.3,\"primary\":{\"text\":\"You have arrived\",\"components\":[{\"text\":\"You have arrived\",\"type\":\"text\"}],\"type\":\"arrive\",\"modifier\":\"right\"}}],\"driving_side\":\"right\",\"weight\":23.4,\"intersections\":[{\"location\":[-122.414122,37.79142],\"bearings\":[75,165,255,345],\"entry\":[false,true,true,true],\"in\":0,\"out\":1}]},{\"distance\":0.0,\"duration\":0.0,\"geometry\":\"}bqagAtypnhF\",\"name\":\"Jones Street\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.413995,37.790783],\"bearing_before\":171.0,\"bearing_after\":0.0,\"instruction\":\"You have arrived at your destination, on the right\",\"type\":\"arrive\",\"modifier\":\"right\"},\"voiceInstructions\":[],\"bannerInstructions\":[],\"driving_side\":\"right\",\"weight\":0.0,\"intersections\":[{\"location\":[-122.413995,37.790783],\"bearings\":[351],\"entry\":[true],\"in\":0}]}],\"annotation\":{\"distance\":[41.75872891540546,11.943930596414795,12.720147968330114,34.58112560559672,50.96480933738249,10.349556668565532,10.428744124966986,26.062939337835754,33.61207745353073,22.152629772397148,11.584122498337331,8.22504067141732,73.25791925952063,11.603839142628226,1.7930152150745173,1.9339724019326627,6.4182775391464615,8.137686480966448,3.8827293592940286,4.137411880032337,15.778927074065876,36.18605026816596,7.882482295429149,9.70628239052936,44.88520535070527,19.777529614845466,18.70069668033605,2.7608352091952852,8.820943994209998,9.167951891088123,20.211271187411434,67.05841934336348,8.004641462478803,9.602196312248573,50.45852431578543,12.710488322858675,9.917381766860117,27.67756036992424,21.374879966188537,14.151341864692084,11.581827207587462,103.10585401718104,20.975906679342675,9.863007930072529,27.750876716852083,31.428390784757813,50.12927675688965,26.70541846540595,124.91524890087112,9.619673955896003,7.744241825057303,9.081282300519934,55.824774196397755,72.99751351106848,4.01125146327413,2.22260556286811,1.7719511994183288,1.7030446015893967,2.4836226090145,5.608523009263369,136.39335067018692,32.0538539535892,114.65384371553199,11.274751280297357,86.03444465258252,8.898707903215941,1.4697888668235173,10.071397847728418,31.880018736822993,96.1785674755274,8.091589695903005,1.90914496306627,8.555399787319097,2.4861312264216893,58.775343965717596],\"congestion\":[\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"heavy\",\"moderate\",\"moderate\",\"moderate\",\"heavy\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"moderate\",\"moderate\",\"moderate\",\"moderate\",\"heavy\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"moderate\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\"]}}],\"routeOptions\":{\"baseUrl\":\"https://api.mapbox.com\",\"user\":\"mapbox\",\"profile\":\"driving-traffic\",\"coordinates\":[[-122.396485,37.7913239],[-122.4142621,37.7907495]],\"alternatives\":true,\"language\":\"en\",\"continue_straight\":false,\"roundabout_exits\":false,\"geometries\":\"polyline6\",\"overview\":\"full\",\"steps\":true,\"annotations\":\"congestion,distance\",\"voice_instructions\":true,\"banner_instructions\":true,\"voice_units\":\"metric\",\"access_token\":\"${getString(R.string.token)}\",\"uuid\":\"ck7dtdd2z00yx75plynvtan26\"},\"voiceLocale\":\"en\"}"

        return DirectionsRoute.fromJson(directionsRouteAsJson)
    }
    private fun initSource(@NonNull loadedMapStyle: Style) {
        loadedMapStyle.addSource(GeoJsonSource(GEOJSON_SOURCE_ID))
        val iconGeoJsonSource = GeoJsonSource(
            ICON_SOURCE_ID, FeatureCollection.fromFeatures(
                arrayOf(
                    Feature.fromGeometry(Point.fromLngLat(origin?.longitude()!!, origin?.latitude()!!)),
                    Feature.fromGeometry(Point.fromLngLat(destination?.longitude()!!, destination?.latitude()!!))
                )
            )
        )
        loadedMapStyle.addSource(iconGeoJsonSource)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initLayers(@NonNull loadedMapStyle: Style) {
        val routeLayer = LineLayer(ROUTE_LAYER_ID, GEOJSON_SOURCE_ID)

// Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
            lineCap(Property.LINE_CAP_ROUND),
            lineJoin(Property.LINE_JOIN_ROUND),
            lineWidth(5f),
            lineColor(Color.parseColor("#009688"))
        )
        loadedMapStyle.addLayer(routeLayer)

// Add the red marker icon image to the map
        loadedMapStyle.addImage(
            RED_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                resources.getDrawable(R.drawable.mapbox_marker_icon_default)
            )!!
        )

// Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(
            SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                iconImage(RED_PIN_ICON_ID),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(arrayOf(0f, -9f))
            )
        )
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        requireActivity().menuInflater.inflate(R.menu.main, menu)
//        val item = menu.findItem(androidx.lifecycle.R.id.action_settings)
        val item1 = menu.findItem(R.id.action_logout)

//        if (item != null) item.isVisible = true
        if (item1 != null) item1.isVisible = true

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_logout -> {
                Intent(requireActivity(), LoginActivity::class.java).also { home ->
                    home.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(home)
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun isAlive(): Boolean {
        return !isDead()
    }

    protected fun isDead(): Boolean {
        return activity == null || requireActivity().isFinishing
    }

    private fun getRoute(style: Style, origin: Point, destination: Point, mapboxMap: MapboxMap, source: GeoJsonSource?) {
        client = MapboxDirections.builder()
            .origin(origin)
            .destination(destination)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .accessToken(getString(R.string.token))
            .build()
        client?.enqueueCall(object : Callback<DirectionsResponse?> {
            override fun onResponse(call: Call<DirectionsResponse?>, response: Response<DirectionsResponse?>) {

                Timber.d("Response code: %s", response.code())
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.")
                    return
                } else if (response.body()!!.routes().size < 1) {
                    Timber.e("No routes found")
                    return
                }

                drivingRoute = response.body()!!.routes()[0]
                val structure = LatLng(destination.latitude(), destination.longitude())
                val bounds = LatLngBounds.Builder()
                    .include(LatLng(origin.latitude(), origin.longitude()))
                    .include(LatLng(structure))
                    .build()

                mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
                if (drivingRoute != null) {
                    _ui?.startnavigating?.show()
                    drawNavigationPolylineRoute(response.body()!!.routes()[0], source)
                }

                ToastUtils().toastShort(requireContext(), drivingRoute?.distance().toString())

            }

            override fun onFailure(call: Call<DirectionsResponse?>, throwable: Throwable) {
                Timber.e("Error: %s", throwable.message)
                Toast.makeText(
                    requireContext(), "Error: " + throwable.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun drawNavigationPolylineRoute(route: DirectionsRoute,source: GeoJsonSource?) {
        source?.setGeoJson(LineString.fromPolyline(route.geometry()!!, PRECISION_6))
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        requireActivity()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the Directions API request
        if (client != null) {
            client!!.cancelCall()
        }
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }


    companion object {
        private const val TAG = "GoToFragment"
        private const val GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID"
        private const val ICON_LAYER_ID = "ICON_LAYER_ID"
        private const val ICON_SOURCE_ID = "ICON_SOURCE_ID"
        private const val ROUTE_LAYER_ID = "ROUTE_LAYER_ID"
        private val RED_PIN_ICON_ID = "RED_PIN_ICON_ID"

    }
}
