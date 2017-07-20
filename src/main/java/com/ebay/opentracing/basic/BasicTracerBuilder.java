package com.ebay.opentracing.basic;

import javax.annotation.Nullable;

import io.opentracing.ActiveSpanSource;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.ThreadLocalActiveSpanSource;

/**
 * Builder for creating instances of {@link BasicTracer}.
 *
 * @param <T> trace context type
 */
public final class BasicTracerBuilder<T>
{
	private final Formatters<T> formatters = new Formatters<>();
	private final TraceContextHandler<T> traceContextHandler;

	@Nullable
	private ActiveSpanSource activeSpanSource;

	@Nullable
	private SampleController<T> sampleController;

	@Nullable
	private FinishedSpanReceiver<T> receiver;

	/**
	 * Create a builder instance that leverages the trace context types supported by the provided
	 * trace context handler and reporting finished spans to the finished span receiver.
	 *
	 * @param traceContextHandler handler instance
	 * @param receiver receiver instance
	 */
	public BasicTracerBuilder(TraceContextHandler<T> traceContextHandler, FinishedSpanReceiver<T> receiver)
	{
		this.traceContextHandler =
			TracerPreconditions.checkNotNull(traceContextHandler, "traceContextHandler may not be null");
		this.receiver = TracerPreconditions.checkNotNull(receiver, "receiver may not be null");
	}

	/**
	 * Configure the {@link ActiveSpanSource} to be used by the tracer.  When no source is configured then
	 * the {@link io.opentracing.util.ThreadLocalActiveSpanSource} will be used.
	 *
	 * @param activeSpanSource active span source instance
	 * @return builder instance
	 */
	public BasicTracerBuilder<T> activeSpanSource(ActiveSpanSource activeSpanSource)
	{
		this.activeSpanSource = TracerPreconditions.checkNotNull(activeSpanSource, "activeSpanSource may not be null");
		return this;
	}

	/**
	 * Configure the sampling controller.  When no controller is configured all traces will be sampled.
	 *
	 * @param sampleController controller instance
	 * @return builder instance
	 */
	public BasicTracerBuilder<T> sampleController(SampleController<T> sampleController)
	{
		this.sampleController = TracerPreconditions.checkNotNull(sampleController, "sampleController may not be null");
		return this;
	}

	/**
	 * Register a {@link Formatter} instance which can be used to marshal and unmarshal the specified
	 * {@link Format}.  The registered formatter will be used as needed by the
	 * {@link BasicTracer#inject(SpanContext, Format, Object)} and
	 * {@link BasicTracer#extract(Format, Object)} implementation.
	 *
	 * @param format format to add support for
	 * @param formatter formatter to use to implement the support
	 * @param <C> data carrier type
	 * @return builder instance
	 */
	public <C> BasicTracerBuilder<T> registerFormatter(Format<C> format, Formatter<T, C> formatter)
	{
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
	public Tracer build()
	{
		if (activeSpanSource == null)
			activeSpanSource = new ThreadLocalActiveSpanSource();

		if (sampleController == null)
			sampleController = new SampleControllerAlways<>();

		return new BasicTracer<>(traceContextHandler, receiver, activeSpanSource, sampleController, formatters);
	}

	/**
	 * Create the tracer instance and install it as the {@link GlobalTracer}.
	 *
	 * @return tracer instance
	 */
	public Tracer buildAndInstall()
	{
		Tracer tracer = build();
		GlobalTracer.register(tracer);
		return tracer;
	}

}
