package com.davidmedenjak.indiana.features.projects

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.api.BitriseApi
import com.davidmedenjak.indiana.base.BaseActivity
import com.davidmedenjak.indiana.features.about.AboutActivity
import com.davidmedenjak.indiana.features.entertoken.EnterTokenActivity
import com.davidmedenjak.indiana.features.entertoken.UserSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_list.*
import javax.inject.Inject

class ProjectActivity : BaseActivity() {

    @Inject lateinit var settings: UserSettings
    @Inject lateinit var api: BitriseApi

    @Inject lateinit var adapter: ProjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (settings.apiToken.isNullOrBlank()) {
            showRequestToken()
            return
        }

        setContentView(R.layout.activity_list)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        swipe_refresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimary))
        swipe_refresh.setOnRefreshListener {
            loadData()
        }
        loadData()
    }

    private fun loadData() {
        api.fetchMyApps()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    adapter.projects = it.data
                }, {
                    Log.wtf("Project", "failed", it)
                    Toast.makeText(this, "Loading projects failed", Toast.LENGTH_SHORT).show()
                })
    }

    private fun showRequestToken() {
        startActivity(Intent(this, EnterTokenActivity::class.java))
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        menuInflater.inflate(R.menu.menu_about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear_token -> onClearTokenClicked()
            R.id.menu_about -> startActivity(Intent(this, AboutActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onClearTokenClicked() {
        AlertDialog.Builder(this)
                .setTitle("Clear API Token")
                .setMessage("Your token will be deleted and you need to add a new one to use this app.")
                .setPositiveButton(android.R.string.ok, { _, _ ->
                    settings.apiToken = null
                    showRequestToken()
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

}

