package com.davidmedenjak.indiana.features.entertoken

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.api.BitriseApi
import com.davidmedenjak.indiana.api.User
import com.davidmedenjak.indiana.base.BaseActivity
import com.davidmedenjak.indiana.features.projects.ProjectActivity
import dagger.Reusable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_enter_token.*
import javax.inject.Inject

class EnterTokenActivity : BaseActivity() {

    @Inject lateinit var api: BitriseApi
    @Inject lateinit var userSettings: UserSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_enter_token)

        action_link_settings.setOnClickListener {
            val uri = Uri.parse("https://www.bitrise.io/me/profile#/security")
            startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        api_key.setOnEditorActionListener { textView, i, keyEvent ->
            val token = textView.text.toString()
            api.fetchMe("token $token")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        onTokenSuccess(it.data, token)
                    }, {
                        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                    })
            true
        }
    }

    fun onTokenSuccess(user: User, token: String) {
        Toast.makeText(this, "Hi ${user.username}", Toast.LENGTH_SHORT).show()
        userSettings.apiToken = token

        startActivity(Intent(this, ProjectActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
    }


}

@Reusable
class UserSettings @Inject constructor(val preferences: SharedPreferences) {

    var apiToken
        get() = preferences.getString(PREF_API_TOKEN, "")
        set(value) = preferences.edit().putString(PREF_API_TOKEN, value).apply()

    companion object {
        private const val PREF_API_TOKEN = "api_token"
    }

}
