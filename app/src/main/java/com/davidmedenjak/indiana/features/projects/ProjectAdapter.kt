package com.davidmedenjak.indiana.features.projects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.davidmedenjak.indiana.R
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
        val view = inflater.inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.title.text = project.title
        holder.owner.text = project.repoOwner
        holder.type.text = project.projectType

        holder.itemView.setOnClickListener {
            it.context.startActivity(BuildActivity.newIntent(it.context, project.slug))
        }
    }

}