package shared.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.CancellationTokenSource
import shared.location.utils.LocationUtil
import shared.location.Location.Companion.notifyOnLocationUnavailable
import shared.location.model.Coordinates
import shared.location.model.LocationData
import shared.location.observer.ActivityLifecycleObserver
import shared.location.utils.toLocationData
import java.lang.ref.WeakReference

internal actual class LocationManager actual constructor() {
    // -------------------------------------------------------------------------------------------
    //  Public (Actual)
    // -------------------------------------------------------------------------------------------

    actual fun isPermissionAllowed() = focusedActivity?.let {
        ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    } ?: false

    actual fun removeAllListeners() { }

    actual fun removeListeners(target: Any) { }

    actual fun requestPermission() {
        val activity = focusedActivity ?: run {
            notifyOnLocationUnavailable()
            return
        }

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            requestPermissionsRequestCode
        )
    }

    @SuppressLint("MissingPermission")
    actual fun startLocationUpdating() {
        val activity = focusedActivity ?: run {
            notifyOnLocationUnavailable()
            return
        }

        if (!isPermissionAllowed()) {
            requestPermission()
            notifyOnLocationUnavailable()
        } else if(!LocationUtil.checkLocationEnable(activity)) {
            notifyOnLocationUnavailable()
        } else {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

    }

    actual fun stopLocationUpdating() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    actual fun getCurrentLocation() {
        val activity = focusedActivity ?: run {
            notifyOnLocationUnavailable()
            return
        }

        var isLocationNotified = false

        if (!isPermissionAllowed()) {
            requestPermission()
            notifyOnLocationUnavailable()
        } else if(!LocationUtil.checkLocationEnable(activity)) {
            notifyOnLocationUnavailable()
        } else {

            val cts = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).addOnSuccessListener { location ->
                if(location != null) {
                    Location.notifyOnLocationUpdated(location.toLocationData())
                    isLocationNotified = true

                    // For update latest location
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }

            }.addOnFailureListener {}

            Handler(Looper.getMainLooper()).postDelayed({
                if(!isLocationNotified) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if(location != null) {
                            Location.notifyOnLocationUpdated(location.toLocationData())
                            isLocationNotified = true

                            // For update latest location
                            fusedLocationClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.getMainLooper()
                            )
                        }
                    }.addOnFailureListener {}
                }
            }, (5 * 1000).toLong())

            Handler(Looper.getMainLooper()).postDelayed({
                if(!isLocationNotified) {
                    notifyOnLocationUnavailable()
                }
            }, (10 * 1000).toLong())

        }

    }

    // -------------------------------------------------------------------------------------------
    //  Internal
    // -------------------------------------------------------------------------------------------

    internal var activity: WeakReference<Activity>? = null

    internal fun configure(context: Context) {
        val application = context.applicationContext as? Application
        application?.registerActivityLifecycleCallbacks(ActivityLifecycleObserver) ?: run {
            val activity = context.applicationContext as? Activity
            this.activity = WeakReference(activity)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.locations.last() ?: return
                val coordinates = Coordinates(location.latitude, location.longitude)
                val data = LocationData(
                    location.accuracy.toDouble(),
                    location.altitude,
                    0.0,
                    location.bearing.toDouble(),
                    location.speed.toDouble(),
                    coordinates)
                Location.notifyOnLocationUpdated(data)
            }
        }

        val settings = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        LocationServices
            .getSettingsClient(context)
            .checkLocationSettings(settings)
    }

    internal fun processRequestPermissionsResult(
        requestCode: Int,
        @Suppress("UNUSED_PARAMETER")
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            requestPermissionsRequestCode -> {
                if (grantResults.isEmpty()) {
                    return
                }
                when (grantResults[0]) {
                    PackageManager.PERMISSION_GRANTED -> {
                        startLocationUpdating()
                        Location.notifyOnPermissionUpdated(true)
                    }
                    PackageManager.PERMISSION_DENIED ->
                        Location.notifyOnPermissionUpdated(false)
                    else -> Unit
                }
            }
        }
    }

    internal fun showNotificationSetting() {
        val activity = focusedActivity ?: run {
            notifyOnLocationUnavailable()
            return
        }

        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                data = Uri.fromParts("package", activity.applicationInfo.packageName, null)
                activity.startActivity(this)
            }
    }

    // -------------------------------------------------------------------------------------------
    //  Private
    // -------------------------------------------------------------------------------------------

    private val requestPermissionsRequestCode = 4885

    private val focusedActivity: Activity?
        get() = activity?.get()?.let {
            if (it.isFinishing || it.isDestroyed) null else { it }
        }

    @SuppressLint("StaticFieldLeak")
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fastestInterval = 1 * 1000L
        interval = 10 * 1000L
    }

    fun setLocationRequest(request: LocationRequest) {
        locationRequest = LocationRequest.create().apply {
            priority = request.priority
            request.fastestInterval.let { fastestInterval = it }
            request.interval.let { interval = it }
            request.maxWaitTime.let { maxWaitTime = it }
            request.smallestDisplacement.let { smallestDisplacement = it }
            request.isWaitForAccurateLocation.let { isWaitForAccurateLocation = it }
            request.numUpdates.let { numUpdates = it }
            request.expirationTime.let { expirationTime = it }
        }
    }
}