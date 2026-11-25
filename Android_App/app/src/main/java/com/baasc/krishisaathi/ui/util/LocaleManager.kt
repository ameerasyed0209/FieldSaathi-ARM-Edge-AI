package com.baasc.krishisaathi.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.util.Locale

class LocalizedContextWrapper(
    private val baseContext: Context,
    private val configContext: Context
) : ContextWrapper(baseContext), ActivityResultRegistryOwner, OnBackPressedDispatcherOwner {

    override fun getResources(): Resources = configContext.resources

    override fun getAssets(): AssetManager = configContext.assets

    override val activityResultRegistry: ActivityResultRegistry
        get() = (baseContext as? ActivityResultRegistryOwner)?.activityResultRegistry
            ?: throw IllegalStateException("Base context does not implement ActivityResultRegistryOwner")

    override val onBackPressedDispatcher: OnBackPressedDispatcher
        get() = (baseContext as? OnBackPressedDispatcherOwner)?.onBackPressedDispatcher
            ?: throw IllegalStateException("Base context does not implement OnBackPressedDispatcherOwner")

    override val lifecycle: Lifecycle
        get() = (baseContext as? LifecycleOwner)?.lifecycle
            ?: throw IllegalStateException("Base context does not implement LifecycleOwner")
}

object LocaleManager {

    fun getLocaleForLanguage(languageCode: String): Locale {
        return when (languageCode) {
            "kn" -> Locale("kn", "IN")
            "hi" -> Locale("hi", "IN")
            "mr" -> Locale("mr", "IN")
            "ta" -> Locale("ta", "IN")
            "te" -> Locale("te", "IN")
            "ml" -> Locale("ml", "IN")
            "gu" -> Locale("gu", "IN")
            "ur" -> Locale("ur", "IN")
            "pa" -> Locale("pa", "IN")
            "ar" -> Locale("ar")
            else -> Locale(languageCode)
        }
    }

    fun createLocalizedContext(baseContext: Context, languageCode: String): Context {
        val locale = getLocaleForLanguage(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(baseContext.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        val configContext = baseContext.createConfigurationContext(config)
        return LocalizedContextWrapper(baseContext, configContext)
    }
}

/**
 * Provides localized Context, Configuration, and ActivityResultRegistryOwner to all
 * Compose children so that:
 * 1. `stringResource(R.string.xyz)` immediately reflects the selected language dynamically.
 * 2. Activity Result Launchers (e.g. Photo Picker / Gallery in HomeScreen) resolve their
 *    ActivityResultRegistryOwner without throwing IllegalStateException.
 */
@Composable
fun LocalizedApp(
    languageCode: String,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current
        ?: (baseContext as? ActivityResultRegistryOwner)
    val backDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
        ?: (baseContext as? OnBackPressedDispatcherOwner)

    val localizedContext = remember(baseContext, languageCode) {
        LocaleManager.createLocalizedContext(baseContext, languageCode)
    }
    val localizedConfig = remember(localizedContext) {
        localizedContext.resources.configuration
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfig
    ) {
        if (activityResultRegistryOwner != null && backDispatcherOwner != null) {
            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides activityResultRegistryOwner,
                LocalOnBackPressedDispatcherOwner provides backDispatcherOwner
            ) {
                content()
            }
        } else if (activityResultRegistryOwner != null) {
            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides activityResultRegistryOwner
            ) {
                content()
            }
        } else if (backDispatcherOwner != null) {
            CompositionLocalProvider(
                LocalOnBackPressedDispatcherOwner provides backDispatcherOwner
            ) {
                content()
            }
        } else {
            content()
        }
    }
}
