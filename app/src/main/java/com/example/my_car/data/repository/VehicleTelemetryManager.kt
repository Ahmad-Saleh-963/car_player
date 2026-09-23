package com.example.my_car.data.repository

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.my_car.data.model.VehicleTelemetry

object VehicleTelemetryManager {

    var telemetry by mutableStateOf(VehicleTelemetry())
        private set

    private var locationManager: LocationManager? = null
    private var isRegistered = false

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val speedKmh = if (location.hasSpeed()) {
                (location.speed * 3.6f).toInt().coerceAtLeast(0)
            } else {
                0
            }
            telemetry = telemetry.copy(
                speedKmh = speedKmh,
                isGpsActive = true
            )
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                if (voltageMv > 0) {
                    val realVolts = voltageMv / 1000f
                    telemetry = telemetry.copy(batteryVoltage = realVolts)
                }
            }
        }
    }

    private val canBusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            var current = telemetry

            // 1. Extract CANBus Speed
            if (intent.hasExtra("speed") || intent.hasExtra("car_speed")) {
                val canSpeed = intent.getIntExtra("speed", intent.getIntExtra("car_speed", -1))
                if (canSpeed >= 0) {
                    current = current.copy(speedKmh = canSpeed, isCanBusActive = true)
                }
            }

            // 2. Extract Engine / Coolant Temperature
            if (intent.hasExtra("coolant_temp") || intent.hasExtra("engine_temp") || intent.hasExtra("temp")) {
                val temp = intent.getIntExtra("coolant_temp", intent.getIntExtra("engine_temp", intent.getIntExtra("temp", -999)))
                if (temp in -40..200) {
                    current = current.copy(engineTempC = temp, isCanBusActive = true)
                }
            }

            // 3. Extract RPM
            if (intent.hasExtra("rpm") || intent.hasExtra("engine_rpm")) {
                val rpmVal = intent.getIntExtra("rpm", intent.getIntExtra("engine_rpm", -1))
                if (rpmVal >= 0) {
                    current = current.copy(rpm = rpmVal, isCanBusActive = true)
                }
            }

            // 4. Extract Battery Voltage from CANBus if available
            if (intent.hasExtra("battery") || intent.hasExtra("voltage")) {
                val volt = intent.getFloatExtra("battery", intent.getFloatExtra("voltage", -1f))
                if (volt > 0f) {
                    current = current.copy(batteryVoltage = volt, isCanBusActive = true)
                }
            }

            telemetry = current
        }
    }

    @SuppressLint("MissingPermission")
    fun startListening(context: Context) {
        if (isRegistered) return
        isRegistered = true

        // 1. Register Real Battery Hardware Voltage Receiver
        try {
            val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val stickyIntent = ContextCompat.registerReceiver(
                context,
                batteryReceiver,
                batteryFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            if (stickyIntent != null) {
                val voltageMv = stickyIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                if (voltageMv > 0) {
                    val realVolts = voltageMv / 1000f
                    telemetry = telemetry.copy(batteryVoltage = realVolts)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Initialize GPS Speed Updates
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationManager?.let { lm ->
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        500L,
                        0f,
                        locationListener
                    )
                }
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000L,
                        0f,
                        locationListener
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Register Vendor CANBus Broadcast Intent Filters
        val canFilter = IntentFilter().apply {
            addAction("android.intent.action.CANBUS_DATA")
            addAction("com.ts.intent.action.CANBUS_DATA")
            addAction("com.microntek.canbus.data")
            addAction("com.flyaudio.intent.action.CANBUS_DATA")
            addAction("com.syu.canbus.action.DATA")
            addAction("com.mcu.intent.action.CANBUS_INFO")
        }

        try {
            ContextCompat.registerReceiver(
                context,
                canBusReceiver,
                canFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopListening(context: Context) {
        if (!isRegistered) return
        isRegistered = false

        try {
            locationManager?.removeUpdates(locationListener)
            locationManager = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            context.unregisterReceiver(canBusReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
