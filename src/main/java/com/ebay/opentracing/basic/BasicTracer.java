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
    private final SampleController<T> sampleController;
    private final ActiveSpanSource activeSpanSource;

    BasicTracer(
            TraceContextHandler<T> traceContextHandler,
            FinishedSpanReceiver<T> finishedSpanReceiver,
            ActiveSpanSource activeSpanSource,
            SampleController<T> sampleController,
            Formatters<T> formatters) {
        this.traceContextHandler = traceContextHandler;
        this.spanFinisher = new SpanFinisher<>(finishedSpanReceiver);
        this.activeSpanSource = activeSpanSource;
        this.sampleController = sampleController;
        this.formatters = formatters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanBuilder buildSpan(String operationName) {
        TracerPreconditions.checkNotNull(operationName, "operationName may not be null");
        return new SpanBuilderImpl<>(activeSpanSource, spanFinisher, traceContextHandler, sampleController, operationName);
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
