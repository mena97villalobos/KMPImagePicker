package kmp.image.picker.util

import android.content.Context
import androidx.startup.Initializer
import shared.location.Location
import shared.location.utils.configure


@Suppress("UNUSED")
internal class ModuleInitializer: Initializer<Int> {
    override fun create(context: Context): Int {
        Location.configure(context)
        return 0
    }
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}