package com.eleonorez.cunny.ui.compose.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import com.adamglin.phosphoricons.regular.WarningCircle
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Info
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.ArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import android.graphics.BlurMaskFilter
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.R
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import androidx.compose.ui.platform.LocalContext
import com.eleonorez.cunny.helper.SoundSynthesizer

@Composable
fun CunnyHeaderBar(
    title: String,
    onBack: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            GlassSurface(
                shape = CircleShape,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onBack)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = CunnyColors.textDark,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.Center)
                )
            }
        } else {
            Spacer(Modifier.size(40.dp))
        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = CunnyColors.textDark,
            textAlign = TextAlign.Center
        )

        if (trailing != null) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                trailing()
            }
        } else {
            Spacer(Modifier.size(40.dp))
        }
    }
}

@Composable
fun StreakEnergyBar(
    streak: Int,
    xp: Int,
    energy: Int = 3,
    lastRefillTime: Long = 0L,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(energy, lastRefillTime) {
        if (energy < 5 && lastRefillTime > 0L) {
            while (true) {
                currentTime = System.currentTimeMillis()
                kotlinx.coroutines.delay(1000L)
            }
        }
    }

    val timerText = if (energy < 5 && lastRefillTime > 0L) {
        val elapsedMs = currentTime - lastRefillTime
        val intervalMs = 30 * 60 * 1000L
        val nextRefillIn = maxOf(0L, intervalMs - (elapsedMs % intervalMs))
        val minutes = (nextRefillIn / 1000) / 60
        val seconds = (nextRefillIn / 1000) % 60
        val minutesStr = minutes.toString().padStart(2, '0')
        val secondsStr = seconds.toString().padStart(2, '0')
        " ($minutesStr:$secondsStr)"
    } else {
        ""
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        val shape = RoundedCornerShape(CunnyDimens.radiusFull)
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(bottom = 3.dp)
                .background(
                    color = CunnyColors.tactileShadow, // Theme-aware 3D base shadow
                    shape = shape
                )
        ) {
            GlassSurface(
                shape = shape,
                modifier = Modifier
                    .wrapContentSize()
                    .offset(y = (-3).dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Streak / Keys
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("🔑", fontSize = 14.sp)
                        Text(
                            text = "$streak",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CunnyColors.textDark
                        )
                    }

                    // Separator 1
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(1.dp)
                            .height(14.dp)
                            .background(CunnyColors.border)
                    )

                    // XP
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("⭐", fontSize = 14.sp)
                        Text(
                            text = "$xp XP",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CunnyColors.textDark
                        )
                    }

                    // Separator 2
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(1.dp)
                            .height(14.dp)
                            .background(CunnyColors.border)
                    )

                    // Energy
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("⚡", fontSize = 14.sp)
                        Text(
                            text = "$energy$timerText",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CunnyColors.textDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CunnyFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val offsetY by animateDpAsState(
        targetValue = if (isFocused) 4.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fieldOffset"
    )

    val shape = RoundedCornerShape(CunnyDimens.radiusSm)
    val shadowColor = CunnyColors.tactileShadow // Theme-aware 3D base shadow

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = DmSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = CunnyColors.textDark
        )
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp), // 56.dp field height + 4.dp shadow
            contentAlignment = Alignment.TopCenter
        ) {
            // Shadow base layer
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(color = shadowColor, shape = shape)
            )

            // BasicTextField with floating glass surface layer
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                interactionSource = interactionSource,
                textStyle = TextStyle(
                    fontFamily = DmSansFontFamily,
                    fontSize = 15.sp,
                    color = CunnyColors.textDark
                ),
                keyboardOptions = keyboardOptions,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .offset(y = offsetY),
                decorationBox = { inner ->
                    GlassSurface(
                        shape = shape,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 15.sp,
                                    color = CunnyColors.textSubtle
                                )
                            }
                            inner()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun Cunny3DIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Icon Button",
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    tint: Color = CunnyColors.textDark
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "iconButtonOffset"
    )

    val shape = CircleShape
    val shadowColor = CunnyColors.tactileShadow // Theme-aware 3D base shadow

    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(width = 40.dp, height = 44.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    SoundSynthesizer.play(context, SoundSynthesizer.SoundType.TAP)
                    onClick()
                }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(40.dp)
                .background(color = shadowColor, shape = shape)
        )

        GlassSurface(
            shape = shape,
            modifier = Modifier
                .size(40.dp)
                .offset(y = offsetY)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun CunnyBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Cunny3DIconButton(
        icon = PhosphorIcons.Regular.ArrowLeft,
        onClick = onClick,
        modifier = modifier,
        contentDescription = "Back",
        iconSize = 20.dp
    )
}

