package com.example.bedrockcompanion

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts

class ResourcePackFragment : Fragment() {

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { importToMinecraft(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_resource_packs, container, false)
        view.findViewById<Button>(R.id.actionButton).apply {
            text = "Import .mcpack / .mcworld"
            setOnClickListener { pickFileLauncher.launch("*/*") }
        }
        return view
    }

    // Hands the file off to Minecraft using the standard Android share intent --
    // Bedrock registers itself as a handler for .mcpack/.mcworld/.mctemplate via
    // its own manifest, so this is just using Android's normal file-association system.
    private fun importToMinecraft(uri: android.net.Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage(VersionChecker.MINECRAFT_PACKAGE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent.createChooser(intent, "Import to Minecraft"))
        }
    }
}
