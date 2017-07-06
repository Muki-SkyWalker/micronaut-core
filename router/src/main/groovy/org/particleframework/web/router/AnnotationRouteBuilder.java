/*
 * Copyright 2017 original authors
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
package org.particleframework.web.router;

import org.particleframework.context.ApplicationContext;
import org.particleframework.context.processor.ExecutableMethodProcessor;
import org.particleframework.inject.ExecutableMethod;
import org.particleframework.inject.annotation.Executable;
import org.particleframework.web.router.annotation.Action;
import org.particleframework.web.router.annotation.Get;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author Graeme Rocher
 * @since 1.0
 */
public class AnnotationRouteBuilder extends DefaultRouteBuilder implements ExecutableMethodProcessor<Executable> {

    private final Map<Class, BiConsumer<Annotation,ExecutableMethod>> httpMethodsHandlers = new LinkedHashMap<>();


    public AnnotationRouteBuilder(ApplicationContext beanContext, UriNamingStrategy uriNamingStrategy) {
        super(beanContext, uriNamingStrategy);
        httpMethodsHandlers.put(Get.class, (Annotation ann, ExecutableMethod method)->
                GET(resolveUri(((Get)ann).value(), method, uriNamingStrategy), method.getDeclaringType(), method.getMethodName(), method.getArgumentTypes())
        );
    }

    private String resolveUri(String value, ExecutableMethod method, UriNamingStrategy uriNamingStrategy) {
        Class declaringType = method.getDeclaringType();
        String rootUri = uriNamingStrategy.resolveUri(declaringType);
        if(value != null && value.length() > 0) {
            return rootUri + value;
        }
        else {
            return rootUri + "/" + method.getMethodName();
        }
    }

    @Override
    public void process(ApplicationContext applicationContext, ExecutableMethod method) {
        Annotation annotation = method.findAnnotation(Action.class);
        if(annotation != null) {
            Class<? extends Annotation> annotationClass = annotation.annotationType();
            BiConsumer<Annotation, ExecutableMethod> handler = httpMethodsHandlers.get(annotationClass);
            if(handler != null) {
                handler.accept(annotation, method);
            }
        }
    }
}
