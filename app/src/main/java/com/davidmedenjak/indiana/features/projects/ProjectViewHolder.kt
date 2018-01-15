package com.davidmedenjak.indiana.features.projects

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.item_project.view.*

class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val title: TextView = view.title
    val owner: TextView = view.owner
    val type: TextView = view.type

}