@Composable
fun CunnyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    textColor: Color? = null,
    backgroundColor: Color = CunnyColors.primary,
    containerColor: Color? = null,
    shadowColor: Color = CunnyColors.primaryShadow,
    brush: Brush? = null,
    pressedBrush: Brush? = null
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Tactile 3D press vertical offset animation
    val offsetY by animateDpAsState(
        targetValue = if ((isPressed || isLoading) && enabled) 0.dp else (-4).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "buttonOffset"
    )

    val bgBrush = if (enabled) {
        if ((isPressed || isLoading) && pressedBrush != null) {
            pressedBrush
        } else {
            brush ?: containerColor?.let { Brush.linearGradient(listOf(it, it)) } ?: Brush.linearGradient(CunnyColors.gradPlum)
        }
    } else {
        Brush.linearGradient(listOf(CunnyColors.borderLight, CunnyColors.borderLight))
    }

    val shadowColorToUse = if (enabled) {
        if (containerColor != null && shadowColor == CunnyColors.primaryShadow) {
            Color(0xFF991B1B) // standard dark red shadow for red button
        } else {
            shadowColor
        }
    } else {
        Color.Transparent
    }

    val shape = RoundedCornerShape(CunnyDimens.radiusFull)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp) // Leave space for bottom 3D shadow
            .let {
                if (enabled) {
                    it.background(
                        color = shadowColorToUse,
                        shape = shape
                    )
                } else it
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = {
                    SoundSynthesizer.play(context, SoundSynthesizer.SoundType.TAP)
                    onClick()
                }
            )
    ) {
        // Main button surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .offset(y = offsetY)
                .clip(shape)
                .background(bgBrush)
                .border(
                    width = 1.dp,
                    color = if (enabled) Color.White.copy(alpha = 0.14f) else Color.Transparent,
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (enabled) (textColor ?: CunnyColors.textOnPrimary) else CunnyColors.textSubtle,
                textAlign = TextAlign.Center
            )

            if (isLoading && enabled) {
                val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
                val translateAnim = infiniteTransition.animateFloat(
                    initialValue = -300f,
                    targetValue = 600f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1400, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "shimmerTranslate"
                )
                
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.28f),
                                    Color.Transparent
                                ),
                                start = Offset(translateAnim.value, 0f),
                                end = Offset(translateAnim.value + 150f, 150f)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun CunnyDarkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Tactile 3D press vertical offset animation
    val offsetY by animateDpAsState(
        targetValue = if ((isPressed || isLoading) && enabled) 0.dp else (-4).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "buttonOffset"
    )

    val shape = RoundedCornerShape(CunnyDimens.radiusFull)

    val shadowColor = if (enabled) CunnyColors.primaryShadow else Color.Transparent
    val bgBrush = if (enabled) {
        Brush.linearGradient(CunnyColors.gradHeroDark)
    } else {
        Brush.linearGradient(listOf(CunnyColors.borderLight, CunnyColors.borderLight))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp) // Leave space for bottom 3D shadow
            .let {
                if (enabled) {
                    it.background(
                        color = shadowColor,
                        shape = shape
                    )
                } else it
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = {
                    SoundSynthesizer.play(context, SoundSynthesizer.SoundType.TAP)
                    onClick()
                }
            )
    ) {
        // Main button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .offset(y = offsetY)
                .clip(shape)
                .background(bgBrush)
                .border(
                    width = 1.dp,
                    color = if (enabled) Color(251, 249, 253, 25) else Color.Transparent,
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (enabled) Color.White else CunnyColors.textSubtle,
                textAlign = TextAlign.Center
            )

            if (isLoading && enabled) {
                val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
                val translateAnim = infiniteTransition.animateFloat(
                    initialValue = -300f,
                    targetValue = 600f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1400, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "shimmerTranslate"
                )
                
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.28f),
                                    Color.Transparent
                                ),
                                start = Offset(translateAnim.value, 0f),
                                end = Offset(translateAnim.value + 150f, 150f)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun CunnyOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Tactile 3D press vertical offset animation
    val offsetY by animateDpAsState(
        targetValue = if ((isPressed || isLoading) && enabled) 0.dp else (-4).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "buttonOffset"
    )

    val shape = RoundedCornerShape(CunnyDimens.radiusFull)
    val shadowColor = if (enabled) CunnyColors.tactileShadow else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .background(
                color = shadowColor, // Solid 3D warm plum base shadow
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = {
                    SoundSynthesizer.play(context, SoundSynthesizer.SoundType.TAP)
                    onClick()
                }
            )
    ) {
        GlassSurface(
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (enabled) CunnyColors.textDark else CunnyColors.textSubtle,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (enabled) CunnyColors.textDark else CunnyColors.textSubtle,
                        textAlign = TextAlign.Center
                    )
                }

                if (isLoading && enabled) {
                    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
                    val translateAnim = infiniteTransition.animateFloat(
                        initialValue = -300f,
                        targetValue = 600f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1400, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "shimmerTranslate"
                    )
                    
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.4f),
                                        Color.Transparent
                                    ),
                                    start = Offset(translateAnim.value, 0f),
                                    end = Offset(translateAnim.value + 150f, 150f)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun CunnyGoogleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Tactile 3D press vertical offset animation
    val offsetY by animateDpAsState(
        targetValue = if ((isPressed || isLoading) && enabled) 0.dp else (-4).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "buttonOffset"
    )

    val shape = RoundedCornerShape(CunnyDimens.radiusFull)
    val shadowColor = if (enabled) CunnyColors.tactileShadow else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .background(
                color = shadowColor, // Solid 3D warm plum base shadow
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = {
                    SoundSynthesizer.play(context, SoundSynthesizer.SoundType.TAP)
                    onClick()
                }
            )
    ) {
        GlassSurface(
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = text,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (enabled) CunnyColors.textDark else CunnyColors.textSubtle
                    )
                }

                if (isLoading && enabled) {
                    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
                    val translateAnim = infiniteTransition.animateFloat(
                        initialValue = -300f,
                        targetValue = 600f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1400, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "shimmerTranslate"
                    )
                    
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.08f),
                                        Color.Transparent
                                    ),
                                    start = Offset(translateAnim.value, 0f),
                                    end = Offset(translateAnim.value + 150f, 150f)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(CunnyColors.border)
        )
        Text(
            text = "or",
            fontFamily = DmSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = CunnyColors.textSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(CunnyColors.border)
        )
    }
}

