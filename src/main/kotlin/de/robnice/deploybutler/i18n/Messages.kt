package de.robnice.deploybutler.i18n

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.messages"

object Messages : DynamicBundle(BUNDLE)

fun message(
    @PropertyKey(resourceBundle = BUNDLE) key: String,
    vararg params: Any
): String = Messages.getMessage(key, *params)
