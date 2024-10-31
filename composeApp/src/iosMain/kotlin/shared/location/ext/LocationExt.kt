package shared.location.ext

import shared.location.Location
import shared.location.LocationAuthorizationStatus
import shared.location.LocationCompanion

typealias OnAlwaysAllowsPermissionRequiredBlock = () -> Unit

var Location.Companion.requiredPermission: LocationAuthorizationStatus
    get() = locationManager.requiredPermission.value
    set(value) { locationManager.requiredPermission.value = value }

fun Location.Companion.onAlwaysAllowsPermissionRequired(
    target: Any,
    block: OnAlwaysAllowsPermissionRequiredBlock
): LocationCompanion {
    locationManager.onAlwaysAllowsPermissionRequired(target, block)
    return this
}

fun Location.Companion.removeOnAlwaysAllowsPermissionRequired(target: Any) =
    locationManager.removeOnAlwaysAllowsPermissionRequired(target)

internal var Location.Companion.previousAuthorizationStatus: LocationAuthorizationStatus
    get() = locationManager.previousAuthorizationStatus.value
    set(value) { locationManager.previousAuthorizationStatus.value = value }
