package com.haokan.mytestimageview.mvvm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.haokan.mytestimageview.R
import com.haokan.mytestimageview.mvp.DataCenter
import kotlinx.android.synthetic.main.activity_mvp.*

class MvvmActivity :AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mvp);

        ViewModel(data1_view,data2_view).init()
        val data = DataCenter.getData()

    }

}