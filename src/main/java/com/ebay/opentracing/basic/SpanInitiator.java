package com.ebay.opentracing.basic;

import io.opentracing.Span;

/**
 * Interface used to define the API for obtaining knowledge of span creation when using the {@link BasicTracer}.
 * The {@link io.opentracing.Tracer.SpanBuilder} implementation will use this to create new {@link Span} instances.
 * This hook allows for complex interactions, such as sampling, trace span aggregation, and more to be implemented
 * and integrated with the {@link BasicTracer}.
 */
public interface SpanInitiator<T> {

    /**
     * Initiate a {@link Span} given the current initiator context and the provided span data.
     *
     * There is no need to worry about activating the span when implementing an initiator, though the initiator
     * context does provide access to the {@link io.opentracing.ActiveSpanSource} to expose any potential currently
     * active span.
     *
     * @param initiatorContext context in which the span is being created, providing access to the necessary internals
     * @param spanData the span data being turned into a {@link Span}
     * @return new span instance
     * @see SpanInitiatorContext#createSpan(MutableSpanData)
     */
    Span initiateSpan(
            SpanInitiatorContext<T> initiatorContext,
            MutableSpanData<T> spanData);

}
