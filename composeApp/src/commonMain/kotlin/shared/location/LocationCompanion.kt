package shared.location

import shared.location.model.LocationData

typealias OnLocationUpdatedBlock = (location: LocationData) -> Unit
typealias OnLocationUnavailableBlock = () -> Unit
typealias OnPermissionUpdatedBlock = (isGranted: Boolean) -> Unit

interface LocationCompanion {
    fun isPermissionAllowed(): Boolean
    fun currentLocation(block: OnLocationUpdatedBlock)
    fun requestPermission()
    fun startLocationUpdating()
    fun stopLocationUpdating()

    fun onLocationUnavailable(target: Any, block: OnLocationUnavailableBlock): LocationCompanion
    fun onLocationUpdated(target: Any, block: OnLocationUpdatedBlock): LocationCompanion
    fun onPermissionUpdated(target: Any, block: OnPermissionUpdatedBlock): LocationCompanion

    fun removeAllListeners()
    fun removeListeners(target: Any)
    fun removeOnLocationUnavailable(target: Any)
    fun removeOnLocationUpdated(target: Any)
    fun removeOnPermissionUpdated(target: Any)
}