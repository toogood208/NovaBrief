package com.example.shared.feature.news.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage
import java.net.HttpURLConnection
import java.net.URI

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
                withContext(Dispatchers.IO) {
                    val connection = URI.create(imageUrl).toURL().openConnection() as HttpURLConnection
                    connection.instanceFollowRedirects = true
                    connection.connectTimeout = 8_000
                    connection.readTimeout = 12_000
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 NovaBriefDesktop/1.0")
                    connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")

                    val bytes = connection.inputStream.use { it.readBytes() }
                    SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
                }
            }.onFailure { throwable ->
                println("[DesktopImage] failed to load image: $imageUrl | ${throwable::class.simpleName}: ${throwable.message}")
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
                    fontSize = 13.sp,
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

