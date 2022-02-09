package com.haokan.mytestimageview.mvp

class Presenter(private val view: IView) {
    fun init() {
        val data = DataCenter.getData()
        view.showData(data)
    }

    interface IView {
        fun showData(data: List<String>)
    }
}


