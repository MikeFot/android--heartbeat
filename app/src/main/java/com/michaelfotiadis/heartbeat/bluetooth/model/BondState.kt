package com.michaelfotiadis.heartbeat.bluetooth.model

enum class BondState(val code: Int) {
    BOND_NONE(10),
    BOND_BONDING(11),
    BOND_BONDED(12);

    companion object {
        fun fromCode(code: Int): BondState {
            values().forEach { value ->
                if (value.code == code) {
                    return value
                }
            }
            return BOND_NONE
        }
    }
}