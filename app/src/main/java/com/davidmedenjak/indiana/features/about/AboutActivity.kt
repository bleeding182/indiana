package com.davidmedenjak.indiana.features.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.base.BaseActivity
import kotlinx.android.synthetic.main.activity_about.*
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity



class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)

        action_privacy_policy.setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }

        action_licenses.setOnClickListener {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }

        action_source.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bleeding182/indiana")))
        }
    }

}