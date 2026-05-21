package edu.gr05513.locator

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.mapview.MapView
import edu.gr05513.locator.locating.Locator
import edu.gr05513.locator.ui.theme.LocatorTheme

class MainActivity : ComponentActivity() {
    private val vm by viewModels<MainViewModel>()

    // 1.
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAPS_API_KEY)
        MapKitFactory.initialize(this)

        //2.
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){ isGranted ->
            vm.accessLocation = when {
                isGranted.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    startLocating()
                    true
                } //  действия, если разрешение предоставлено для точного определения координат
                isGranted.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> false // действия, если разрешение предоставлено для приближенного определения координат
                else -> false // В разрешении отказано
            }
        }

        enableEdgeToEdge()
        setContent {
            LocatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(innerPadding)
                    ) {
                        ShowLocationAccessStatus(
                            vm.accessLocation,
                        )
                        /*LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(vm.locations){
                                Text(text = "Lat=${it.latitude}; Lon=${it.longitude}",
                                    modifier = Modifier.padding(8.dp))
                            }
                        }*/
                        MapScreen(
                            locations = vm.locations,
                            currentLocation = vm.locations.lastOrNull()
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        // 3. Непосредственный запрос разрешений - в любом месте по необходимости
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    fun startLocating(){
        Locator.addLocationListener { location ->
            vm.locations.add(location)
            println("Location Added $location")
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Locator.start(this)
    }
}

@Composable
fun ShowLocationAccessStatus(
    fineAccessGranted: Boolean,
    modifier: Modifier = Modifier
) {
    val result = if (fineAccessGranted) stringResource(R.string.access_granted) else stringResource(
        R.string.access_restricted
    )
    val color = if (fineAccessGranted) Color.Green else Color.Red
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.access, result),
            modifier = Modifier.padding(12.dp),
            color = color,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ShowLocationAccessStatusPreview() {
    LocatorTheme {
        ShowLocationAccessStatus(false)
    }
}

@Composable
fun MapScreen(
    locations: List<Location>,
    modifier: Modifier = Modifier,
    currentLocation: Location? = null,
){
    println("Updating Map Screen $currentLocation")

    var currentPoint by remember { mutableStateOf<Point?>(null) }
    var yandexMap by remember { mutableStateOf<com.yandex.mapkit.map.Map?>(null) }
    var locationCircle by remember { mutableStateOf<Circle?>(null) }

    currentLocation?.let {
        currentPoint = Point(currentLocation.latitude, currentLocation.longitude)
    }

    AndroidView(factory = { context ->
        MapView(context).also{ view ->
            view.mapWindow.map
        }
    }, update = { mapView ->
        val map = yandexMap
        println("Updating Map $currentLocation")
        currentPoint?.let { center ->
            val circle = Circle(center, 5f)
            val position = Circle(center, 100f)
            mapView.mapWindow.map.mapObjects.addCircle(
                circle
            )
            val path = Polyline(locations.map {
                Point(it.latitude, it.longitude)
            })
            mapView.mapWindow.map.mapObjects.addPolyline(path)
            val g1 = Geometry.fromPolyline(path)
            val g2 = Geometry.fromCircle(position)
            mapView.mapWindow.map.cameraPosition(g1).let {
                mapView.mapWindow.map.move(it)
            }
        }

    })
}