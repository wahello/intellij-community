// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.fir.fe10.binding

import com.intellij.lang.ASTNode
import org.jetbrains.kotlin.analysis.api.calls.*
import org.jetbrains.kotlin.analysis.api.diagnostics.KtDiagnostic
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.idea.fir.fe10.Fe10WrapperContext
import org.jetbrains.kotlin.idea.fir.fe10.toDeclarationDescriptor
import org.jetbrains.kotlin.idea.fir.fe10.toKotlinType
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.CallMaker
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.Receiver
import org.jetbrains.kotlin.types.expressions.ControlStructureTypingUtils
import org.jetbrains.kotlin.utils.addToStdlib.safeAs


class CallAndResolverCallWrappers(bindingContext: KtSymbolBasedBindingContext) {
    private val context = bindingContext.context

    init {
        bindingContext.registerGetterByKey(BindingContext.CALL, this::getCall)
        bindingContext.registerGetterByKey(BindingContext.RESOLVED_CALL, this::getResolvedCall)
        bindingContext.registerGetterByKey(BindingContext.REFERENCE_TARGET, this::getReferenceTarget)
    }

    private fun getCall(element: KtElement): Call? {
        val call = createCall(element) ?: return null

        /**
         * In FE10 [org.jetbrains.kotlin.resolve.calls.tasks.TracingStrategyImpl.bindCall] happening to the calleeExpression
         */
        check(call.calleeExpression == element) {
            "${call.calleeExpression} != $element"
        }
        return call
    }

    private fun createCall(element: KtElement): Call? {
        val parent = element.parent
        if (parent is KtCallElement) {
            val callParent = parent.parent
            val callOperationNode: ASTNode?
            val receiver: Receiver?
            if (callParent is KtQualifiedExpression) {
                callOperationNode = callParent.operationTokenNode
                receiver = callParent.receiverExpression.toExpressionReceiverValue(context)
            } else {
                callOperationNode = null
                receiver = null
            }

            return CallMaker.makeCall(receiver, callOperationNode, parent)
        }

        if (element is KtSimpleNameExpression && element !is KtOperationReferenceExpression) {
            if (parent is KtQualifiedExpression) {
                val receiver = parent.receiverExpression.toExpressionReceiverValue(context)
                return CallMaker.makePropertyCall(receiver, parent.operationTokenNode, element)
            }

            return CallMaker.makePropertyCall(null, null, element)
        }

        when (parent) {
            is KtBinaryExpression -> {
                val receiver = parent.left?.toExpressionReceiverValue(context) ?: context.errorHandling()
                return CallMaker.makeCall(receiver, parent)
            }
            is KtUnaryExpression -> {
                if (element is KtOperationReferenceExpression && element.getReferencedNameElementType() == KtTokens.EXCLEXCL) {
                    return ControlStructureTypingUtils.createCallForSpecialConstruction(parent, element, listOf(parent.baseExpression))
                }

                val receiver = parent.baseExpression?.toExpressionReceiverValue(context) ?: context.errorHandling()
                return CallMaker.makeCall(receiver, parent)
            }
        }

        // todo support array get/set calls
        return null
    }

    internal fun KtExpression.toExpressionReceiverValue(context: Fe10WrapperContext): ExpressionReceiver {
        val ktType = context.withAnalysisSession {
            this@toExpressionReceiverValue.getKtType() ?: context.implementationPostponed()
        }

        // TODO: implement THIS_TYPE_FOR_SUPER_EXPRESSION Binding slice
        return ExpressionReceiver.create(this, ktType.toKotlinType(context), context.bindingContext)
    }

    private fun getResolvedCall(call: Call): ResolvedCall<*>? {
        val ktElement = call.calleeExpression ?: call.callElement

        val ktCallInfo = context.withAnalysisSession { ktElement.resolveCall() }
        val diagnostic: KtDiagnostic?
        val ktCall: KtCall = when (ktCallInfo) {
            null -> return null
            is KtSuccessCallInfo -> {
                diagnostic = null
                ktCallInfo.call
            }
            is KtErrorCallInfo -> {
                diagnostic = ktCallInfo.diagnostic
                ktCallInfo.candidateCalls.singleOrNull() ?: return null
            }
        }

        when (ktCall) {
            is KtFunctionCall<*> -> {
                if (ktCall.safeAs<KtSimpleFunctionCall>()?.isImplicitInvoke == true) {
                    context.implementationPostponed("Variable + invoke resolved call")
                }
                return FunctionFe10WrapperResolvedCall(call, ktCall, diagnostic, context)
            }
            is KtVariableAccessCall -> {
                return VariableFe10WrapperResolvedCall(call, ktCall, diagnostic, context)
            }
            is KtCheckNotNullCall -> {
                val kotlinType = context.withAnalysisSession { ktCall.baseExpression.getKtType() }?.toKotlinType(context)
                return Fe10BindingSpecialConstructionResolvedCall(
                    call,
                    kotlinType,
                    context.fe10BindingSpecialConstructionFunctions.EXCL_EXCL,
                    context
                )
            }

            else -> context.implementationPostponed(ktCall.javaClass.canonicalName)
        }
    }

    private fun getReferenceTarget(key: KtReferenceExpression): DeclarationDescriptor? {
        val ktSymbol = context.withAnalysisSession { key.mainReference.resolveToSymbols().singleOrNull() } ?: return null
        return ktSymbol.toDeclarationDescriptor(context)
    }
}
