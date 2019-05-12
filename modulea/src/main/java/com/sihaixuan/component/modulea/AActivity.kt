package com.sihaixuan.component.modulea



import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.sihaixuan.base.util.bindView
import com.sihaixuan.component.moduleb.BEvent
import  com.sihaixuan.component.moduleb.RouterConstants as ModuleB
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@Route(path = RouterConstants.AACITIVTY_PATH)
class AActivity : AppCompatActivity() {

    private val messageTxt by bindView<TextView>(R.id.text1)
    private val gotoBtn by bindView<Button>(R.id.gotoOtherModulePageBtn)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a)

//        messageTxt.setOnClickListener {
//            EventBus.getDefault().post(AEvent("this is a message from moduleA!"))
//        }


        gotoBtn.setOnClickListener {
            ARouter.getInstance().build(ModuleB.BACITIVTY_PATH).navigation()
        }

        EventBus.getDefault().register(this)

    }

    @Subscribe
    fun onEvent(event : BEvent){
        messageTxt.text = event.message
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}




