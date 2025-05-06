@file:OptIn(ExperimentalForeignApi::class)

package com.example.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.currentItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.Foundation.NSBundle
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.UIKit.UIView

@Composable
actual fun VideoPlayer(
    videoFileName: String,
    modifier: Modifier,
    onVideoFinished: () -> Unit
) {
    val videoName = videoFileName.removeSuffix(".mp4")
    val videoPath = NSBundle.mainBundle.pathForResource(
        videoName,
        "mp4",
        "videos" // Busca en subdirectorio videos
    )

    if (videoPath != null) {
        val videoUrl = NSURL.fileURLWithPath(videoPath)
        val controller = LocalUIViewController.current

        DisposableEffect(Unit) {
            val player = AVPlayer(uRL = videoUrl)
            val playerView = UIView().apply {
                val playerLayer = AVPlayerLayer().apply {
                    frame = bounds
                    videoGravity = AVLayerVideoGravityResizeAspectFill
                    this.player = player
                }
                layer.addSublayer(playerLayer)
            }

            controller.view.addSubview(playerView)

            val observer = NSNotificationCenter.defaultCenter.addObserverForName(
                name = AVPlayerItemDidPlayToEndTimeNotification,
                `object` = player.currentItem,
                queue = null
            ) { _: NSNotification? ->
                onVideoFinished()
            }

            player.play()

            onDispose {
                player.pause()
                NSNotificationCenter.defaultCenter.removeObserver(observer)
                playerView.removeFromSuperview()
            }
        }
    }
}