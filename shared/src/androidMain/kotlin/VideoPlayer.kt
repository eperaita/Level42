// androidMain/kotlin/com/example/shared/VideoPlayer.android.kt
package com.example.shared

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import java.io.File
import com.example.intrapp.generated.resources.Res


@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    videoFileName: String,
    modifier: Modifier,
    onVideoFinished: () -> Unit
) {
    val context = LocalContext.current

    // Acceder directamente al archivo en la raíz de assets (sin subcarpeta)
    val videoUri = remember(videoFileName) {
        try {
            // Abrir el archivo desde assets
            val inputStream = context.assets.open(videoFileName)

            // Crear archivo temporal
            val tempFile = File.createTempFile("video", ".mp4", context.cacheDir)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            // Crear URI para el archivo temporal
            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            Log.e("VIDEO", "Error cargando video: ${e.message}")
            null
        }
    }

    // Solo mostrar el reproductor si se pudo cargar el video
    if (videoUri != null) {
        // ExoPlayer para reproducir el video
        val exoPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(videoUri)
                setMediaItem(mediaItem)
                repeatMode = ExoPlayer.REPEAT_MODE_OFF
                prepare()
                play()
            }
        }

        // Detector de fin de video
        LaunchedEffect(exoPlayer) {
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onVideoFinished()
                    }
                }
            })
        }

        // Liberar recursos
        DisposableEffect(Unit) {
            onDispose {
                exoPlayer.release()
            }
        }

        // Reproducir el video
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false

                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = modifier.fillMaxSize().offset(x = 0.dp, y = 0.dp)

        )
    } else {
        Log.e("VIDEO", "Error despues de cargar el video")
    }
}