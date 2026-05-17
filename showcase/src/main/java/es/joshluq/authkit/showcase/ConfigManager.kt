package es.joshluq.authkit.showcase

import android.content.Context

class ConfigManager(context: Context) {
    private val prefs = context.getSharedPreferences("showcase_config", Context.MODE_PRIVATE)

    fun savePreset(preset: SessionPreset) {
        prefs.edit().putString("active_preset", preset.name).commit()
    }

    fun getActivePreset(): SessionPreset? {
        val name = prefs.getString("active_preset", null) ?: return null
        return try {
            SessionPreset.valueOf(name)
        } catch (e: Exception) {
            null
        }
    }

    fun clearConfig() {
        prefs.edit().clear().commit()
    }
}
