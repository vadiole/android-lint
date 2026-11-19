package vadiole.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.ApiConstraint
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.minSdkLessThan
import com.android.tools.lint.checks.ApiLookup
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypes
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UField
import org.jetbrains.uast.ULocalVariable
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.toUElementOfType

class DelegateDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UField::class.java, ULocalVariable::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitField(node: UField) = check(node)
        override fun visitLocalVariable(node: ULocalVariable) = check(node)

        private fun check(node: UVariable) {
            val property = node.sourcePsi as? KtProperty ?: return
            val delegate = KtPsiUtil.deparenthesize(property.delegateExpression) ?: return

            val type = (propertyType(node) as? PsiClassType)?.resolve()?.qualifiedName
            if (type != null) {
                val since = classSince(context, type)
                if (since > 1) {
                    val incident = Incident(
                        API_TYPE, node as UElement, context.getLocation(delegate),
                        "The delegate getter casts to `$type` (API $since) on every read and crashes below it; use a plain field",
                    )
                    context.report(incident, minSdkLessThan(since))
                }
            }

            val selector = (delegate as? KtQualifiedExpression)?.selectorExpression ?: delegate
            val call = selector.toUElementOfType<UCallExpression>() ?: return
            val method = call.resolve() ?: return
            if (method.name == "lazy" && call.valueArgumentCount == 1 &&
                method.containingClass?.qualifiedName?.startsWith("kotlin.LazyKt") == true
            ) {
                context.report(
                    IMPLICIT_LAZY, node as UElement, context.getLocation(selector),
                    "Use a plain or nullable field, or pass a `LazyThreadSafetyMode` to state why this is deferred",
                )
            }
        }
    }

    private fun propertyType(node: UVariable): PsiType {
        val owner = (node as? UField)?.uastParent as? UClass ?: return node.type
        val getter = owner.methods.firstOrNull { it.sourcePsi === node.sourcePsi && it.returnType != PsiTypes.voidType() }
        return getter?.returnType ?: node.type
    }

    private fun classSince(context: JavaContext, type: String): Int {
        val lookup = ApiLookup.get(context.client, context.project.buildTarget) ?: return -1
        val versions = lookup.getClassVersions(type)
        return if (versions == ApiConstraint.UNKNOWN) -1 else versions.min()
    }

    companion object {
        private val implementation = Implementation(DelegateDetector::class.java, Scope.JAVA_FILE_SCOPE)

        val API_TYPE = Issue.create(
            id = "ApiTypeDelegate",
            briefDescription = "Delegated property typed with a class newer than minSdk",
            explanation = "A delegated property reads its value through a generic getter, so the compiler casts the result " +
                "to the property type on every read, even when the value is null. When that type is a class added after " +
                "minSdk, the read throws `NoClassDefFoundError` on older devices, whatever version check the initializer has. " +
                "A plain field of the same type needs no cast.",
            category = Category.CORRECTNESS,
            priority = 9,
            severity = Severity.ERROR,
            implementation = implementation,
        )

        val IMPLICIT_LAZY = Issue.create(
            id = "ImplicitLazy",
            briefDescription = "`lazy` without a thread-safety mode",
            explanation = "`by lazy` allocates a wrapper and a lambda and takes a lock on every first read. Prefer a plain field, " +
                "or a nullable field with a get-or-create accessor. When the value is expensive and may never be needed, " +
                "pass `LazyThreadSafetyMode.NONE` for main-thread use or `SYNCHRONIZED` when threads share it.",
            category = Category.PERFORMANCE,
            priority = 5,
            severity = Severity.WARNING,
            implementation = implementation,
        )
    }
}
