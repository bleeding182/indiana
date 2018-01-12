package com.davidmedenjak.indiana.features.builds;

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.api.BitriseApi
import com.davidmedenjak.indiana.base.BaseActivity
import com.davidmedenjak.indiana.features.entertoken.EnterTokenActivity
import com.davidmedenjak.indiana.features.entertoken.UserSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_list.*
import javax.inject.Inject

class BuildActivity : BaseActivity() {

    @Inject lateinit var settings: UserSettings
    @Inject lateinit var api: BitriseApi

    @Inject lateinit var adapter: BuildAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (settings.apiToken.isNullOrBlank()) {
            startActivity(Intent(this, EnterTokenActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_list)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val appSlug = intent.getStringExtra(EXTRA_APP_SLUG)

        adapter.projectSlug = appSlug

        api.fetchAppBuilds(appSlug)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    adapter.builds = it.data
                },
                        { Log.wtf("Project", "failed", it) })
    }

    companion object {
        private const val EXTRA_APP_SLUG = "app_slug"

        fun newIntent(context: Context, slug: String) = Intent(context, BuildActivity::class.java).apply {
            putExtra(EXTRA_APP_SLUG, slug)
        }
    }

}

