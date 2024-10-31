package shared.location

enum class LocationAuthorizationStatus(val rawValue: Int) {
    NotSet(-1),
    NotDetermined(0),
    Restricted(1),
    Denied(2),
    AuthorizedAlways(3),
    AuthorizedWhenInUse(4);

    companion object {
        fun fromInt(value: Int) = entries.first { it.rawValue == value }
    }
}