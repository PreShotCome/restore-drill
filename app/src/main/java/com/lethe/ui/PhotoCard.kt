package com.lethe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lethe.data.Photo
import com.lethe.ui.theme.LetheDiscard
import com.lethe.ui.theme.LetheKeep
import com.lethe.ui.theme.LetheMidnightSoft
import kotlin.math.abs
import kotlin.math.min

@Composable
fun PhotoCard(
    photo: Photo,
    swipeOffset: Float = 0f,
    swipeThreshold: Float = 1f,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(LetheMidnightSoft),
    ) {
        AsyncImage(
            model = photo.uri,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )

        val progress = min(abs(swipeOffset) / swipeThreshold, 1f)
        if (progress > 0.02f) {
            val tint = if (swipeOffset < 0) LetheDiscard else LetheKeep
            Box(
                Modifier
                    .fillMaxSize()
                    .background(tint.copy(alpha = 0.35f * progress)),
            )
            val label = if (swipeOffset < 0) "DELETE" else "KEEP"
            val align = if (swipeOffset < 0) Alignment.TopEnd else Alignment.TopStart
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = align) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .background(tint.copy(alpha = 0.85f * progress), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}
