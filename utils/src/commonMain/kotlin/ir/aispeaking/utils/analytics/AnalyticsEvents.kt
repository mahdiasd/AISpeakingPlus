package ir.aispeaking.utils.analytics

/**
 * Standard event names, parameter keys, and convenience extension methods
 * for tracking user journey, stages, auth, and monetization on Firebase.
 */
object AnalyticsEvents {
    // Events
    const val EVENT_APP_OPEN = "app_open"
    const val EVENT_SCREEN_VIEW = "screen_view"
    const val EVENT_SIGN_UP = "sign_up"
    const val EVENT_LOGIN = "login"
    const val EVENT_LOGOUT = "logout"
    const val EVENT_STAGE_OPENED = "stage_opened"
    const val EVENT_STAGE_COMPLETED = "stage_completed"
    const val EVENT_STAGE_LOCKED_CLICK = "stage_locked_click"
    const val EVENT_HINT_REQUESTED = "hint_requested"
    const val EVENT_CHAT_MESSAGE_SENT = "chat_message_sent"
    const val EVENT_SUBSCRIPTION_PLANS_VIEWED = "subscription_plans_viewed"
    const val EVENT_SUBSCRIPTION_PURCHASE_STARTED = "subscription_purchase_started"
    const val EVENT_SUBSCRIPTION_PURCHASE_SUCCESS = "subscription_purchase_success"
    const val EVENT_SUBSCRIPTION_PURCHASE_FAILED = "subscription_purchase_failed"
    const val EVENT_LEADERBOARD_OPENED = "leaderboard_opened"

    // Parameters
    const val PARAM_SCREEN_NAME = "screen_name"
    const val PARAM_SCREEN_CLASS = "screen_class"
    const val PARAM_STAGE_ID = "stage_id"
    const val PARAM_STAGE_TITLE = "stage_title"
    const val PARAM_STAGE_ORDER = "stage_order"
    const val PARAM_USER_LEVEL = "user_level"
    const val PARAM_ACCESS_TIER = "access_tier"
    const val PARAM_PLATFORM = "platform"
    const val PARAM_METHOD = "method"
    const val PARAM_SCORE = "score"
    const val PARAM_STARS = "stars"
    const val PARAM_TURNS_COUNT = "turns_count"
    const val PARAM_HINTS_USED = "hints_used"
    const val PARAM_PASSED = "passed"
    const val PARAM_LOCK_STATUS = "lock_status"
    const val PARAM_PLAN_ID = "plan_id"
    const val PARAM_MARKET = "market"
    const val PARAM_ERROR = "error"
    const val PARAM_INPUT_MODE = "input_mode"

    // User properties
    const val USER_PROP_PLATFORM = "platform"
    const val USER_PROP_LANGUAGE_LEVEL = "language_level"
    const val USER_PROP_ACCESS_TIER = "access_tier"
    const val USER_PROP_TOTAL_STARS = "total_stars"
}

fun AnalyticsTracker.logScreenView(screenName: String, screenClass: String? = null) {
    val params = mutableMapOf<String, Any?>(
        AnalyticsEvents.PARAM_SCREEN_NAME to screenName
    )
    if (screenClass != null) {
        params[AnalyticsEvents.PARAM_SCREEN_CLASS] = screenClass
    }
    logEvent(AnalyticsEvents.EVENT_SCREEN_VIEW, params)
}

fun AnalyticsTracker.logSignUp(
    userId: String,
    method: String,
    platform: String = "android",
    userLevel: String? = null
) {
    setUserId(userId)
    setUserProperty(AnalyticsEvents.USER_PROP_PLATFORM, platform)
    if (userLevel != null) {
        setUserProperty(AnalyticsEvents.USER_PROP_LANGUAGE_LEVEL, userLevel)
    }
    logEvent(
        AnalyticsEvents.EVENT_SIGN_UP,
        mapOf(
            "user_id" to userId,
            AnalyticsEvents.PARAM_METHOD to method,
            AnalyticsEvents.PARAM_PLATFORM to platform,
            AnalyticsEvents.PARAM_USER_LEVEL to (userLevel ?: "A1")
        )
    )
}

fun AnalyticsTracker.logLogin(
    userId: String,
    method: String,
    platform: String = "android",
    accessTier: String? = null,
    userLevel: String? = null
) {
    setUserId(userId)
    setUserProperty(AnalyticsEvents.USER_PROP_PLATFORM, platform)
    if (accessTier != null) {
        setUserProperty(AnalyticsEvents.USER_PROP_ACCESS_TIER, accessTier)
    }
    if (userLevel != null) {
        setUserProperty(AnalyticsEvents.USER_PROP_LANGUAGE_LEVEL, userLevel)
    }
    logEvent(
        AnalyticsEvents.EVENT_LOGIN,
        mapOf(
            "user_id" to userId,
            AnalyticsEvents.PARAM_METHOD to method,
            AnalyticsEvents.PARAM_PLATFORM to platform,
            AnalyticsEvents.PARAM_ACCESS_TIER to (accessTier ?: "GUEST"),
            AnalyticsEvents.PARAM_USER_LEVEL to (userLevel ?: "A1")
        )
    )
}

fun AnalyticsTracker.logStageOpened(
    stageId: String,
    stageTitle: String,
    stageOrder: Int,
    userLevel: String,
    accessTier: String
) {
    logEvent(
        AnalyticsEvents.EVENT_STAGE_OPENED,
        mapOf(
            AnalyticsEvents.PARAM_STAGE_ID to stageId,
            AnalyticsEvents.PARAM_STAGE_TITLE to stageTitle,
            AnalyticsEvents.PARAM_STAGE_ORDER to stageOrder,
            AnalyticsEvents.PARAM_USER_LEVEL to userLevel,
            AnalyticsEvents.PARAM_ACCESS_TIER to accessTier
        )
    )
}

fun AnalyticsTracker.logStageCompleted(
    stageId: String,
    stageTitle: String,
    score: Int,
    stars: Int,
    turnsCount: Int,
    hintsUsed: Int,
    passed: Boolean
) {
    logEvent(
        AnalyticsEvents.EVENT_STAGE_COMPLETED,
        mapOf(
            AnalyticsEvents.PARAM_STAGE_ID to stageId,
            AnalyticsEvents.PARAM_STAGE_TITLE to stageTitle,
            AnalyticsEvents.PARAM_SCORE to score,
            AnalyticsEvents.PARAM_STARS to stars,
            AnalyticsEvents.PARAM_TURNS_COUNT to turnsCount,
            AnalyticsEvents.PARAM_HINTS_USED to hintsUsed,
            AnalyticsEvents.PARAM_PASSED to passed
        )
    )
}

fun AnalyticsTracker.logStageLockedClick(
    stageId: String,
    stageTitle: String,
    stageOrder: Int,
    lockStatus: String
) {
    logEvent(
        AnalyticsEvents.EVENT_STAGE_LOCKED_CLICK,
        mapOf(
            AnalyticsEvents.PARAM_STAGE_ID to stageId,
            AnalyticsEvents.PARAM_STAGE_TITLE to stageTitle,
            AnalyticsEvents.PARAM_STAGE_ORDER to stageOrder,
            AnalyticsEvents.PARAM_LOCK_STATUS to lockStatus
        )
    )
}
