package com.eleonorez.cunny.data.retrofit

import com.eleonorez.cunny.data.model.ParentConsentRequest
import com.eleonorez.cunny.data.model.ParentConsentResponse
import com.eleonorez.cunny.data.response.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {

    @GET("categories")
    suspend fun getCategories(
        @Query("language") language: String = "id"
    ): CategoriesResponse

    @GET("categories/{catSlug}/courses")
    suspend fun getCategoryCourses(
        @Path("catSlug") catSlug: String,
        @Query("language") language: String = "id"
    ): CategoryCoursesResponse

    @GET("courses/{courseSlug}/journey")
    suspend fun getCourseJourney(
        @Path("courseSlug") courseSlug: String,
        @Query("language") language: String = "id"
    ): CourseJourneyResponse

    @GET("courses/{courseSlug}/lessons")
    suspend fun getCourseLessons(
        @Path("courseSlug") courseSlug: String,
        @Query("language") language: String = "id"
    ): CourseLessonsResponse

    @GET("learning-materials")
    suspend fun getLearningMaterials(
        @Query("language") language: String = "id"
    ): MaterialsResponse

    @Multipart
    @POST("predict")
    suspend fun uploadImage(
        @Part uploaded_file: MultipartBody.Part,
        @Query("language") language: String = "id"
    ): PredictionResponse

    @GET("lessons")
    suspend fun getLessons(
        @Query("language") language: String = "id"
    ): LessonsListResponse

    @GET("lessons/{slug}")
    suspend fun getLessonBySlug(
        @Path("slug") slug: String,
        @Query("language") language: String = "id"
    ): LessonDetailResponse

    @POST("auth/register")
    suspend fun registerUser(
        @Body body: Map<String, String>
    ): AuthResponse

    @GET("auth/me")
    suspend fun getMe(): AuthResponse

    @DELETE("auth/me")
    suspend fun deleteAccount(): AuthResponse

    @GET("me/progress")
    suspend fun getProgress(): ProgressResponse

    @PUT("me/progress")
    suspend fun syncProgress(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): SyncProgressResponse

    @PUT("me/gamification")
    suspend fun syncGamification(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): AuthResponse

    @PUT("me/badges")
    suspend fun syncBadges(
        @Body body: Map<String, List<String>>
    ): retrofit2.Response<Any>

    @POST("classes")
    suspend fun createClass(
        @Body body: Map<String, String>
    ): ClassResponse

    @POST("classes/join")
    suspend fun joinClass(
        @Body body: Map<String, String>
    ): ClassResponse

    @GET("classes")
    suspend fun getClasses(): ClassesListResponse

    @GET("classes/{id}/progress")
    suspend fun getClassProgress(
        @Path("id") classId: Int
    ): ClassProgressResponse

    @GET("classes/{id}/assignments")
    suspend fun getAssignments(
        @Path("id") classId: Int
    ): AssignmentsResponse

    @POST("classes/{id}/assignments")
    suspend fun createClassAssignment(
        @Path("id") classId: Int,
        @Body body: Map<String, String>
    ): AssignmentsResponse

    @POST("billing/verify")
    suspend fun verifyPurchase(
        @Body body: Map<String, String>
    ): BillingVerifyResponse

    @POST("auth/parent-consent")
    suspend fun requestParentConsent(
        @Body body: ParentConsentRequest
    ): ParentConsentResponse
}
