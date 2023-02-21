package com.asiman.badgedimageviewexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.asiman.badgedimageview.BadgedImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<BadgedImageView>(R.id.image_with_number).setBadgeCount(3)
        findViewById<BadgedImageView>(R.id.image).setBadgeCount(3)
    }
}