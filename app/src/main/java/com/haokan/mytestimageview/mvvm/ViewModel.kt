package com.haokan.mytestimageview.mvvm

import android.widget.EditText
import com.haokan.mytestimageview.mvp.DataCenter

class ViewModel(val data1View: EditText, val data2View: EditText) {
    var data0: StringAttr = StringAttr()
    var data1: StringAttr = StringAttr()

    init {
        ViewBinder.bind(data1View,data0)
        ViewBinder.bind(data2View,data0)
    }

    //整体调度
    //数据的双向绑定
    fun init() {
        val data = DataCenter.getData()
        data0.value = data[0]
        data1.value = data[1]

//        bind(edit,data)
    }


}