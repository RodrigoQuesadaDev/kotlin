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

package org.jetbrains.kotlin.analyzer

import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.ErrorUtils

public open class AnalysisResult protected constructor(
        public val bindingContext: BindingContext,
        public val moduleDescriptor: ModuleDescriptor,
        public val shouldGenerateCode: Boolean = true
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return (other is AnalysisResult && bindingContext == other.bindingContext &&
                moduleDescriptor == other.moduleDescriptor && shouldGenerateCode == other.shouldGenerateCode)
    }

    override fun hashCode(): Int {
        var result = 17
        result = 29 * result + bindingContext.hashCode()
        result = 29 * result + moduleDescriptor.hashCode()
        result = 29 * result + shouldGenerateCode.hashCode()
        return result
    }

    operator fun component1() = bindingContext

    operator fun component2() = moduleDescriptor

    operator fun component3() = shouldGenerateCode

    public val error: Throwable
        get() = if (this is Error) this.exception else throw IllegalStateException("Should only be called for error analysis result")

    public fun isError(): Boolean = this is Error

    public fun throwIfError() {
        if (isError()) {
            throw IllegalStateException("failed to analyze: " + error, error)
        }
    }

    private class Error(bindingContext: BindingContext, val exception: Throwable) : AnalysisResult(bindingContext, ErrorUtils.getErrorModule())

    companion object {
        public val EMPTY: AnalysisResult = success(BindingContext.EMPTY, ErrorUtils.getErrorModule())

        @JvmStatic
        public fun success(bindingContext: BindingContext, module: ModuleDescriptor): AnalysisResult {
            return AnalysisResult(bindingContext, module, true)
        }

        @JvmStatic
        public fun success(bindingContext: BindingContext, module: ModuleDescriptor, shouldGenerateCode: Boolean): AnalysisResult {
            return AnalysisResult(bindingContext, module, shouldGenerateCode)
        }

        @JvmStatic
        public fun error(bindingContext: BindingContext, error: Throwable): AnalysisResult {
            return Error(bindingContext, error)
        }
    }
}
