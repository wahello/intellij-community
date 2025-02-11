// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.kotlin.idea.intentions

import com.intellij.codeInspection.CleanupLocalInspectionTool
import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.idea.KotlinBundle
import org.jetbrains.kotlin.idea.inspections.IntentionBasedInspection
import org.jetbrains.kotlin.idea.util.isExpectDeclaration
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.psiUtil.containingClass

@Suppress("DEPRECATION")
class RemoveEmptyPrimaryConstructorInspection : IntentionBasedInspection<KtPrimaryConstructor>(
    RemoveEmptyPrimaryConstructorIntention::class
), CleanupLocalInspectionTool

class RemoveEmptyPrimaryConstructorIntention : SelfTargetingOffsetIndependentIntention<KtPrimaryConstructor>(
    KtPrimaryConstructor::class.java,
    KotlinBundle.lazyMessage("remove.empty.primary.constructor")
) {

    override fun applyTo(element: KtPrimaryConstructor, editor: Editor?) = element.delete()

    override fun isApplicableTo(element: KtPrimaryConstructor) = when {
        element.valueParameters.isNotEmpty() -> false
        element.annotations.isNotEmpty() -> false
        element.modifierList?.text?.isBlank() == false -> false
        element.containingClass()?.secondaryConstructors?.isNotEmpty() == true -> false
        element.isExpectDeclaration() -> false
        else -> true
    }
}
