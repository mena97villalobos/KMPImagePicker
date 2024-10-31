package shared.location

internal expect class LocationManager() {
    fun isPermissionAllowed(): Boolean
    fun removeAllListeners()
    fun removeListeners(target: Any)
    fun requestPermission()
    fun startLocationUpdating()
    fun stopLocationUpdating()
    fun getCurrentLocation()
}