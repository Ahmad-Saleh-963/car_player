package com.example.my_car.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class VehicleTelemetry(
    val speedKmh: Int? = null,
    val engineTempC: Int? = null,
    val rpm: Int? = null,
    val batteryVoltage: Float? = null,
    val isGpsActive: Boolean = false,
    val isCanBusActive: Boolean = false
)
