package vadiole.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

class Registry : IssueRegistry() {
    override val issues = listOf(DelegateDetector.API_TYPE, DelegateDetector.IMPLICIT_LAZY)
    override val api = CURRENT_API
    override val minApi = 14
    override val vendor = Vendor(
        vendorName = "vadiole",
        identifier = "vadiole:android-lint",
        feedbackUrl = "https://github.com/vadiole/android-lint/issues",
    )
}
