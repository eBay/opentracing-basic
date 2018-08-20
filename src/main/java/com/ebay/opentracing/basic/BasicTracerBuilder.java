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

import io.opentracing.ScopeManager;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.ThreadLocalScopeManager;

import javax.annotation.Nullable;

/**
 * Builder for creating instances of {@link BasicTracer}.
 *
 * @param <T> trace context type
 */
@SuppressWarnings("WeakerAccess") // API class
public final class BasicTracerBuilder<T> {
    private final Formatters<T> formatters = new Formatters<>();
    private final TraceContextHandler<T> traceContextHandler;

    @Nullable
    private ScopeManager scopeManager;

    @Nullable
    private SpanInitiator<T> spanInitiator;

    @Nullable
    private FinishedSpanReceiver<T> receiver;

    /**
     * Create a builder instance that leverages the trace context types supported by the provided
     * trace context handler and reporting finished spans to the finished span receiver.
     *
     * @param traceContextHandler handler instance
     * @param receiver            receiver instance
     */
    public BasicTracerBuilder(TraceContextHandler<T> traceContextHandler, FinishedSpanReceiver<T> receiver) {
        this.traceContextHandler = TracerPreconditions.checkNotNull(
                traceContextHandler, "traceContextHandler may not be null");
        this.receiver = TracerPreconditions.checkNotNull(receiver, "receiver may not be null");
    }

    /**
     * Configure the {@link ScopeManager} to be used by the tracer.  When no source is configured then
     * the {@link io.opentracing.util.ThreadLocalScopeManager} will be used.
     *
     * @param scopeManager active span source instance
     * @return builder instance
     */
    public BasicTracerBuilder<T> scopeManager(ScopeManager scopeManager) {
        this.scopeManager = TracerPreconditions.checkNotNull(
                scopeManager, "scopeManager may not be null");
        return this;
    }

    /**
     * Configure the {@link SpanInitiator} to be used by the tracer.
     *
     * @param spanInitiator span initiator instance
     * @return builder instance
     */
    public BasicTracerBuilder<T> spanInitiator(SpanInitiator<T> spanInitiator) {
        this.spanInitiator = TracerPreconditions.checkNotNull(
                spanInitiator, "spanInitiator may not be null");
        return this;
    }

    /**
     * Register a {@link Formatter} instance which can be used to marshal and unmarshal the specified
     * {@link Format}.  The registered formatter will be used as needed by the
     * {@link BasicTracer#inject(SpanContext, Format, Object)} and
     * {@link BasicTracer#extract(Format, Object)} implementation.
     *
     * @param format    format to add support for
     * @param formatter formatter to use to implement the support
     * @param <C>       data carrier type
     * @return builder instance
     */
    public <C> BasicTracerBuilder<T> registerFormatter(Format<C> format, Formatter<T, C> formatter) {
        TracerPreconditions.checkNotNull(format, "format may not be null");
        TracerPreconditions.checkNotNull(formatter, "formatter may not be null");
        formatters.register(format, formatter);
        return this;
    }

    /**
     * Create the tracer instance.
     *
     * @return tracer instance
     */
    public Tracer build() {
        if (scopeManager == null) {
            scopeManager = new ThreadLocalScopeManager();
        }

        if (spanInitiator == null) {
            spanInitiator = new SpanInitiatorImpl<>();
        }

        return new BasicTracer<>(traceContextHandler, spanInitiator, receiver, scopeManager, formatters);
    }

    /**
     * Create the tracer instance and install it as the {@link GlobalTracer}.
     *
     * @return tracer instance
     */
    public Tracer buildAndInstall() {
        Tracer tracer = build();
        GlobalTracer.register(tracer);
        return tracer;
    }

}
