package com.davidmedenjak.indiana.features.builds

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.api.Build
import com.davidmedenjak.indiana.di.PerActivity
import com.davidmedenjak.indiana.features.artifacts.ArtifactActivity
import javax.inject.Inject

@PerActivity
class BuildAdapter @Inject constructor() : RecyclerView.Adapter<BuildViewHolder>() {
    var projectSlug: String = ""
    var projectTitle: String = ""
    var builds: List<Build> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = builds.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuildViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_build, parent, false)
        return BuildViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuildViewHolder, position: Int) {
        val build = builds[position]
        val context = holder.itemView.context

        val formattedStartTime = DateUtils.getRelativeDateTimeString(
            context,
            build.triggeredAt.time,
            DateUtils.MINUTE_IN_MILLIS, DateUtils.HOUR_IN_MILLIS,
            0
        )
        holder.title.text = "${build.buildNumber} ${build.branch}"
        holder.info.text = "Triggered $formattedStartTime"
        holder.workflow.text = build.workflow
        holder.status.setImageDrawable(getBuildDrawable(context, build))

        holder.progress.visibility = if (build.status == 0 && !build.isOnHold) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val info = "${build.buildNumber} ${build.branch} (${build.workflow})"
            it.context.startActivity(
                ArtifactActivity.newIntent(
                    it.context, projectSlug, build.slug, projectTitle, info
                )
            )
        }
    }

    private fun getBuildDrawable(context: Context, build: Build): Drawable {
        val resourceId = when (build.status) {
            0 -> R.drawable.ic_hourglass_empty_black_24dp
            1 -> R.drawable.ic_check_black_24dp
            2 -> R.drawable.ic_error_black_24dp
            3 -> R.drawable.ic_cancel_black_24dp
            else -> 0
        }
        return VectorDrawableCompat.create(context.resources, resourceId, context.theme)!!
    }

}