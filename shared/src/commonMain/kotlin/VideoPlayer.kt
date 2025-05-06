package com.example.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
expect fun VideoPlayer(
    videoFileName: String,
    modifier: Modifier,
    onVideoFinished: () -> Unit
)