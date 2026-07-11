package com.example.bedrockcompanion

import android.content.pm.PackageManager
import android.content.Context

object VersionChecker {
    const val MINECRAFT_PACKAGE = "com.mojang.minecraftpe"

    fun getInstalledVersion(context: Context): String? {
        return try {
            context.packageManager.getPackageInfo(MINECRAFT_PACKAGE, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}
