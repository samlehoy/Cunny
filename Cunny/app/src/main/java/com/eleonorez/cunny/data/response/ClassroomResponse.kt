package com.eleonorez.cunny.data.response

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @field:SerializedName("error") val error: Boolean? = null,
    @field:SerializedName("message") val message: String? = null,
    @field:SerializedName("user") val user: UserDto? = null
)

data class UserDto(
    @field:SerializedName("id") val id: Int = 0,
    @field:SerializedName("firebase_uid") val firebaseUid: String? = null,
    @field:SerializedName("display_name") val displayName: String? = null,
    @field:SerializedName("role") val role: String? = null,
    @field:SerializedName("xp") val xp: Int = 0,
    @field:SerializedName("level") val level: Int = 1,
    @field:SerializedName("streak") val streak: Int = 0,
    @field:SerializedName("energy") val energy: Int = 5,
    @field:SerializedName("last_active_date") val lastActiveDate: String? = null
)

data class ProgressResponse(
    @field:SerializedName("error") val error: Boolean? = null,
    @field:SerializedName("progress") val progress: List<ProgressDto>? = null,
    @field:SerializedName("badges") val badges: List<BadgeDto>? = null
)

// Response for PUT /me/progress (returns single object, not list)
data class SyncProgressResponse(
    @field:SerializedName("error") val error: Boolean? = null,
    @field:SerializedName("progress") val progress: ProgressDto? = null
)

data class ProgressDto(
    @field:SerializedName("lesson_slug") val lessonSlug: String? = null,
    @field:SerializedName("score") val score: Int = 0
)

data class BadgeDto(
    @field:SerializedName("badge_id") val badgeId: String? = null
)

data class ClassResponse(
    @field:SerializedName("error") val error: Boolean? = null,
    @field:SerializedName("message") val message: String? = null,
    @field:SerializedName("class") val classData: ClassDto? = null
)

data class ClassesListResponse(
    @field:SerializedName("error") val error: Boolean? = null,
    @field:SerializedName("message") val message: String? = null,
    @field:SerializedName("classes") val classes: List<ClassDto>? = null
)

data class ClassDto(
    @field:SerializedName("id") val id: Int = 0,
    @field:SerializedName("name") val name: String? = null,
    @field:SerializedName("join_code") val joinCode: String? = null,
    @field:SerializedName("membership_role") val membershipRole: String? = null
)

data class ClassProgressResponse(
    @field:SerializedName("error") val error: Boolean? = null,
    @field:SerializedName("message") val message: String? = null,
    @field:SerializedName("roster") val roster: List<RosterEntry>? = null
)

data class RosterEntry(
    @field:SerializedName("id") val id: Int = 0,
    @field:SerializedName("display_name") val displayName: String? = null,
    @field:SerializedName("lessons_completed") val lessonsCompleted: Int = 0,
    @field:SerializedName("total_score") val totalScore: Int = 0
)

data class AssignmentsResponse(
    @field:SerializedName("error") val error: Boolean? = null,
    @field:SerializedName("message") val message: String? = null,
    @field:SerializedName("assignments") val assignments: List<AssignmentDto>? = null
)

data class AssignmentDto(
    @field:SerializedName("id") val id: Int = 0,
    @field:SerializedName("lesson_slug") val lessonSlug: String? = null,
    @field:SerializedName("title") val title: String? = null,
    @field:SerializedName("due_at") val dueAt: String? = null
)


