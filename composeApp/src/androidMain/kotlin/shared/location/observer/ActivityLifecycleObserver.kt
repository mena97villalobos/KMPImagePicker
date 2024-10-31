package shared.location.observer

import android.app.Activity
import android.app.Application
import android.os.Bundle
import shared.location.Location
import shared.location.utils.activity

internal object ActivityLifecycleObserver : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(p0: Activity) {}
    override fun onActivityStarted(p0: Activity) {}
    override fun onActivityDestroyed(p0: Activity) {}
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityStopped(p0: Activity) {}
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        val currentActivity = Location.activity
        if (currentActivity != null && !currentActivity.isFinishing && !currentActivity.isDestroyed) {
            return
        }
        Location.activity = p0
    }

    override fun onActivityResumed(p0: Activity) {}
}