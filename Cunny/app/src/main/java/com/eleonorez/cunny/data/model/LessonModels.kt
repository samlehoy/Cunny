package com.eleonorez.cunny.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.RawValue
import androidx.compose.runtime.Immutable

@Immutable
data class Lesson(
    val id: Int,
    val slug: String,
    val courseSlug: String = "",
    val chapterSlug: String = "",
    val lessonOrder: Int = 0,
    val title: String,
    val summary: String?,
    val blocks: List<Block>,
    val language: String
)

@Immutable
sealed class Block : Parcelable {
    @IgnoredOnParcel
    abstract val type: String
}

@Immutable
@Parcelize
data class TextBlock(
    val markdown: String
) : Block() {
    @IgnoredOnParcel
    override val type = "text"
}

@Immutable
@Parcelize
data class ImageBlock(
    val imageUrl: String,
    val caption: String?
) : Block() {
    @IgnoredOnParcel
    override val type = "image"
}

@Immutable
@Parcelize
data class VideoBlock(
    val videoUrl: String,
    val caption: String?
) : Block() {
    @IgnoredOnParcel
    override val type = "video"
}

@Immutable
@Parcelize
data class CalloutBlock(
    val style: String,
    val markdown: String
) : Block() {
    @IgnoredOnParcel
    override val type = "callout"
}

@Immutable
@Parcelize
data class WidgetBlock(
    val widgetType: String,
    val config: @RawValue Map<String, Any>?
) : Block() {
    @IgnoredOnParcel
    override val type = "widget"
}

@Immutable
@Parcelize
data class QuizBlock(
    val question: String,
    val choices: List<String>,
    val answerIndex: Int,
    val hint: String?,
    val explanation: String?
) : Block() {
    @IgnoredOnParcel
    override val type = "quiz"
}


