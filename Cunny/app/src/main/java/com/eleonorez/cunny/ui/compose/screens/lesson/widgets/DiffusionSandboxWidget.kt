package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.snap
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.data.model.WidgetBlock
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

/**
 * DiffusionSandboxWidget
 * 
 * Interactive simulation of image denoising diffusion process (Generative AI).
 * Uses "Kelinci Pembersih Kabut" (Fog-Cleaning Rabbit) analogy to demystify
 * how AI generates images from noise, step by step.
 * 
 * Reference Journal:
 * Ali, S., Ravi, P., Moore, K., Abelson, H., & Breazeal, C. (2024).
 * A Picture Is Worth a Thousand Words: Co-designing Text-to-Image Generation
 * Learning Materials for K-12 with Educators.
 * Proceedings of the AAAI Conference on Artificial Intelligence, 38(21), 23260-23267.
 * https://doi.org/10.1609/aaai.v38i21.30373
 */
@Composable
fun DiffusionSandboxWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    var selectedCharacter by remember { mutableStateOf("🐰") }
    var selectedItem by remember { mutableStateOf("🥕") }
    var sliderVal by remember { mutableFloatStateOf(0f) }
    val completed = sliderVal >= 100f
    val activeChar = when (selectedCharacter) {
        "🐰" -> 0
        "🐱" -> 1
        else -> 2
    }
    val activeItem = when (selectedItem) {
        "🥕" -> 0
        "🚀" -> 1
        else -> 2
    }

    val characterChips = remember {
        listOf("🐰 Kelinci" to "🐰", "🐱 Kucing" to "🐱", "🐼 Panda" to "🐼")
    }
    val itemChips = remember {
        listOf("🥕 Wortel" to "🥕", "🚀 Roket" to "🚀", "🎸 Gitar" to "🎸")
    }

    LaunchedEffect(completed) {
        if (completed) {
            onWidgetCompleted(true)
        }
    }

    val headerEmoji = when {
        sliderVal >= 100f -> "${selectedCharacter}🎉"
        sliderVal >= 70f -> "${selectedCharacter}✨"
        sliderVal >= 40f -> "${selectedCharacter}🌬️"
        sliderVal >= 10f -> "${selectedCharacter}🌬️"
        else -> selectedCharacter
    }

    val promptText = when (selectedCharacter) {
        "🐰" -> "Kelinci sedang bersama "
        "🐱" -> "Kucing sedang bersama "
        else -> "Panda sedang bersama "
    } + when (selectedItem) {
        "🥕" -> "Wortel 🥕"
        "🚀" -> "Roket 🚀"
        else -> "Gitar 🎸"
    }

    val narrativeText = when {
        sliderVal >= 100f -> when (selectedCharacter to selectedItem) {
            "🐰" to "🥕" -> "Hore! Jendelanya bersih! Ternyata ada gambar wortel 🥕 kesukaan Kelinci 🐰!"
            "🐰" to "🚀" -> "Hebat! Kelinci 🐰 terbang tinggi menembus kabut luar angkasa dengan roket 🚀!"
            "🐰" to "🎸" -> "Keren! Kelinci 🐰 memainkan melodi indah dengan gitarnya 🎸 di balik kaca!"
            "🐱" to "🥕" -> "Nyam! Kucing 🐱 penasaran mencoba mengunyah wortel 🥕 milik Kelinci!"
            "🐱" to "🚀" -> "Kucing 🐱 astronot meluncur membelah kabut galaksi yang gelap dengan roket 🚀!"
            "🐱" to "🎸" -> "Kucing 🐱 menyanyikan lagu riang sambil memetik senar gitarnya 🎸!"
            "🐼" to "🥕" -> "Panda 🐼 menyukai wortel 🥕 manis ini sebagai camilan selain bambu!"
            "🐼" to "🚀" -> "Panda 🐼 gemas melambaikan tangan dari dalam roket 🚀 super cepat!"
            else -> "Panda 🐼 memainkan gitar 🎸 dengan speaker besar hingga kabutnya terbang!"
        }
        sliderVal >= 70f -> "Dikit lagi bersih! Gambarnya sudah hampir terlihat utuh. Terus gosok kacanya!"
        sliderVal >= 40f -> "Wah, kabutnya berkurang! Bisakah kamu menebak gambarnya?"
        sliderVal >= 10f -> "Syuuu... Kabutnya mulai menipis. Eh, ada objek apa itu?"
        else -> "Uuuh, kaca jendelanya penuh kabut tebal! Yuk bantu membersihkan kabutnya!"
    }

    val visualEmojis = when {
        sliderVal >= 100f -> when (selectedCharacter to selectedItem) {
            "🐰" to "🥕" -> "🥕🐰🎉"
            "🐰" to "🚀" -> "🐰🚀✨"
            "🐰" to "🎸" -> "🐰🎸🎶"
            "🐱" to "🥕" -> "🐱🥕😋"
            "🐱" to "🚀" -> "🐱🚀🌌"
            "🐱" to "🎸" -> "🐱🎸🎵"
            "🐼" to "🥕" -> "🐼🥕🎋"
            "🐼" to "🚀" -> "🐼🚀🌠"
            else -> "🐼🎸🔊"
        }
        sliderVal >= 70f -> "✨${selectedItem}🌿"
        sliderVal >= 40f -> "🌬️${selectedItem}🌫️"
        sliderVal >= 10f -> "🌫️${selectedItem}🌫️"
        else -> "🌫️🌫️🌫️"
    }

    val outerShape = RoundedCornerShape(CunnyDimens.radiusLg)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(bottom = 4.dp)
            .background(
                color = CunnyColors.tactileShadow,
                shape = outerShape
            )
    ) {
        GlassSurface(
            shape = outerShape,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = headerEmoji,
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Bengkel Gambar AI",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bantu Kelinci membersihkan gambar dari gangguan! Geser slider pelan-pelan untuk melihat bagaimana AI menggambar dengan membersihkan kabut.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Animasi skala memantul (bounce scale) ketika gambar 100% terbuka
                val scale by animateFloatAsState(
                    targetValue = if (completed) 1.1f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "emojiScale"
                )

                val glowAlpha by animateFloatAsState(
                    targetValue = if (completed) 0.8f else 0.0f,
                    animationSpec = if (completed) {
                        infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    } else {
                        snap(0)
                    },
                    label = "glowAlpha"
                )

                // Pre-allocated paths to avoid GC churn during animation / rubbing
                val sheenPath1 = remember { Path() }
                val sheenPath2 = remember { Path() }

                val boxShape = RoundedCornerShape(CunnyDimens.radiusMd)
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .padding(bottom = 3.dp)
                        .background(
                            color = CunnyColors.tactileShadow,
                            shape = boxShape
                        )
                ) {
                    val borderColor = lerp(
                        CunnyColors.borderLight,
                        CunnyColors.primary,
                        glowAlpha
                    )

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .offset(y = (-3).dp)
                            .clip(boxShape)
                            .background(CunnyColors.backgroundWarm) // default base background under illustration
                            .border(1.dp, borderColor, boxShape)
                            // Detektor gestur menggosok/drag
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val distance = dragAmount.getDistance()
                                    val speedFactor = 12f // Faktor sensitivitas gosokan jari
                                    sliderVal = (sliderVal + (distance / speedFactor)).coerceIn(0f, 100f)
                                }
                            }
                            // Sheen overlay on top of content
                            .drawWithContent {
                                drawContent() // Draw emojis and fog layer first
                                
                                val sheenAlpha = if (completed) 0.08f else 0.15f
                                
                                sheenPath1.reset()
                                sheenPath1.moveTo(0f, 0f)
                                sheenPath1.lineTo(size.width * 0.35f, 0f)
                                sheenPath1.lineTo(0f, size.height * 0.35f)
                                sheenPath1.close()
                                drawPath(sheenPath1, Color.White.copy(alpha = sheenAlpha))

                                sheenPath2.reset()
                                sheenPath2.moveTo(size.width * 0.45f, 0f)
                                sheenPath2.lineTo(size.width * 0.6f, 0f)
                                sheenPath2.lineTo(0f, size.height * 0.6f)
                                sheenPath2.lineTo(0f, size.height * 0.45f)
                                sheenPath2.close()
                                drawPath(sheenPath2, Color.White.copy(alpha = sheenAlpha * 0.5f))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Lapis 1: Latar belakang dan elemen ilustrasi penuh (Option A) yang memudar masuk
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(scale) // Bounce pop animation on completed
                                .graphicsLayer { alpha = sliderVal / 100f }
                        ) {
                            // 1. Background
                            val bgBrush = when (activeItem) {
                                0 -> Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))) // Kebun hijau
                                1 -> Brush.verticalGradient(listOf(Color(0xFF0D0B21), Color(0xFF221F30))) // Antariksa gelap
                                else -> Brush.verticalGradient(listOf(Color(0xFF262040), Color(0xFF4A3FB5))) // Panggung ungu
                            }
                            Box(modifier = Modifier.fillMaxSize().background(bgBrush))

                            // 2. Partikel Dekorasi (Melayang stasioner)
                            when (activeItem) {
                                0 -> { // Wortel: Daun-daunan dan kilau
                                    Text("🌿", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopStart).padding(12.dp).graphicsLayer { rotationZ = -15f })
                                    Text("🌸", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopEnd).padding(14.dp).graphicsLayer { rotationZ = 10f })
                                    Text("✨", fontSize = 14.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 28.dp))
                                    Text("✨", fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 24.dp))
                                }
                                1 -> { // Roket: Bintang-bintang luar angkasa
                                    Text("⭐", fontSize = 14.sp, modifier = Modifier.align(Alignment.TopStart).padding(14.dp))
                                    Text("✨", fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp))
                                    Text("⭐", fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 14.dp, bottom = 26.dp))
                                    Text("🛸", fontSize = 16.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 24.dp).graphicsLayer { rotationZ = -10f })
                                }
                                else -> { // Gitar: Not-not lagu
                                    Text("🎵", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopStart).padding(14.dp).graphicsLayer { rotationZ = -15f })
                                    Text("🎶", fontSize = 18.sp, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).graphicsLayer { rotationZ = 15f })
                                    Text("🎵", fontSize = 14.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 28.dp).graphicsLayer { rotationZ = 10f })
                                    Text("✨", fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 26.dp))
                                }
                            }

                            // 3. Karakter & Item Utama
                            val charEmoji = when (activeChar) {
                                0 -> "🐰"
                                1 -> "🐱"
                                else -> "🐼"
                            }

                            val itemEmoji = when (activeItem) {
                                0 -> "🥕"
                                1 -> "🚀"
                                else -> "🎸"
                            }

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                when (activeItem) {
                                    0 -> { // Wortel (Berkebun): Karakter memeluk wortel besar
                                        Text(
                                            text = charEmoji,
                                            fontSize = 52.sp,
                                            modifier = Modifier.offset(y = 8.dp)
                                        )
                                        Text(
                                            text = itemEmoji,
                                            fontSize = 44.sp,
                                            modifier = Modifier
                                                .offset(x = 14.dp, y = 16.dp)
                                                .graphicsLayer { rotationZ = -20f }
                                        )
                                    }
                                    1 -> { // Roket (Luar Angkasa): Karakter berada di dalam roket (peeking)
                                        // Roket besar miring
                                        Text(
                                            text = itemEmoji,
                                            fontSize = 62.sp,
                                            modifier = Modifier
                                                .offset(x = (-4).dp, y = (-6).dp)
                                                .graphicsLayer { rotationZ = 45f }
                                        )
                                        // Karakter di jendela roket
                                        Text(
                                            text = charEmoji,
                                            fontSize = 22.sp,
                                            modifier = Modifier
                                                .offset(x = (-4).dp, y = (-6).dp) // Disesuaikan agar pas di jendela roket emoji
                                        )
                                    }
                                    else -> { // Gitar (Konser Musik): Karakter memainkan gitar
                                        // Karakter di tengah agak atas
                                        Text(
                                            text = charEmoji,
                                            fontSize = 52.sp,
                                            modifier = Modifier.offset(y = (-10).dp)
                                        )
                                        // Gitar diposisikan miring di depan badan karakter
                                        Text(
                                            text = itemEmoji,
                                            fontSize = 48.sp,
                                            modifier = Modifier
                                                .offset(x = 10.dp, y = 12.dp)
                                                .graphicsLayer { rotationZ = -30f }
                                        )
                                    }
                                }
                            }
                        }

                        // Lapis 2: Lapisan kabut abu-abu yang memudar keluar (di atas Lapis 1)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray.copy(alpha = 1f - (sliderVal / 100f)))
                                .drawBehind {
                                    val fogAlpha = 1f - (sliderVal / 100f)
                                    if (fogAlpha > 0.05f) {
                                        val dropletCount = 15
                                        for (i in 0 until dropletCount) {
                                            val x = (i * 103 + 23) % size.width
                                            val y = (i * 83 + 13) % size.height
                                            val radius = ((i * 17 + 7) % 10 + 6).dp.toPx()
                                            drawCircle(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(Color.White.copy(alpha = fogAlpha * 0.35f), Color.Transparent),
                                                    center = Offset(x, y),
                                                    radius = radius
                                                ),
                                                radius = radius,
                                                center = Offset(x, y)
                                            )
                                        }
                                    }
                                }
                        )

                        // Lapis 3: Status Text Overlay Pill di bagian paling depan
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (completed) "Bersih!" else "Noise: ${100 - sliderVal.toInt()}%",
                                fontSize = 10.sp,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (completed) Color(0xFFFFD700) /* Gold */ else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .clip(RoundedCornerShape(CunnyDimens.radiusMd))
                        .background(
                            if (completed) CunnyColors.primaryPale else CunnyColors.backgroundSoft
                        )
                        .border(
                            width = 1.dp,
                            color = if (completed) CunnyColors.primary.copy(alpha = 0.2f) else CunnyColors.borderLight,
                            shape = RoundedCornerShape(CunnyDimens.radiusMd)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = narrativeText,
                        fontFamily = DmSansFontFamily,
                        fontSize = 13.sp,
                        color = if (completed) CunnyColors.primary else CunnyColors.textBody,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bagian 1: Pemilih Karakter
                Text(
                    text = "1. Pilih Karakter Utama:",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CunnyColors.textDark,
                    modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    characterChips.forEach { (label, emoji) ->
                        val isSelected = selectedCharacter == emoji
                        
                        val chipInteractionSource = remember { MutableInteractionSource() }
                        val chipIsPressed by chipInteractionSource.collectIsPressedAsState()
                        val chipOffsetY by animateDpAsState(
                            targetValue = if (chipIsPressed) 0.dp else (-3).dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "charChipOffset"
                        )
                        
                        val chipShape = RoundedCornerShape(CunnyDimens.radiusFull)
                        val shadowColor = if (isSelected) CunnyColors.primaryShadow else CunnyColors.tactileShadow
                        val bg = if (isSelected) CunnyColors.primary else CunnyColors.backgroundSoft
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 3.dp)
                                .background(shadowColor, shape = chipShape)
                                .clickable(
                                    interactionSource = chipInteractionSource,
                                    indication = null,
                                    role = Role.Button
                                ) {
                                    if (selectedCharacter != emoji) {
                                        selectedCharacter = emoji
                                        sliderVal = 0f
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .offset(y = chipOffsetY)
                                    .clip(chipShape)
                                    .background(bg)
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else CunnyColors.textBody
                                )
                            }
                        }
                    }
                }

                // Bagian 2: Pemilih Benda/Aksi
                Text(
                    text = "2. Pilih Benda/Aksi:",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CunnyColors.textDark,
                    modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemChips.forEach { (label, item) ->
                        val isSelected = selectedItem == item
                        
                        val chipInteractionSource = remember { MutableInteractionSource() }
                        val chipIsPressed by chipInteractionSource.collectIsPressedAsState()
                        val chipOffsetY by animateDpAsState(
                            targetValue = if (chipIsPressed) 0.dp else (-3).dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "itemChipOffset"
                        )
                        
                        val chipShape = RoundedCornerShape(CunnyDimens.radiusFull)
                        val shadowColor = if (isSelected) CunnyColors.primaryShadow else CunnyColors.tactileShadow
                        val bg = if (isSelected) CunnyColors.primary else CunnyColors.backgroundSoft
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 3.dp)
                                .background(shadowColor, shape = chipShape)
                                .clickable(
                                    interactionSource = chipInteractionSource,
                                    indication = null,
                                    role = Role.Button
                                ) {
                                    if (selectedItem != item) {
                                        selectedItem = item
                                        sliderVal = 0f
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .offset(y = chipOffsetY)
                                    .clip(chipShape)
                                    .background(bg)
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else CunnyColors.textBody
                                )
                            }
                        }
                    }
                }

                // Bagian 3: Prompt Terkumpul Box
                GlassSurface(
                    shape = RoundedCornerShape(CunnyDimens.radiusMd),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CunnyColors.primary.copy(alpha = 0.15f), RoundedCornerShape(CunnyDimens.radiusMd))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Prompt Gambar: \"$promptText\"",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CunnyColors.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = sliderVal,
                    onValueChange = { sliderVal = it },
                    valueRange = 0f..100f,
                    steps = 9
                )
            }
        }
    }
}
