package com.ebay.opentracing.basic;

import io.opentracing.References;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class TestTraceContextHandler implements TraceContextHandler<TestTraceContext> {
    private static final AtomicInteger SERIAL = new AtomicInteger();

    @Override
    public InternalSpanContext<TestTraceContext> createNew() {
        String traceId = "Trace-" + SERIAL.incrementAndGet();
        String spanId = "Span-" + SERIAL.incrementAndGet();
        TestTraceContext traceContext = new TestTraceContext(traceId, spanId);
        return new InternalSpanContext<>(traceContext, new BaggageBuilder().build());
    }

    @Override
    public InternalSpanContext<TestTraceContext> createForContext(Map<String, List<InternalSpanContext<TestTraceContext>>> references) {
        List<InternalSpanContext<TestTraceContext>> childOfList = references.get(References.CHILD_OF);
        if (childOfList == null)
            childOfList = Collections.emptyList();

        List<InternalSpanContext<TestTraceContext>> followsFromList = references.get(References.FOLLOWS_FROM);
        if (followsFromList == null)
            followsFromList = Collections.emptyList();

        AtomicReference<String> traceIdRef = new AtomicReference<>();
        List<Baggage> baggageList = new ArrayList<>(childOfList.size() + followsFromList.size());
        addFromInternalSpanContext(traceIdRef, baggageList, childOfList);
        addFromInternalSpanContext(traceIdRef, baggageList, followsFromList);

        String traceId = traceIdRef.get();
        if (traceId == null)
            traceId = "Trace-" + SERIAL.incrementAndGet();

        String spanId = "Span-" + SERIAL.incrementAndGet();

        TestTraceContext traceContext = new TestTraceContext(traceId, spanId);
        Baggage baggage = new BaggageBuilder()
                .inheritAll(baggageList)
                .build();
        return new InternalSpanContext<>(traceContext, baggage);
    }

    private void addFromInternalSpanContext(
            AtomicReference<String> traceIdRef,
            List<Baggage> baggageList,
            List<InternalSpanContext<TestTraceContext>> list) {
        String traceId = traceIdRef.get();
        for (InternalSpanContext<TestTraceContext> internalSpanContext : list) {
            TestTraceContext traceContext = internalSpanContext.getTraceContext();

            Baggage baggage = internalSpanContext.getBaggage();
            baggageList.add(baggage);

            String parentTraceId = traceContext.getTraceId();

            String previousTraceId = traceIdRef.getAndSet(traceId);
            if (previousTraceId != null && !previousTraceId.equals(parentTraceId))
                throw new IllegalStateException("Cannot be a member of multiple trace IDs");
        }
    }

}
