package com.davidmedenjak.indiana.features.artifacts

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.api.BitriseApi
import com.davidmedenjak.indiana.base.BaseActivity
import com.davidmedenjak.indiana.features.entertoken.EnterTokenActivity
import com.davidmedenjak.indiana.features.entertoken.UserSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_artifacts.*
import javax.inject.Inject

class ArtifactActivity : BaseActivity() {

    @Inject
    lateinit var settings: UserSettings
    @Inject
    lateinit var api: BitriseApi

    @Inject
    lateinit var adapter: ArtifactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (settings.apiToken.isNullOrBlank()) {
            startActivity(Intent(this, EnterTokenActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_artifacts)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val projectSlug = intent.getStringExtra(EXTRA_APP_SLUG)
        val buildSlug = intent.getStringExtra(EXTRA_BUILD_SLUG)

        adapter.projectSlug = projectSlug
        adapter.buildSlug = buildSlug

        api.fetchBuildArtifacts(projectSlug, buildSlug)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                adapter.artifacts = it.data
            },
                { Log.wtf("Project", "failed", it) })
    }

    override fun onStart() {
        super.onStart()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            unknown_sources_layout.visibility = View.VISIBLE
            action_unknown_sources.setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        } else {
            unknown_sources_layout.visibility = View.GONE
        }
    }

    companion object {
        private const val EXTRA_APP_SLUG = "app_slug"
        private const val EXTRA_BUILD_SLUG = "build_slug"

        fun newIntent(context: Context, appSlug: String, buildSlug: String) =
            Intent(context, ArtifactActivity::class.java).apply {
                putExtra(EXTRA_APP_SLUG, appSlug)
                putExtra(EXTRA_BUILD_SLUG, buildSlug)
            }
    }

}

