package com.haokan.mytestimageview.mvvm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.haokan.mytestimageview.R
import com.haokan.mytestimageview.databinding.ActivityMvvmDemoLayoutBinding

class MvvmDemoActivity : AppCompatActivity() {
    //声明dataBinding
    var binding: ActivityMvvmDemoLayoutBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_mvvm_demo_layout)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_mvvm_demo_layout)
        binding?.first="databinding 设置"
    }
}