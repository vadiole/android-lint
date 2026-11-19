package vadiole.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestFiles.manifest
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class DelegateDetectorTest {

    private fun check(source: String) = lint()
        .files(manifest().minSdk(23), kotlin(source).indented())
        .issues(DelegateDetector.API_TYPE, DelegateDetector.IMPLICIT_LAZY)
        .allowMissingSdk(false)
        .run()

    @Test
    fun delegateTypedWithNewerClass() {
        check(
            """
            package test

            import android.os.Build
            import android.os.VibrationAttributes

            class Slider {
                private val touchAttributes by lazy(LazyThreadSafetyMode.NONE) {
                    if (Build.VERSION.SDK_INT >= 33) VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH) else null
                }

                fun local() {
                    val attributes by lazy(LazyThreadSafetyMode.NONE) {
                        if (Build.VERSION.SDK_INT >= 33) VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH) else null
                    }
                }
            }
            """
        ).expectErrorCount(2).expectContains("ApiTypeDelegate").expectContains("android.os.VibrationAttributes")
    }

    @Test
    fun topLevelDelegateTypedWithNewerClass() {
        check(
            """
            package test

            import android.os.Build
            import android.os.VibrationAttributes

            var touchAttributes: VibrationAttributes? by kotlin.properties.Delegates.observable(null) { _, _, _ -> }
            """
        ).expectErrorCount(1).expectContains("ApiTypeDelegate")
    }

    @Test
    fun plainFieldWithNewerClass() {
        check(
            """
            package test

            import android.os.Build
            import android.os.VibrationAttributes

            class Slider {
                private val touchAttributes = if (Build.VERSION.SDK_INT >= 33) {
                    VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH)
                } else null
            }
            """
        ).expectClean()
    }

    @Test
    fun delegateTypedWithOldClass() {
        check(
            """
            package test

            import android.graphics.Paint

            class Slider {
                private val paint by lazy(LazyThreadSafetyMode.NONE) { Paint() }
            }
            """
        ).expectClean()
    }

    @Test
    fun lazyWithoutMode() {
        check(
            """
            package test

            import android.graphics.Paint

            class Slider {
                private val paint by lazy { Paint() }

                fun local(): Int {
                    val size by lazy { 4 }
                    return size
                }
            }
            """
        ).expectWarningCount(2).expectContains("ImplicitLazy")
    }

    @Test
    fun otherDelegates() {
        check(
            """
            package test

            import kotlin.properties.Delegates

            class Slider {
                private var value by Delegates.notNull<Int>()
            }
            """
        ).expectClean()
    }
}
