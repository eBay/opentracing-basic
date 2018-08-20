/*
 * Copyright (c) 2017-2018 eBay Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ebay.opentracing.basic;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * {@link io.opentracing.Tracer.SpanBuilder} implementation.
 *
 * @param <T> trace context type
 */
final class SpanBuilderImpl<T> implements Tracer.SpanBuilder {
    private final ScopeManager scopeManager;
    private final SpanInitiatorContext<T> spanInitiatorContext;
    private final SpanInitiator<T> spanInitiator;
    private final TraceContextHandler<T> traceContextHandler;
    private final String operationName;

    private boolean ignoreActiveSpan;
    private TimeUnit startTimeUnit;
    private long startTimeStamp;

    @Nullable
    private Map<String, List<InternalSpanContext<T>>> references;
    @Nullable
    private Map<String, String> tags;

    SpanBuilderImpl(
            ScopeManager scopeManager,
            SpanInitiatorContext<T> spanInitiatorContext,
            SpanInitiator<T> spanInitiator,
            TraceContextHandler<T> traceContextHandler,
            String operationName) {
        this.scopeManager = scopeManager;
        this.spanInitiatorContext = spanInitiatorContext;
        this.spanInitiator = spanInitiator;
        this.traceContextHandler = traceContextHandler;
        this.operationName = operationName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer.SpanBuilder asChildOf(SpanContext parent) {
        return addReference(References.CHILD_OF, parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer.SpanBuilder asChildOf(Span parent) {
        if (parent == null)
            return this;

        return addReference(References.CHILD_OF, parent.context());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer.SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
        if (referencedContext == null)
            return this;

        if (!(referencedContext instanceof InternalSpanContext)) {
            throw new IllegalStateException("Foreign span context provided");
        }

        @SuppressWarnings("unchecked")
        InternalSpanContext<T> spanContextImpl = (InternalSpanContext<T>) referencedContext;

        return addReference(referenceType, spanContextImpl);
    }

    private Tracer.SpanBuilder addReference(String referenceType, InternalSpanContext<T> spanContext) {
        if (references == null) {
            references = new HashMap<>();
        }
        List<InternalSpanContext<T>> list = references.get(referenceType);
        if (list == null) {
            list = new ArrayList<>(4);
            references.put(referenceType, list);
        }
        list.add(spanContext);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer.SpanBuilder ignoreActiveSpan() {
        ignoreActiveSpan = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer.SpanBuilder withTag(String key, String value) {
        if (tags == null) {
            tags = new HashMap<>(5);
        }
        tags.put(key, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer.SpanBuilder withTag(String key, boolean value) {
        return withTag(key, Boolean.toString(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer.SpanBuilder withTag(String key, Number value) {
        return withTag(key, value.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
        startTimeUnit = TimeUnit.MICROSECONDS;
        startTimeStamp = microseconds;
        return this;
    }

    @Override
    public Scope startActive(boolean finishSpanOnClose) {
        Span span = start();
        return scopeManager.activate(span, finishSpanOnClose);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public Span startManual() {
        return start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span start() {
        SpanState<T> spanState = buildContext();
        return spanInitiator.initiateSpan(spanInitiatorContext, spanState);
    }

    private SpanState<T> buildContext() {
        ScopeManager scopeManager = this.scopeManager;

        if (references == null) {
            Scope activeScope = scopeManager.active();
            if (activeScope != null) {
                Span span = activeScope.span();

                @SuppressWarnings("unchecked")
                InternalSpanContext<T> activeContext = (InternalSpanContext<T>) span.context();

                // Implicit child-of active span relationship
                if (activeContext != null && !ignoreActiveSpan) {
                    references = Collections.singletonMap(References.CHILD_OF, Collections.singletonList(activeContext));
                }
            }

            if (references == null) {
                references = Collections.emptyMap();
            }
        }

        InternalSpanContext<T> internalSpanContext;
        if (references.isEmpty()) {
            internalSpanContext = traceContextHandler.createNew();
        } else {
            internalSpanContext = traceContextHandler.createForContext(references);
        }

        // Use current time as start time if not specified
        if (startTimeUnit == null) {
            startTimeUnit = TimeUnit.MILLISECONDS;
            startTimeStamp = System.currentTimeMillis();
        }

        return new SpanState<>(internalSpanContext, operationName, startTimeUnit, startTimeStamp, tags, references);
    }

}
