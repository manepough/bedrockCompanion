package com.example.bedrockcompanion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class VersionCheckerFragment : Fragment() {

    private val TARGET_VERSION = "1.26.32.02"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_resource_packs, container, false) // reuse simple layout
        val statusText = view.findViewById<TextView>(R.id.statusText)
        val actionButton = view.findViewById<Button>(R.id.actionButton)

        val installed = VersionChecker.getInstalledVersion(requireContext())
        when {
            installed == null -> {
                statusText.text = "Minecraft Bedrock is not installed."
                actionButton.text = "Open Play Store"
                actionButton.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${VersionChecker.MINECRAFT_PACKAGE}")))
                }
            }
            installed != TARGET_VERSION -> {
                statusText.text = "Installed: $installed\nTarget: $TARGET_VERSION\nVersions differ."
                actionButton.text = "Launch Anyway"
                actionButton.setOnClickListener { launchGame() }
            }
            else -> {
                statusText.text = "Version $TARGET_VERSION matches. Ready to launch."
                actionButton.text = "Launch"
                actionButton.setOnClickListener { launchGame() }
            }
        }
        return view
    }

    private fun launchGame() {
        val intent = requireContext().packageManager
            .getLaunchIntentForPackage(VersionChecker.MINECRAFT_PACKAGE)
        if (intent != null) startActivity(intent)
    }
}
