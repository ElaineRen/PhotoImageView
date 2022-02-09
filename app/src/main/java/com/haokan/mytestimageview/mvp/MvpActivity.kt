package com.haokan.mytestimageview.mvp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.haokan.mytestimageview.R
import kotlinx.android.synthetic.main.activity_mvp.*

class MvpActivity :AppCompatActivity(), Presenter.IView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mvp);
        Presenter(this).init()
    }

    override fun showData(data: List<String>) {
        data1_view.setText(data[0])
        data2_view.setText(data[1])
    }

}