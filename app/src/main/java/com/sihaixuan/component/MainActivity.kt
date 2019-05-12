package com.sihaixuan.component


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.sihaixuan.component.modulea.RouterConstants as ModuleA

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ARouter.getInstance().build(ModuleA.AACITIVTY_PATH).navigation()
        finish()
    }
}
