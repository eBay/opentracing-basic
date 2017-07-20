/**
 * <p>
 * This package is a simple implementation of the OpenTracing API for Java.  The goal of this code is to
 * implement the API and semantics of the OpenTracing API in a plug-able manner such that it can be used as
 * a bridge to any OpenTracing- compliant span data collector.
 * </p>
 * <p>
 * As a result of the above, this library abstracts all tracer- specific implementation details
 * (e.g., trace ID, span ID, sampling data, etc.) into an arbitrary "trace context" object.  In places
 * where the library needs to respond based on the state of the tracer, a
 * {@link com.ebay.opentracing.basic.TraceContextHandler} is used to work with the trace context object
 * to implement the contract.
 * </p>
 * <p>
 * Additionally, injection and extraction of the {@link io.opentracing.SpanContext} to and from a data
 * carrier object can only be done with knowledge of the tracer implementation.  In order to facilitate this
 * functionality the library exposes the ability to register a {@link com.ebay.opentracing.basic.Formatter}
 * instance against a specified {@link io.opentracing.propagation.Format}.  The formatter will be used at the
 * system boundaries to implement the {@link io.opentracing.SpanContext} propagation.
 * </p>
 * <p>
 * When this library determines that a span has finished a call is made to a registered
 * {@link com.ebay.opentracing.basic.FinishedSpanReceiver} callback with the {@link io.opentracing.Span}'s
 * data.
 * </p>
 */
@Nonnull
package com.ebay.opentracing.basic;

import javax.annotation.Nonnull;