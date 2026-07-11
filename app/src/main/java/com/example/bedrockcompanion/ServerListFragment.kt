package com.example.bedrockcompanion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject

class ServerListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServerAdapter
    private val servers = mutableListOf<Server>()
    private val PREFS_NAME = "bedrock_companion_prefs"
    private val KEY_SERVERS = "saved_servers"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_server_list, container, false)

        recyclerView = view.findViewById(R.id.serverRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadServers()

        adapter = ServerAdapter(
            servers,
            onConnect = { server -> connectToServer(server) },
            onDelete = { index -> removeServer(index) }
        )
        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.addServerButton).setOnClickListener {
            showAddServerDialog()
        }

        return view
    }

    // Uses Bedrock's official minecraft:// deep link -- this hands off to Mojang's
    // own client-side server-join flow, it does not touch the game's process or memory.
    private fun connectToServer(server: Server) {
        val uri = Uri.parse("minecraft://?addExternalServer=${server.name}|${server.address}:${server.port}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.mojang.minecraftpe")
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Couldn't open Minecraft. Is it installed?", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddServerDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_server, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val addressInput = dialogView.findViewById<EditText>(R.id.addressInput)
        val portInput = dialogView.findViewById<EditText>(R.id.portInput)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Server")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().ifBlank { "Unnamed Server" }
                val address = addressInput.text.toString()
                val port = portInput.text.toString().toIntOrNull() ?: 19132
                if (address.isNotBlank()) {
                    servers.add(Server(name, address, port))
                    saveServers()
                    adapter.notifyItemInserted(servers.size - 1)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun removeServer(index: Int) {
        servers.removeAt(index)
        saveServers()
        adapter.notifyItemRemoved(index)
    }

    private fun saveServers() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, 0)
        val array = JSONArray()
        for (s in servers) {
            val obj = JSONObject()
            obj.put("name", s.name)
            obj.put("address", s.address)
            obj.put("port", s.port)
            array.put(obj)
        }
        prefs.edit().putString(KEY_SERVERS, array.toString()).apply()
    }

    private fun loadServers() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, 0)
        val raw = prefs.getString(KEY_SERVERS, null) ?: return
        val array = JSONArray(raw)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            servers.add(Server(obj.getString("name"), obj.getString("address"), obj.getInt("port")))
        }
    }
}
