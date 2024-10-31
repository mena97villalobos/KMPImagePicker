package shared.location.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.location.LocationRequest
import shared.location.Location
import shared.location.model.Coordinates
import shared.location.model.LocationData
import java.lang.ref.WeakReference

fun Location.Companion.processRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String?>,
    grantResults: IntArray
) = locationManager.processRequestPermissionsResult(
    requestCode,
    permissions,
    grantResults
)

fun Location.Companion.showNotificationSetting() =
    locationManager.showNotificationSetting()

fun Location.Companion.setLocationRequest(locationRequest: LocationRequest) =
    locationManager.setLocationRequest(locationRequest)


internal var Location.Companion.activity: Activity?
    get() = locationManager.activity?.get()
    set(value) { locationManager.activity = WeakReference(value) }

internal fun Location.Companion.configure(context: Context) =
    locationManager.configure(context)

fun android.location.Location.toLocationData(): LocationData = LocationData(
    accuracy.toDouble(),
    altitude,
    0.0,
    bearing.toDouble(),
    speed.toDouble(),
    Coordinates(latitude, longitude)
)