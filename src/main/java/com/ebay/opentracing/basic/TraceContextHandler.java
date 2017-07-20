package com.ebay.opentracing.basic;

import java.util.List;
import java.util.Map;

/**
 * Interface used to encapsulate required interactions with a provider of trace context objects.
 */
public interface TraceContextHandler<T>
{

	/**
	 * Create a new span context which is not associated with any other spans.  i.e., a new root/initial span.
	 *
	 * @return span context
	 */
	InternalSpanContext<T> createNew();

	/**
	 * Create a new span context given the span's configured references.  This would typically look for
	 * child-of or follows-from relationships and create a new span context configured with the same
	 * trace ID but a new/unique span ID.
	 *
	 * @param references reference map
	 * @return span context
	 */
	InternalSpanContext<T> createForContext(Map<String, List<InternalSpanContext<T>>> references);

}
