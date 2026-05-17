package fr.troubidoo.travelpascher.ui.components

import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import fr.troubidoo.travelpascher.R

@Composable
fun Post(
    username: String,
    location: String,
    time: Long,
    imageUrl: String,
    authorProfileImageUrl: String = "",
    isLiked: Boolean = false,
    likeCount: Int = 0,
    latitude: Double? = null,
    longitude: Double? = null,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val context = LocalContext.current
    ProvideTextStyle(
        value = TextStyle(
            color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column {

                // ---------------- HEADER ----------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (authorProfileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = authorProfileImageUrl,
                            contentDescription = stringResource(R.string.profile_picture),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.outline_account_circle_24),
                            contentDescription = stringResource(R.string.profile_picture),
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {

                        Text(
                            text = username, fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = location,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    val uri = if (latitude != null && longitude != null) {
                                        "geo:$latitude,$longitude?q=$latitude,$longitude($location)".toUri()
                                    } else {
                                        "geo:0,0?q=$location".toUri()
                                    }
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    intent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(intent)
                                })

                            Text(
                                text = stringResource(R.string.middle_point),
                            )

                            Text(
                                text = DateUtils.getRelativeTimeSpanString(
                                    time,
                                    System.currentTimeMillis(),
                                    DateUtils.MINUTE_IN_MILLIS)
                                    .toString(),
                            )
                        }
                    }
                }

                // ---------------- IMAGE ----------------
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = stringResource(R.string.post_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ImageNotSupported,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                )

                // ---------------- ACTION BAR ----------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onLikeClick) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = stringResource(R.string.like),
                                tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (likeCount > 0) {
                            Text(
                                text = likeCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onCommentClick) {
                        Icon(
                            painter = painterResource(R.drawable.outline_chat_24),
                            contentDescription = stringResource(R.string.comment)
                        )
                    }

                    IconButton(onClick = onShareClick) {
                        Icon(
                            painter = painterResource(R.drawable.outline_share_24),
                            contentDescription = stringResource(R.string.share)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = onSaveClick) {
                        Icon(
                            painter = painterResource(R.drawable.outline_bookmark_24),
                            contentDescription = stringResource(R.string.save)
                        )
                    }
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
        time = 1758665200000,
        imageUrl = "https://example.com/image.jpg"
    )
}
