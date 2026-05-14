package edu.gr05513.locator.locating

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService

object Locator : LocationListener {

    private val listeners = mutableListOf<(Location)->Unit>()
    fun addLocationListener(listener: (Location)->Unit){
        listeners.add(listener)
    }
    fun removeLocationListener(listener: (Location)->Unit){
        listeners.remove(listener)
    }

    override fun onLocationChanged(loc: Location) {
        listeners.forEach { it(loc) }
    }

    @RequiresPermission(allOf = [
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    fun start(context: Context) {
        val locator = context.getSystemService<LocationManager>()
        locator?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getLastKnownLocation(LocationManager.FUSED_PROVIDER)
                requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10f,
                    Locator
                )
            } else {
                getLastKnownLocation(LocationManager.GPS_PROVIDER)
                requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10f,
                    Locator
                )
            }
        }
    }
}