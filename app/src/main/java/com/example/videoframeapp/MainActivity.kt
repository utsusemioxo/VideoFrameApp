package com.example.videoframeapp
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RecordActivity::class.java
                )
            )
        }

        findViewById<Button>(R.id.btnProcess).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ProcessActivity::class.java
                )
            )
        }

    }

}