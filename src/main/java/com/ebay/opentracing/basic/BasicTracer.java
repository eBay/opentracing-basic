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

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

import java.util.Objects;

/**
 * Basic {@link Tracer} implementation which implements the primary OpenTracing API but delegates the resulting
 * span data to a callback upon span completion.  It also allows for the provision of arbitrary "trace context"
 * data which can be used to fully implement {@link SpanContext} - typically a tuple of: Trace ID, Span ID, and
 * sampling information - when bridging to an actual tracing implementation.
 *
 * @param <T> trace context type
 */
final class BasicTracer<T> implements Tracer {

    private final TraceContextHandler<T> traceContextHandler;
    private final SpanFinisher<T> spanFinisher;
    private final Formatters<T> formatters;
    private final ScopeManager scopeManager;
    private final SpanInitiatorContext<T> spanInitiatorContext;
    private final SpanInitiator<T> spanInitiator;

    BasicTracer(
            TraceContextHandler<T> traceContextHandler,
            SpanInitiator<T> spanInitiator,
            FinishedSpanReceiver<T> finishedSpanReceiver,
            ScopeManager scopeManager,
            Formatters<T> formatters) {
        this.traceContextHandler = traceContextHandler;
        this.spanInitiator = spanInitiator;
        this.spanFinisher = new SpanFinisher<>(finishedSpanReceiver);
        this.scopeManager = scopeManager;
        this.formatters = formatters;

        this.spanInitiatorContext = new SpanInitiatorContextImpl<>(scopeManager, spanFinisher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanBuilder buildSpan(String operationName) {
        Objects.requireNonNull(operationName, "operationName may not be null");
        return new SpanBuilderImpl<>(scopeManager, spanInitiatorContext, spanInitiator, traceContextHandler, operationName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        if (!(spanContext instanceof InternalSpanContext)) {
            throw new IllegalStateException("Foreign span context provided");
        }
        Objects.requireNonNull(format, "format may not be null");
        Objects.requireNonNull(carrier, "carrier may not be null");

        @SuppressWarnings("unchecked")
        InternalSpanContext<T> internalSpanContext = (InternalSpanContext<T>) spanContext;

        Formatter<T, C> formatter = formatters.get(format);
        formatter.inject(internalSpanContext, carrier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        Objects.requireNonNull(format, "format may not be null");
        Objects.requireNonNull(carrier, "carrier may not be null");

        Formatter<T, C> formatter = formatters.get(format);
        return formatter.extract(carrier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span activeSpan() {
        Scope activeScope = scopeManager.active();
        return (activeScope == null) ? null : activeScope.span();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScopeManager scopeManager() {
        return scopeManager;
    }

}
