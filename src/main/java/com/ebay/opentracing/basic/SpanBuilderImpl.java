package com.ebay.opentracing.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;
import io.opentracing.BaseSpan;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;

/**
 * {@link io.opentracing.Tracer.SpanBuilder} implementation.
 *
 * @param <T> trace context type
 */
final class SpanBuilderImpl<T> implements Tracer.SpanBuilder
{
	private final ActiveSpanSource activeSpanSource;
	private final SpanFinisher<T> spanFinisher;
	private final TraceContextHandler<T> traceContextHandler;
	private final SampleController<T> sampleController;
	private final String operationName;

	private boolean ignoreActiveSpan;
	private TimeUnit startTimeUnit;
	private long startTimeStamp;

	@Nullable
	private Map<String, List<InternalSpanContext<T>>> references;
	@Nullable
	private Map<String, String> tags;

	SpanBuilderImpl(
		ActiveSpanSource activeSpanSource,
		SpanFinisher<T> spanFinisher,
		TraceContextHandler<T> traceContextHandler,
		SampleController<T> sampleController,
		String operationName)
	{
		this.activeSpanSource = activeSpanSource;
		this.spanFinisher = spanFinisher;
		this.traceContextHandler = traceContextHandler;
		this.sampleController = sampleController;
		this.operationName = operationName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tracer.SpanBuilder asChildOf(SpanContext parent)
	{
		return addReference(References.CHILD_OF, parent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tracer.SpanBuilder asChildOf(BaseSpan<?> parent)
	{
		return addReference(References.CHILD_OF, parent.context());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tracer.SpanBuilder addReference(String referenceType, SpanContext referencedContext)
	{
		if (!(referencedContext instanceof InternalSpanContext))
			throw new IllegalStateException("Foreign span context provided");

		@SuppressWarnings("unchecked")
		InternalSpanContext<T> spanContextImpl = (InternalSpanContext<T>) referencedContext;

		return addReference(referenceType, spanContextImpl);
	}

	private Tracer.SpanBuilder addReference(String referenceType, InternalSpanContext<T> spanContext)
	{
		if (references == null)
			references = new HashMap<>();
		List<InternalSpanContext<T>> list = references.get(referenceType);
		if (list == null)
		{
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
	public Tracer.SpanBuilder ignoreActiveSpan()
	{
		ignoreActiveSpan = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tracer.SpanBuilder withTag(String key, String value)
	{
		if (tags == null)
			tags = new HashMap<>(5);
		tags.put(key, value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tracer.SpanBuilder withTag(String key, boolean value)
	{
		return withTag(key, Boolean.toString(value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tracer.SpanBuilder withTag(String key, Number value)
	{
		return withTag(key, value.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tracer.SpanBuilder withStartTimestamp(long microseconds)
	{
		startTimeUnit = TimeUnit.MICROSECONDS;
		startTimeStamp = microseconds;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ActiveSpan startActive()
	{
		Span span = startManual();
		return activeSpanSource.makeActive(span);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Span startManual()
	{
		SpanState<T> spanState = buildContext();
		ActiveSpan activeSpan = activeSpanSource.activeSpan();
		if (activeSpan != null || sampleController.isSampled(spanState))
			return new SpanImpl<>(spanState, spanFinisher);
		else
			return new UnsampledRootSpan<>(spanState.getSpanContext());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Span start()
	{
		return startManual();
	}

	private SpanState<T> buildContext()
	{
		ActiveSpan activeSpan = activeSpanSource.activeSpan();

		@SuppressWarnings("unchecked")
		InternalSpanContext<T> activeContext = (InternalSpanContext<T>) (activeSpan == null ? null : activeSpan.context());

		// Implicit child-of active span relationship
		if (references == null && activeContext != null && !ignoreActiveSpan)
			references = Collections.singletonMap(References.CHILD_OF, Collections.singletonList(activeContext));

		if (references == null)
			references = Collections.emptyMap();

		InternalSpanContext<T> internalSpanContext;
		if (ignoreActiveSpan || activeContext == null)
			internalSpanContext = traceContextHandler.createNew();
		else
			internalSpanContext = traceContextHandler.createForContext(references);

		// Use current time as start time if not specified
		if (startTimeUnit == null)
		{
			startTimeUnit = TimeUnit.MILLISECONDS;
			startTimeStamp = System.currentTimeMillis();
		}

		return new SpanState<>(internalSpanContext, operationName, startTimeUnit, startTimeStamp, tags, references);
	}

}
