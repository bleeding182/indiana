package com.davidmedenjak.indiana.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

class IndianaIssueRegistry : IssueRegistry() {
    override val issues = listOf(Material3ImportAliasDetector.ISSUE)
    override val api = CURRENT_API
    override val vendor = Vendor(vendorName = "Indiana")
}
