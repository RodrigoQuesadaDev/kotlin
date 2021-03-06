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

@file:JvmName("TypeMappingUtil")
package org.jetbrains.kotlin.codegen.state

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.firstOverridden
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.parentsWithSelf
import org.jetbrains.kotlin.resolve.descriptorUtil.propertyIfAccessor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.Variance

fun KotlinType.isMostPreciseContravariantArgument(parameter: TypeParameterDescriptor): Boolean =
        // TODO: probably class upper bound should be used
        KotlinBuiltIns.isAnyOrNullableAny(this)

fun KotlinType.isMostPreciseCovariantArgument(): Boolean = !canHaveSubtypesIgnoringNullability()

private fun KotlinType.canHaveSubtypesIgnoringNullability(): Boolean {
    val constructor = constructor
    val descriptor = constructor.declarationDescriptor

    when (descriptor) {
        is TypeParameterDescriptor -> return true
        is ClassDescriptor -> if (descriptor.modality.isOverridable) return true
    }

    for ((parameter, argument) in constructor.parameters.zip(arguments)) {
        if (argument.isStarProjection) return true
        val projectionKind = argument.projectionKind
        val type = argument.type

        val effectiveVariance = getEffectiveVariance(parameter.variance, projectionKind)
        if (effectiveVariance == Variance.OUT_VARIANCE && !type.isMostPreciseCovariantArgument()) return true
        if (effectiveVariance == Variance.IN_VARIANCE && !type.isMostPreciseContravariantArgument(parameter)) return true
    }

    return false
}

public fun getEffectiveVariance(parameterVariance: Variance, projectionKind: Variance): Variance {
    if (parameterVariance === Variance.INVARIANT) {
        return projectionKind
    }
    if (projectionKind === Variance.INVARIANT) {
        return parameterVariance
    }
    if (parameterVariance === projectionKind) {
        return parameterVariance
    }

    // In<out X> = In<*>
    // Out<in X> = Out<*>
    return Variance.OUT_VARIANCE
}

val CallableDescriptor?.isMethodWithDeclarationSiteWildcards: Boolean
    get() {
        if (this !is CallableMemberDescriptor) return false
        return firstOverridden {
            METHODS_WITH_DECLARATION_SITE_WILDCARDS.containsRaw(it.propertyIfAccessor.fqNameOrNull())
        } != null
    }

private val METHODS_WITH_DECLARATION_SITE_WILDCARDS = setOf(
        FqName("kotlin.MutableCollection.addAll"),
        FqName("kotlin.MutableList.addAll"),
        FqName("kotlin.MutableMap.putAll")
)

internal fun TypeMappingMode.updateArgumentModeFromAnnotations(type: KotlinType): TypeMappingMode {
    type.suppressWildcardsMode()?.let {
        return TypeMappingMode.createWithConstantDeclarationSiteWildcardsMode(
                skipDeclarationSiteWildcards = it, isForAnnotationParameter = isForAnnotationParameter)
    }

    if (type.annotations.hasAnnotation(JVM_WILDCARD_ANNOTATION_FQ_NAME)) {
        return TypeMappingMode.createWithConstantDeclarationSiteWildcardsMode(
                skipDeclarationSiteWildcards = false, isForAnnotationParameter = isForAnnotationParameter, fallbackMode = this)
    }

    return this
}

internal fun extractTypeMappingModeFromAnnotation(
        callableDescriptor: CallableDescriptor?,
        outerType: KotlinType,
        isForAnnotationParameter: Boolean
): TypeMappingMode? =
        (outerType.suppressWildcardsMode() ?: callableDescriptor?.suppressWildcardsMode())?.let {
            if (outerType.arguments.isNotEmpty())
                TypeMappingMode.createWithConstantDeclarationSiteWildcardsMode(
                        skipDeclarationSiteWildcards = it, isForAnnotationParameter = isForAnnotationParameter)
            else
                TypeMappingMode.DEFAULT
        }

private fun DeclarationDescriptor.suppressWildcardsMode(): Boolean? =
        parentsWithSelf.mapNotNull {
            it.annotations.findAnnotation(JVM_SUPPRESS_WILDCARDS_ANNOTATION_FQ_NAME)
        }.firstOrNull().suppressWildcardsMode()

private fun KotlinType.suppressWildcardsMode(): Boolean? =
        annotations.findAnnotation(JVM_SUPPRESS_WILDCARDS_ANNOTATION_FQ_NAME).suppressWildcardsMode()

private fun AnnotationDescriptor?.suppressWildcardsMode(): Boolean? {
    return (this ?: return null).allValueArguments.values.firstOrNull()?.value as? Boolean ?: true
}

private val JVM_SUPPRESS_WILDCARDS_ANNOTATION_FQ_NAME = FqName("kotlin.jvm.JvmSuppressWildcards")
private val JVM_WILDCARD_ANNOTATION_FQ_NAME = FqName("kotlin.jvm.JvmWildcard")
