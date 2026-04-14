package com.example.travelpascher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelpascher.R

@Composable
fun Post(
    username: String,
    location: String,
    time: String,
    imageRes: Int,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Column {

            // ---------------- HEADER ----------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    painter = painterResource(R.drawable.outline_account_circle_24),
                    contentDescription = "profile picture",
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {

                    Text(
                        text = username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = location,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = " • ",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = time,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // ---------------- IMAGE ----------------
            Image(
                painter = painterResource(imageRes),
                contentDescription = "post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            // ---------------- ACTION BAR ----------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onLikeClick) {
                    Icon(
                        painter = painterResource(R.drawable.outline_favorite_24),
                        contentDescription = "like"
                    )
                }

                IconButton(onClick = onCommentClick) {
                    Icon(
                        painter = painterResource(R.drawable.outline_chat_24),
                        contentDescription = "comment"
                    )
                }

                IconButton(onClick = onShareClick) {
                    Icon(
                        painter = painterResource(R.drawable.outline_share_24),
                        contentDescription = "share"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onSaveClick) {
                    Icon(
                        painter = painterResource(R.drawable.outline_bookmark_24),
                        contentDescription = "save"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostPreview() {
    Post(
        username = "John Doe",
        location = "Paris",
        time = "2h",
        imageRes = R.drawable.chevoul
    )
}