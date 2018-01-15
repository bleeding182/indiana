package com.davidmedenjak.indiana.features.builds

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.item_build.view.*

class BuildViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val title: TextView = view.title
    val info: TextView = view.info
    val progress: ProgressBar = view.progress
    val status: ImageView = view.status

}