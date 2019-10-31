package com.michaelfotiadis.heartbeat.bluetooth

enum class DeviceType(val code: Int) {
    DEVICE_TYPE_UNKNOWN(0),
    DEVICE_TYPE_CLASSIC(1),
    DEVICE_TYPE_LE(2),
    DEVICE_TYPE_DUAL(3);


    companion object {
        fun fromCode(code: Int): DeviceType {
            values().forEach { value ->
                if (value.code == code) {
                    return value
                }
            }
            return DEVICE_TYPE_UNKNOWN
        }
    }

}