/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.caches.resolve

import org.jetbrains.kotlin.descriptors.ClassDescriptorWithResolutionScopes
import org.jetbrains.kotlin.idea.project.ResolveElementCache
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.QualifiedExpressionResolver
import org.jetbrains.kotlin.resolve.bindingContextUtil.getDataFlowInfo
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.resolve.lazy.ResolveSession
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.resolve.scopes.utils.addImportingScopes
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.expressions.ExpressionTypingServices
import org.jetbrains.kotlin.types.expressions.PreliminaryDeclarationVisitor
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import javax.inject.Inject

public class CodeFragmentAnalyzer(
        private val resolveSession: ResolveSession,
        private val qualifierResolver: QualifiedExpressionResolver,
        private val expressionTypingServices: ExpressionTypingServices
) {

    // component dependency cycle
    public var resolveElementCache: ResolveElementCache? = null
        @Inject set

    public fun analyzeCodeFragment(codeFragment: KtCodeFragment, trace: BindingTrace, bodyResolveMode: BodyResolveMode) {
        val codeFragmentExpression = codeFragment.getContentElement()
        if (codeFragmentExpression !is KtExpression) return

        val (scopeForContextElement, dataFlowInfo) = getScopeAndDataFlowForAnalyzeFragment(codeFragment) {
            resolveElementCache!!.resolveToElement(it, bodyResolveMode)
        } ?: return

        PreliminaryDeclarationVisitor.createForExpression(codeFragmentExpression, trace)
        expressionTypingServices.getTypeInfo(
                scopeForContextElement,
                codeFragmentExpression,
                TypeUtils.NO_EXPECTED_TYPE,
                dataFlowInfo,
                trace,
                false
        )
    }

    //TODO: this code should be moved into debugger which should set correct context for its code fragment
    private fun KtExpression.correctContextForExpression(): KtExpression {
        return when (this) {
                   is KtProperty -> this.getDelegateExpressionOrInitializer()
                   is KtFunctionLiteral -> this.getBodyExpression()?.getStatements()?.lastOrNull()
                   is KtDeclarationWithBody -> this.getBodyExpression()
                   is KtBlockExpression -> this.getStatements().lastOrNull()
                   else -> {
                       val previousSibling = this.siblings(forward = false, withItself = false).firstIsInstanceOrNull<KtExpression>()
                       if (previousSibling != null) return previousSibling
                       for (parent in this.parents) {
                           if (parent is KtWhenEntry || parent is KtIfExpression || parent is KtBlockExpression) return this
                           if (parent is KtExpression) return parent
                       }
                       null
                   }
               } ?: this
    }

    private fun getScopeAndDataFlowForAnalyzeFragment(
            codeFragment: KtCodeFragment,
            resolveToElement: (KtElement) -> BindingContext
    ): Pair<LexicalScope, DataFlowInfo>? {
        val context = codeFragment.getContext()
        if (context !is KtExpression) return null

        val scopeForContextElement: LexicalScope?
        val dataFlowInfo: DataFlowInfo

        when (context) {
            is KtPrimaryConstructor -> {
                val descriptor = resolveSession.getClassDescriptor(context.getContainingClassOrObject(), NoLookupLocation.FROM_IDE) as ClassDescriptorWithResolutionScopes

                scopeForContextElement = descriptor.getScopeForInitializerResolution()
                dataFlowInfo = DataFlowInfo.EMPTY
            }
            is KtSecondaryConstructor -> {
                val correctedContext = context.getDelegationCall().calleeExpression!!

                val contextForElement = resolveToElement(correctedContext)

                scopeForContextElement = contextForElement[BindingContext.LEXICAL_SCOPE, correctedContext]
                dataFlowInfo = DataFlowInfo.EMPTY
            }
            is KtClassOrObject -> {
                val descriptor = resolveSession.getClassDescriptor(context, NoLookupLocation.FROM_IDE) as ClassDescriptorWithResolutionScopes

                scopeForContextElement = descriptor.getScopeForMemberDeclarationResolution()
                dataFlowInfo = DataFlowInfo.EMPTY
            }
            is KtExpression -> {
                val correctedContext = context.correctContextForExpression()

                val contextForElement = resolveToElement(correctedContext)

                scopeForContextElement = contextForElement[BindingContext.LEXICAL_SCOPE, correctedContext]
                dataFlowInfo = contextForElement.getDataFlowInfo(correctedContext)
            }
            is KtFile -> {
                scopeForContextElement = resolveSession.getFileScopeProvider().getFileResolutionScope(context)
                dataFlowInfo = DataFlowInfo.EMPTY
            }
            else -> return null
        }

        if (scopeForContextElement == null) return null

        val importList = codeFragment.importsAsImportList()
        if (importList == null || importList.imports.isEmpty()) {
            return scopeForContextElement to dataFlowInfo
        }

        val importScopes = importList.imports.mapNotNull {
            qualifierResolver.processImportReference(it, resolveSession.moduleDescriptor, resolveSession.trace, null)
        }

        return scopeForContextElement.addImportingScopes(importScopes) to dataFlowInfo
    }
}
