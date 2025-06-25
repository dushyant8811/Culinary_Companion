// PreferencesManager.kt
import android.content.Context
import androidx.core.content.edit

class PreferencesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var isProfileSetupComplete: Boolean
        get() = sharedPreferences.getBoolean("profile_setup_complete", false)
        set(value) {
            sharedPreferences.edit {
                putBoolean("profile_setup_complete", value)
                apply() // Ensure immediate write
            }
        }
}