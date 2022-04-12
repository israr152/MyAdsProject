package com.isroid.app.studio.myadsproject

import android.view.View

typealias ids = R.id

fun View.gone(){
    visibility = View.GONE
}

fun View.show(){
    visibility = View.VISIBLE
}