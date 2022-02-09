package com.haokan.mytestimageview.mvvm

class StringAttr {
    var value: String? = null
        set(value) {
            field = value
            onChangeLisenter?.onChange(value)
        }

    var onChangeLisenter: onChangeLisener? = null

    interface onChangeLisener {
        fun onChange(newValue: String?)
    }
}