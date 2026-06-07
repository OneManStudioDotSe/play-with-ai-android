package se.onemanstudio.playaroundwithai.feature.settings

/**
 * Identifies the feature screen that opened the Settings sheet.
 *
 * Drives which screen-specific section is rendered at the top of the sheet.
 * The common sections (General, AI Models, Weekly Usage, About) are always shown
 * below the screen-specific one, regardless of the value.
 */
enum class SettingsScreen {
    CHAT,
    EXPLORE,
    DREAM,
    PLAN,
}
