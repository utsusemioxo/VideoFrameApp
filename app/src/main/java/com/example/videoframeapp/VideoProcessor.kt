package com.example.videoframeapp

import android.R

object VideoProcessor {
    init { System.loadLibrary("video_frame_processor")}

    external fun processVideo(path: String)

    fun processVideoSafe(path: String) {
        try {
            processVideo(path)
        } catch (e: UnsatisfiedLinkError) {
            println("native processVideo 未实现, 路径: $path")
        }
    }
}