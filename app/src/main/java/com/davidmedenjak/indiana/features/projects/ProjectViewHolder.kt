package com.davidmedenjak.indiana.features.projects

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_project.view.*

class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val title: TextView = view.title
    val owner: TextView = view.owner
    val type: TextView = view.type

}