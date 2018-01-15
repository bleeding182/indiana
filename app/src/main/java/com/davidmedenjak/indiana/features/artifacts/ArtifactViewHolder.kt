package com.davidmedenjak.indiana.features.artifacts

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.item_artifact.view.*

class ArtifactViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val title: TextView = view.title

}