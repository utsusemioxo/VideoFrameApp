package com.example.videoframeapp

import android.R

object VideoProcessor {
    init { System.loadLibrary("native-lib")}
    external fun processVideo(path: String)
}