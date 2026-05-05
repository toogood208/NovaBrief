package com.example.shared.feature.news.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.jetbrains.skia.Image as SkiaImage

@Composable
actual fun PlatformArticleImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier
) {
    val loadState by produceState<ImageLoadState>(initialValue = ImageLoadState.Loading, key1 = imageUrl) {
        value = if (imageUrl.isBlank()) {
            ImageLoadState.Failure
        } else {
            runCatching {
                val response = window.fetch(imageUrl).await()
                if (!response.ok) error("Image request failed (${response.status.toInt()})")

                val buffer = response.arrayBuffer().await()
                val uint8 = Uint8Array(buffer)
                val bytes = ByteArray(uint8.length)
                for (index in 0 until uint8.length) {
                    bytes[index] = (uint8.asDynamic()[index] as Int).toByte()
                }
                SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
            }.fold(
                onSuccess = { ImageLoadState.Success(it) },
                onFailure = { ImageLoadState.Failure }
            )
        }
    }

    Box(modifier = modifier) {
        when (val state = loadState) {
            is ImageLoadState.Success -> {
                Image(
                    bitmap = state.bitmap,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            ImageLoadState.Failure -> {
                Text(
                    text = "Image unavailable",
                    color = Color(0xFFFFFFFF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            ImageLoadState.Loading -> Unit
        }
    }
}

private sealed interface ImageLoadState {
    data object Loading : ImageLoadState
    data object Failure : ImageLoadState
    data class Success(val bitmap: ImageBitmap) : ImageLoadState
}