@Composable
fun BorderedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Tactile 3D press vertical offset animation
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else (-4).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardOffset"
    )

    val shape = RoundedCornerShape(CunnyDimens.radiusLg)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp) // Leave space for bottom 3D shadow
            .background(
                color = CunnyColors.tactileShadow, // Theme-aware 3D base shadow
                shape = shape
            )
    ) {
        GlassSurface(
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
fun CunnyProgressTrack(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(CunnyColors.borderLight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.horizontalGradient(CunnyColors.gradProgress))
        )
    }
}

@Composable
fun CunnySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(CunnyDimens.radiusFull)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = DmSansFontFamily,
            fontSize = 15.sp,
            color = CunnyColors.textDark
        ),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .background(
                        color = CunnyColors.tactileShadow, // Theme-aware 3D base shadow
                        shape = shape
                    )
            ) {
                GlassSurface(
                    shape = shape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-4).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.MagnifyingGlass,
                            contentDescription = "Search",
                            tint = CunnyColors.textSubtle,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 15.sp,
                                    color = CunnyColors.textSubtle
                                )
                            }
                            inner()
                        }
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun BannerCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1.0f, label = "bannerScale")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(CunnyDimens.radiusLg))
            .background(Brush.linearGradient(CunnyColors.gradPlum))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = title,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontFamily = DmSansFontFamily,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun CourseListRow(
    title: String,
    subtitle: String,
    progress: Float,
    emoji: String,
    emojiBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    drawableResId: Int? = null
) {
    BorderedCard(onClick = onClick, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            if (drawableResId != null) {
                Image(
                    painter = painterResource(id = drawableResId),
                    contentDescription = title,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.width(12.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(emojiBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 24.sp)
                }
                Spacer(Modifier.width(16.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CunnyColors.textDark
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = CunnyColors.textSubtle
                )
                Spacer(Modifier.height(8.dp))
                CunnyProgressTrack(progress)
            }
        }
    }
}

@Composable
fun BookmarkListRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = CunnyColors.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CunnyColors.primaryPale),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CunnyColors.textDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = DmSansFontFamily,
                fontSize = 12.sp,
                color = CunnyColors.textSubtle
            )
        }
    }
}

@Composable
fun SettingsRow(
    label: String,
    onClick: (() -> Unit)? = null,
    labelColor: Color = CunnyColors.textDark,
    icon: @Composable () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            icon()
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontFamily = DmSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = labelColor
        )
        if (trailing != null) trailing()
    }
}

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CunnyColors.borderLight)
    )
}

@Composable
fun SettingsSectionSpacer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(CunnyColors.backgroundSoft)
    )
}

@Composable
fun CunnyToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (checked) CunnyColors.primary else CunnyColors.border)
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer(translationX = if (checked) 20.dp.value * 2.5f else 0f) // Simplified offset
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun ThemeSelector(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val themes = listOf("auto" to "⚙️", "light" to "☀️", "dark" to "🌙")
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(CunnyDimens.radiusFull))
            .background(CunnyColors.backgroundSoft)
            .padding(3.dp)
    ) {
        themes.forEach { (value, emoji) ->
            val isActive = selectedTheme == value
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(CunnyDimens.radiusFull))
                    .background(if (isActive) CunnyColors.background else Color.Transparent)
                    .clickable { onThemeSelected(value) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun HeroPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(CunnyColors.borderLight)
    )
}

