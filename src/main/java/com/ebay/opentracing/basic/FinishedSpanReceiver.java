package com.ebay.opentracing.basic;

import javax.annotation.Nonnull;

/**
 * Callback used to receive span data one the spans have been completed.  This callback can be used to
 * implement a bridge to an arbitrary back-end tracing system.
 *
 * @param <T> trace context type
 */
public interface FinishedSpanReceiver<T>
{
	/**
	 * Called when a span has finished on the thread which the executing the spanned work.
	 *
	 * @param spanData span data
	 */
	void spanFinished(SpanData<T> spanData);

}
