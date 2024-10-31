package shared.location.native

internal expect class NativeAtomicReference<T>(value: T) {
    var value: T
}