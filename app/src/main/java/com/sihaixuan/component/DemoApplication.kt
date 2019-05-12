package com.sihaixuan.component

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter

/**
 *
 * 项目名称：ComponentDemo
 * 类描述：
 * 创建人：toney
 * 创建时间：2019/5/9 15:54
 * 邮箱：xiyangfeisa@foxmail.com
 * 备注：
 * @version   1.0
 *
 */
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if(BuildConfig.DEBUG){
            ARouter.openLog()    // Print log

        }
        ARouter.openDebug()
        ARouter.init(this)
    }
}