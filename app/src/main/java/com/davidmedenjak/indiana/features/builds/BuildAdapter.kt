package com.davidmedenjak.indiana.features.builds

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.davidmedenjak.indiana.api.Build
import com.davidmedenjak.indiana.di.PerActivity
import com.davidmedenjak.indiana.features.artifacts.ArtifactActivity
import javax.inject.Inject

@PerActivity
class BuildAdapter @Inject constructor() : RecyclerView.Adapter<BuildViewHolder>() {
    var projectSlug: String = ""
    var builds: List<Build> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = builds.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuildViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        return BuildViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuildViewHolder, position: Int) {
        val build = builds[position]
        holder.text.text = "${build.buildNumber} ${build.branch}"

        holder.itemView.setOnClickListener {
            it.context.startActivity(ArtifactActivity.newIntent(it.context, projectSlug, build.slug))
        }
    }

}