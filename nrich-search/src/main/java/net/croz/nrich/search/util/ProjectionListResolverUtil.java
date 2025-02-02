/*
 *  Copyright 2020-2023 CROZ d.o.o, the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package net.croz.nrich.search.util;

import net.croz.nrich.search.api.annotation.Projection;
import net.croz.nrich.search.api.model.SearchProjection;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ProjectionListResolverUtil {

    private ProjectionListResolverUtil() {
    }

    public static <R> List<SearchProjection<R>> resolveSearchProjectionList(Class<?> projectionType) {
        Predicate<Field> shouldIncludeField = field -> !(field.isSynthetic() || Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()));

        return Arrays.stream(projectionType.getDeclaredFields())
            .filter(shouldIncludeField)
            .map(ProjectionListResolverUtil::<R>convertToProjection)
            .collect(Collectors.toList());
    }

    private static <R> SearchProjection<R> convertToProjection(Field field) {
        String alias = field.getName();
        Annotation[] annotationList = field.getAnnotations();

        String path = alias;
        Predicate<R> condition = request -> true;
        if (annotationList.length > 0) {
            Projection projectionAnnotation = findProjectionAnnotation(annotationList);

            if (projectionAnnotation != null) {
                path = projectionAnnotation.path();

                if (!Projection.DEFAULT.class.equals(projectionAnnotation.condition())) {
                    @SuppressWarnings("unchecked")
                    Predicate<R> predicate = (Predicate<R>) BeanUtils.instantiateClass(projectionAnnotation.condition());
                    condition = predicate;
                }
            }
            else {
                Value valueAnnotation = findValueAnnotation(annotationList);

                if (valueAnnotation != null) {
                    path = valueAnnotation.value();
                }
            }
        }

        return new SearchProjection<>(path, alias, condition);
    }

    private static Value findValueAnnotation(Annotation[] annotationList) {
        return (Value) Arrays.stream(annotationList)
            .filter(annotation -> Value.class.isAssignableFrom(annotation.annotationType()))
            .findFirst()
            .orElse(null);
    }

    private static Projection findProjectionAnnotation(Annotation[] annotationList) {
        return (Projection) Arrays.stream(annotationList)
            .filter(annotation -> Projection.class.isAssignableFrom(annotation.annotationType()))
            .findFirst()
            .orElse(null);
    }
}
