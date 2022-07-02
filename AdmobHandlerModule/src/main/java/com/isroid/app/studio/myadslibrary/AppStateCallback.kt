package com.isroid.app.studio.myadslibrary

interface AppStateCallback {
    fun onAppStart(){}
    fun onAppPause(){}
    fun onAppResume(){}
    fun onAppStop(){}
    fun onAppDestroy(){}
    fun showLog(msg:String){}
}