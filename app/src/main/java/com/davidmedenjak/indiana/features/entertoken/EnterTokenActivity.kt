package com.davidmedenjak.indiana.features.entertoken

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.api.BitriseApi
import com.davidmedenjak.indiana.api.User
import com.davidmedenjak.indiana.base.BaseActivity
import com.davidmedenjak.indiana.features.about.AboutActivity
import com.davidmedenjak.indiana.features.about.PrivacyActivity
import com.davidmedenjak.indiana.features.projects.ProjectActivity
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_enter_token.*
import javax.inject.Inject

class EnterTokenActivity : BaseActivity() {

    @Inject lateinit var api: BitriseApi
    @Inject lateinit var userSettings: UserSettings
    @Inject lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_enter_token)

        if (userSettings.apiToken.isNullOrBlank()) {
            analytics.setUserProperty("has_token", null)
        }

        val data = intent.data
        if (data != null) {
            val token = data.lastPathSegment
            api_key.setText(token)
            tryToken(token)
        }

        action_link_settings.setOnClickListener {
            val uri = Uri.parse("https://www.bitrise.io/me/profile#/security")
            startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        action_privacy_policy.setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }

        api_key.setOnEditorActionListener { textView, i, keyEvent ->
            val token = textView.text.toString()
            tryToken(token)
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_about -> startActivity(Intent(this, AboutActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun tryToken(token: String) {
        api.fetchMe("token $token")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onTokenSuccess(it.data, token)
                }, {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                })
    }

    fun onTokenSuccess(user: User, token: String) {
        Toast.makeText(this, "Hi ${user.username}", Toast.LENGTH_SHORT).show()
        userSettings.apiToken = token
        analytics.setUserProperty("has_token", "bitrise")

        startActivity(Intent(this, ProjectActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
    }
}

