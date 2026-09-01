/**
 * Musie player backgrounds derived from the GPL-3.0 Echo Music implementation.
 */
package com.metrolist.music.ui.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

@Composable
internal fun AnimatedGlowBackground(
    colors: List<Color>,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val palette =
        if (colors.isEmpty()) {
            listOf(Color(0xFF5B2C83), Color(0xFF123F66), Color(0xFF8A3D62))
        } else {
            List(6) { colors[it % colors.size] }
        }
    val transition = rememberInfiniteTransition(label = "glowBackground")
    val progress by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(20_000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "glowProgress",
        )

    fun animatedColor(index: Int): Color {
        val position = index + progress * palette.size
        val first = floor(position).toInt() % palette.size
        val second = (first + 1) % palette.size
        return lerp(palette[first], palette[second], position - floor(position))
    }

    fun oscillate(
        minimum: Float,
        maximum: Float,
        phase: Float,
    ): Float {
        val wave = sin(2f * PI.toFloat() * (progress + phase))
        return minimum + (maximum - minimum) * ((wave + 1f) / 2f)
    }

    val glowColors = List(6) { animatedColor(it) }
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .alpha(alpha)
                .drawWithCache {
                    val width = size.width
                    val height = size.height
                    val brushes =
                        glowColors.mapIndexed { index, color ->
                            val phase = index * 0.13f
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        color.copy(alpha = 0.82f - index * 0.05f),
                                        color.copy(alpha = 0.38f),
                                        Color.Transparent,
                                    ),
                                center =
                                    androidx.compose.ui.geometry.Offset(
                                        width * oscillate(0.05f, 0.95f, phase),
                                        height * oscillate(0.08f, 0.92f, phase + 0.19f),
                                    ),
                                radius = width * oscillate(0.75f, 1.55f, phase + 0.07f),
                            )
                        }
                    onDrawBehind {
                        drawRect(Color(0xFF050505))
                        brushes.forEach { brush -> drawRect(brush = brush) }
                    }
                },
    )
}

@Composable
internal fun AppleMusicBackground(
    thumbnailUrl: String?,
    alpha: Float,
    showClearArtwork: Boolean,
    modifier: Modifier = Modifier,
) {
    if (thumbnailUrl == null) return
    val clearArtworkAlpha by
        animateFloatAsState(
            targetValue = if (showClearArtwork) 1f else 0f,
            animationSpec = tween(500),
            label = "appleMusicClearArtworkAlpha",
        )

    Box(modifier = modifier.fillMaxSize().alpha(alpha)) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(145.dp),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .alpha(clearArtworkAlpha)
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush =
                                Brush.verticalGradient(
                                    colorStops =
                                        arrayOf(
                                            0f to Color.Black,
                                            0.75f to Color.Black,
                                            0.92f to Color.Black.copy(alpha = 0.4f),
                                            1f to Color.Transparent,
                                        ),
                                ),
                            blendMode = BlendMode.DstIn,
                        )
                    },
        ) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops =
                            arrayOf(
                                0f to Color.Black.copy(alpha = 0.05f),
                                1f to Color.Black.copy(alpha = 0.4f),
                            ),
                    ),
                ),
        )
    }
}

@Composable
internal fun LiveMeshBackground(
    thumbnailUrl: String?,
    alpha: Float,
    liquidGlass: Boolean,
    modifier: Modifier = Modifier,
) {
    if (thumbnailUrl == null) return
    val transition = rememberInfiniteTransition(label = "liveMeshBackground")
    val anchorRotation by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = -360f,
            animationSpec = infiniteRepeatable(tween(80_000, easing = LinearEasing)),
            label = "meshAnchor",
        )
    val fastRotation by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(40_000, easing = LinearEasing)),
            label = "meshFast",
        )
    val slowRotation by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(60_000, easing = LinearEasing)),
            label = "meshSlow",
        )

    Box(modifier = modifier.fillMaxSize().alpha(alpha).background(Color.Black)) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.7f
                    scaleY = 1.7f
                },
        ) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(100.dp).graphicsLayer { rotationZ = anchorRotation },
            )
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopStart,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .blur(120.dp)
                        .graphicsLayer {
                            rotationZ = fastRotation
                            this.alpha = 0.6f
                        },
            )
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomEnd,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .blur(120.dp)
                        .graphicsLayer {
                            rotationZ = slowRotation
                            this.alpha = 0.5f
                        },
            )
        }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = if (liquidGlass) 0.08f else 0.2f)))
        if (liquidGlass) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.16f),
                                Color.White.copy(alpha = 0.035f),
                                Color.Black.copy(alpha = 0.18f),
                            ),
                        ),
                    )
                    .drawWithCache {
                        val highlight =
                            Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.25f, 0f),
                                radius = size.width * 0.9f,
                            )
                        onDrawBehind { drawRect(highlight) }
                    },
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f)))),
            )
        }
    }
}
