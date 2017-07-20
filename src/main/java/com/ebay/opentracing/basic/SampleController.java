package com.ebay.opentracing.basic;

/**
 * Interface used to determine if a span should be sampled.  If there is already an active span then the span
 * will always be sampled (otherwise traces would be incomplete).  If no span is yet active, the sample controller
 * may use any means and knowledge necessary to determine whether a new the trace should begin with the current span.
 *
 * @param <T> trace context
 */
@SuppressWarnings("WeakerAccess") // API class
public interface SampleController<T>
{

	/**
	 * Determines whether or not the span with the provided information should be sampled.
	 *
	 * @param spanData information about the span which is being considered
	 * @return {@code true} if a trace should be created, {@code false} otherwise
	 */
	boolean isSampled(SpanData<T> spanData);

}
