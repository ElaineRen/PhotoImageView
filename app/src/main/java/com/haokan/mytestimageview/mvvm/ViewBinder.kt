package com.haokan.mytestimageview.mvvm

import android.text.TextUtils
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged

class ViewBinder {
    companion object {
        fun bind(editText: EditText, string: StringAttr?) {
            editText.doAfterTextChanged {
                if (!TextUtils.equals(string?.value, it)) {
                    string?.value = it.toString()
                    println("表现数据通知内存了")
                }
            }
            string?.onChangeLisenter = object : StringAttr.onChangeLisener {
                override fun onChange(newValue: String?) {
                    if (!TextUtils.equals(newValue, editText.text)) {
                        editText.setText(newValue)
                        println("内存数据通知表现数据了")
                    }
                }
            }
        }
    }
}