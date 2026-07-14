package com.eleonorez.cunny.ui.compose.navigation

object CunnyRoutes {
    const val ONBOARDING = "onboarding/{step}"
    const val ONBOARDING_STEP_1 = "onboarding/1"
    const val ONBOARDING_STEP_2 = "onboarding/2"

    const val AGE_GATE = "onboarding/age-gate"
    const val PARENT_RESTRICTION = "onboarding/parent-restriction"
    const val PARENT_EMAIL_SENT = "onboarding/parent-email-sent/{parentEmail}"

    fun parentEmailSent(parentEmail: String) = "onboarding/parent-email-sent/$parentEmail"

    const val AUTH_LOGIN = "auth/login"
    const val AUTH_REGISTER = "auth/register"

    const val HOME = "home"
    const val COURSES = "courses"
    const val SETTINGS = "settings"

    const val COURSE_DETAIL = "course/{slug}"
    const val LESSON = "lesson/{slug}"
    const val LESSON_INTRO = "lesson/{slug}/intro"
    const val PRACTICE = "practice/{lessonSlug}"
    const val PREDICTION = "prediction"
    const val PROFILE = "profile"
    const val PLAYGROUND = "playground"
    const val PAYWALL = "paywall"
    const val COURSE_CELEBRATION = "celebration/{courseSlug}"
    fun courseCelebration(courseSlug: String) = "celebration/$courseSlug"

    val TAB_ROUTES = setOf(HOME, COURSES, PLAYGROUND, SETTINGS)

    fun courseDetail(slug: String) = "course/$slug"
    fun lesson(slug: String) = "lesson/$slug"
    fun lessonIntro(slug: String) = "lesson/$slug/intro"
    fun practice(lessonSlug: String) = "practice/$lessonSlug"
}


