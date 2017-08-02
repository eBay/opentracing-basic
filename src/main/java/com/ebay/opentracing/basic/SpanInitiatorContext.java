package com.ebay.opentracing.basic;

import io.opentracing.ActiveSpanSource;
import io.opentracing.Span;

/**
 * This interface exposes the requisite internals of the {@link BasicTracer} instance so that the {@link SpanInitiator}
 * has enough knowledge to intelligently integrate.
 *
 * @param <T> trace context type
 */
public interface SpanInitiatorContext<T> {

    /**
     * Get the {@link ActiveSpanSource} being used by the tracer.
     *
     * @return source instance
     */
    ActiveSpanSource getActiveSpanSource();

    /**
     * Create a span instance from the {@link MutableSpanData} provided.
     *
     * @param spanData span data
     * @return span instance
     */
    Span createSpan(MutableSpanData<T> spanData);

}
