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

package org.jetbrains.kotlin.idea.intentions.branchedTransformations

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.lastBlockStatementOrThis
import org.jetbrains.kotlin.utils.addToStdlib.check

object BranchedFoldingUtils {
    public fun getFoldableBranchedAssignment(branch: KtExpression?): KtBinaryExpression? {
        fun checkAssignment(expression: KtBinaryExpression): Boolean {
            if (expression.getOperationToken() !in KtTokens.ALL_ASSIGNMENTS) return false

            val left = expression.getLeft() as? KtNameReferenceExpression ?: return false
            if (expression.getRight() == null) return false

            val parent = expression.getParent()
            if (parent is KtBlockExpression) {
                return !KtPsiUtil.checkVariableDeclarationInBlock(parent, left.getText())
            }

            return true
        }
        return (branch?.lastBlockStatementOrThis() as? KtBinaryExpression)?.check(::checkAssignment)
    }

    public fun getFoldableBranchedReturn(branch: KtExpression?): KtReturnExpression? {
        return (branch?.lastBlockStatementOrThis() as? KtReturnExpression)?.check { it.getReturnedExpression() != null }
    }

    public fun checkAssignmentsMatch(a1: KtBinaryExpression, a2: KtBinaryExpression): Boolean {
        return a1.getLeft()?.getText() == a2.getLeft()?.getText() && a1.getOperationToken() == a2.getOperationToken()
    }
}
