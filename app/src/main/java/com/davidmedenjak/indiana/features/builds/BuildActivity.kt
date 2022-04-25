package com.davidmedenjak.indiana.features.builds

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.api.BitriseApi
import com.davidmedenjak.indiana.base.BaseActivity
import com.davidmedenjak.indiana.features.entertoken.EnterTokenActivity
import com.davidmedenjak.indiana.features.entertoken.UserSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_list.*
import javax.inject.Inject

class BuildActivity : BaseActivity() {

    @Inject
    lateinit var settings: UserSettings
    @Inject
    lateinit var api: BitriseApi

    @Inject
    lateinit var adapter: BuildAdapter

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

        val appSlug = intent.getStringExtra(EXTRA_APP_SLUG)!!
        title = intent.getStringExtra(EXTRA_TITLE)!!

        adapter.projectSlug = appSlug
        adapter.projectTitle = intent.getStringExtra(EXTRA_TITLE)!!

        swipe_refresh.setColorSchemeColors(
            ContextCompat.getColor(this, R.color.colorAccent),
            ContextCompat.getColor(this, R.color.colorPrimary)
        )
        swipe_refresh.setOnRefreshListener {
            loadData(appSlug)
        }
        loadData(appSlug)
    }

    private fun loadData(appSlug: String) {
        api.fetchAppBuilds(appSlug)
            .doOnSubscribe { swipe_refresh.isRefreshing = true }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { swipe_refresh.isRefreshing = false }
            .subscribe({
                adapter.builds = it.data
            }, {
                Log.wtf("Build", "failed", it)
                Toast.makeText(this, "Loading builds failed", Toast.LENGTH_SHORT).show()
            })
    }

    companion object {
        private const val EXTRA_APP_SLUG = "app_slug"
        private const val EXTRA_TITLE = "title"

        fun newIntent(context: Context, slug: String, title: String): Intent =
            Intent(context, BuildActivity::class.java)
                .putExtra(EXTRA_APP_SLUG, slug)
                .putExtra(EXTRA_TITLE, title)
    }

}