fun Modifier.buttonShadow(
    color: Color = Color(108, 92, 231).copy(alpha = 0.24f),
    borderRadius: Dp = 20.dp,
    blurRadius: Dp = 10.dp,
    offsetY: Dp = 6.dp
) = this.drawBehind {
    val shadowColor = color.toArgb()
    val blurRadiusPx = blurRadius.toPx()
    val borderRadiusPx = borderRadius.toPx()
    val offsetYPx = offsetY.toPx()

    drawIntoCanvas { canvas ->
        val paint = androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
            this.color = shadowColor
            if (blurRadiusPx > 0f) {
                maskFilter = BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
            }
        }
        
        canvas.nativeCanvas.drawRoundRect(
            0f,
            offsetYPx,
            size.width,
            size.height + offsetYPx,
            borderRadiusPx,
            borderRadiusPx,
            paint
        )
    }
}

fun Modifier.coloredShadow(
    color: Color = Color(0xFF6C5CE7),
    alpha: Float = 0.24f,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 22.dp,
    offsetY: Dp = 6.dp,
    offsetX: Dp = 0.dp
) = this.drawBehind {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    // Use a very tiny alpha (0.01f) so the GPU doesn't optimize out the draw call, while keeping the fill invisible.
    val nonTransparentColor = Color.Black.copy(alpha = 0.01f).toArgb()
    
    this.drawIntoCanvas { canvas ->
        val paint = androidx.compose.ui.graphics.Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = nonTransparentColor
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint = paint
        )
    }
}

enum class CunnyToastType {
    SUCCESS, ERROR, INFO
}

object CunnyToast {
    var message by mutableStateOf<String?>(null)
        private set
    var type by mutableStateOf(CunnyToastType.INFO)
        private set

    fun show(msg: String, toastType: CunnyToastType = CunnyToastType.INFO) {
        message = msg
        type = toastType
    }

    fun dismiss() {
        message = null
    }
}

@Composable
fun CunnyToastOverlay(
    modifier: Modifier = Modifier
) {
    val message = CunnyToast.message
    val type = CunnyToast.type

    // Auto dismiss after 3 seconds
    LaunchedEffect(message) {
        if (message != null) {
            delay(3000)
            CunnyToast.dismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(animationSpec = tween(250)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        if (message != null) {
            GlassSurface(
                shape = RoundedCornerShape(CunnyDimens.radiusFull),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Micro-island for icon
                    val iconBg = when (type) {
                        CunnyToastType.ERROR -> Color(0xFFB42318).copy(alpha = 0.08f)
                        CunnyToastType.SUCCESS -> Color(0xFF4A6B58).copy(alpha = 0.08f)
                        CunnyToastType.INFO -> CunnyColors.primary.copy(alpha = 0.08f)
                    }
                    val iconColor = when (type) {
                        CunnyToastType.ERROR -> Color(0xFFB42318)
                        CunnyToastType.SUCCESS -> Color(0xFF4A6B58)
                        CunnyToastType.INFO -> CunnyColors.primary
                    }
                    val iconVector = when (type) {
                        CunnyToastType.ERROR -> PhosphorIcons.Regular.WarningCircle
                        CunnyToastType.SUCCESS -> PhosphorIcons.Regular.CheckCircle
                        CunnyToastType.INFO -> PhosphorIcons.Regular.Info
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(iconBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = type.name,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = message,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = CunnyColors.textDark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun CunnyLessonSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header: Simulating progress bar and back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerEffect(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                ShimmerEffect(
                    modifier = Modifier
                        .width(180.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                )
                ShimmerEffect(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card content skeleton
            GlassSurface(
                shape = RoundedCornerShape(CunnyDimens.radiusLg),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title block
                    ShimmerEffect(
                        modifier = Modifier
                            .width(140.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(CunnyDimens.radiusSm))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mascot placeholder — uses pulsing shimmer for hero feel
                    PulsingShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(CunnyDimens.radiusMd))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Content paragraph lines
                    repeat(4) { i ->
                        val widthPercent = when (i) {
                            0 -> 1f
                            1 -> 0.92f
                            2 -> 0.85f
                            else -> 0.45f
                        }
                        ShimmerEffect(
                            modifier = Modifier
                                .fillMaxWidth(widthPercent)
                                .height(14.dp)
                                .clip(RoundedCornerShape(CunnyDimens.radiusSm))
                        )
                    }
                }
            }
        }

        // Bottom action button skeleton
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(CunnyDimens.radiusFull))
        )
    }
}

