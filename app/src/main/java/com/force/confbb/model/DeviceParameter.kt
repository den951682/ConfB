package com.force.confbb.model

data class DeviceParameter<Type>(
    val id: Int,
    val value: Type,
    val name: String? = null,
    val description: String? = null,
    val minValue: Type? = null,
    val maxValue: Type? = null,
    val editable: Boolean = false
)
