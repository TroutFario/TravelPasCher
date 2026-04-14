package com.example.travelpascher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.example.travelpascher.ui.components.Post

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val feedView = findViewById<ComposeView>(R.id.feedView)

        feedView.setContent {
            FeedScreen()
        }
    }
}

@Composable
fun FeedScreen() {
    LazyColumn {
        item {
            Post(
                username = "John",
                location = "Paris",
                time = "2h",
                imageRes = R.drawable.chevoul
            )
        }

        item {
            Post(
                username = "Alice",
                location = "Lyon",
                time = "5h",
                imageRes = R.drawable.chevoul
            )
        }
    }
}