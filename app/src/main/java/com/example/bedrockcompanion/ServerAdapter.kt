package com.example.bedrockcompanion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ServerAdapter(
    private val servers: MutableList<Server>,
    private val onConnect: (Server) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {

    class ServerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.serverName)
        val address: TextView = view.findViewById(R.id.serverAddress)
        val connectBtn: View = view.findViewById(R.id.connectButton)
        val deleteBtn: View = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_server, parent, false)
        return ServerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val server = servers[position]
        holder.name.text = server.name
        holder.address.text = "${server.address}:${server.port}"
        holder.connectBtn.setOnClickListener { onConnect(server) }
        holder.deleteBtn.setOnClickListener { onDelete(position) }
    }

    override fun getItemCount() = servers.size
}
