package com.sihaixuan.component.moduleb


import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route
import org.greenrobot.eventbus.EventBus

@Route(path =RouterConstants.BACITIVTY_PATH)
class BActivity : AppCompatActivity() {

                    override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_b)
//        EventBus.builder().addIndex(MyEventBusIndex()).installDefaultEventBus()
                findViewById<Button>(R.id.sendEventBtn).setOnClickListener {
                    EventBus.getDefault().post(BEvent("send message by module b"))
        }

    }
}
