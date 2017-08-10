/*
 * Copyright (c) 2017 eBay Inc.
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

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

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
    private final ActiveSpanSource activeSpanSource;
    private final SpanInitiatorContext<T> spanInitiatorContext;
    private final SpanInitiator<T> spanInitiator;

    BasicTracer(
            TraceContextHandler<T> traceContextHandler,
            SpanInitiator<T> spanInitiator,
            FinishedSpanReceiver<T> finishedSpanReceiver,
            ActiveSpanSource activeSpanSource,
            Formatters<T> formatters) {
        this.traceContextHandler = traceContextHandler;
        this.spanInitiator = spanInitiator;
        this.spanFinisher = new SpanFinisher<>(finishedSpanReceiver);
        this.activeSpanSource = activeSpanSource;
        this.formatters = formatters;

        this.spanInitiatorContext = new SpanInitiatorContextImpl<>(activeSpanSource, spanFinisher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanBuilder buildSpan(String operationName) {
        TracerPreconditions.checkNotNull(operationName, "operationName may not be null");
        return new SpanBuilderImpl<>(activeSpanSource, spanInitiatorContext, spanInitiator, traceContextHandler, operationName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        if (!(spanContext instanceof InternalSpanContext)) {
            throw new IllegalStateException("Foreign span context provided");
        }
        TracerPreconditions.checkNotNull(format, "format may not be null");
        TracerPreconditions.checkNotNull(carrier, "carrier may not be null");

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
        TracerPreconditions.checkNotNull(format, "format may not be null");
        TracerPreconditions.checkNotNull(carrier, "carrier may not be null");

        Formatter<T, C> formatter = formatters.get(format);
        return formatter.extract(carrier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActiveSpan activeSpan() {
        return activeSpanSource.activeSpan();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActiveSpan makeActive(Span span) {
        return activeSpanSource.makeActive(span);
    }

}
