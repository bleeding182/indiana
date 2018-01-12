package com.davidmedenjak.indiana.features.projects

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.davidmedenjak.indiana.api.Project
import com.davidmedenjak.indiana.di.PerActivity
import com.davidmedenjak.indiana.features.builds.BuildActivity
import javax.inject.Inject

@PerActivity
class ProjectAdapter @Inject constructor() : RecyclerView.Adapter<ProjectViewHolder>() {
    var projects: List<Project> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = projects.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.text.text = project.title

        holder.itemView.setOnClickListener {
            it.context.startActivity(BuildActivity.newIntent(it.context, project.slug))
        }
    }

}