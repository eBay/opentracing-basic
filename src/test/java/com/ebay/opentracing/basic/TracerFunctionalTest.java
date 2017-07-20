package com.ebay.opentracing.basic;

import io.opentracing.ActiveSpan;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.Tracer;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Basic functional testing of the tracer.
 */
@SuppressWarnings({"EmptyTryBlock", "unchecked"})
@RunWith(JMockit.class)
public class TracerFunctionalTest {

    @Mocked
    private Provider<String> traceIdProvider;

    @Mocked
    private Provider<String> spanIdProvider;

    @Mocked
    private SampleController<TestTraceContext> sampleController;

    @Mocked
    private FinishedSpanReceiver<TestTraceContext> finishedSpanReceiver;

    private Tracer uut;

    @Before
    public void before() throws Exception {
        uut = new BasicTracerBuilder<>(new TestTraceContextHandler(), finishedSpanReceiver)
                .sampleController(sampleController)
                .build();
    }

    @Test
    public void noActiveSpanOutsideOfTrace() {
        ActiveSpan actual = uut.activeSpan();
        assertNull(actual);
    }

    @Test
    public void activeSpanWithinSpan() {
        whenAlwaysSampling();
        try (ActiveSpan span = uut.buildSpan("outer").startActive()) {
            ActiveSpan actual = uut.activeSpan();
            assertSame(span, actual);
        }
    }

    @Test
    public void spanContextToStringDoesNotBlowUp() {
        whenAlwaysSampling();
        try (ActiveSpan span = uut.buildSpan("span").startActive()) {
            String string = span.context().toString();
            assertNotNull(string);
        }
    }

    @Test
    public void spanAutoCloseCausesFinish() {
        whenAlwaysSampling();
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 1;
        }};

        try (ActiveSpan span = uut.buildSpan("spanOperation").startActive()) {
            // Do stuff
        }

        assertEquals(1, capturedSpanData.size());
    }

    @Test
    public void spanDataToStringDoesNotBlowUp() {
        whenAlwaysSampling();
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
        }};

        try (ActiveSpan outerSpan = uut.buildSpan("outerSpan").startActive()) {
            Span anotherSpan = uut.buildSpan("anotherSpan").startManual();
            anotherSpan.finish();

            try (ActiveSpan childSpan = uut.buildSpan("childSpan")
                    .asChildOf(outerSpan)
                    .addReference(References.FOLLOWS_FROM, anotherSpan.context())
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .startActive()) {
                // Do stuff
            }
        }

        assertEquals(3, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(1);
        String string = spanData.toString();
        assertNotNull(string);
    }

    @SuppressWarnings("deprecation") // Testing deprecated method in official API
    @Test
    public void spanLogEvents() {
        whenAlwaysSampling();
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 1;
        }};

        HashMap<String, String> mapWithTimestamp = new HashMap<>();
        mapWithTimestamp.put("with1", "val1");
        mapWithTimestamp.put("with2", "val2");

        HashMap<String, String> mapWithoutTimestamp = new HashMap<>();
        mapWithoutTimestamp.put("without1", "val1");
        mapWithoutTimestamp.put("without2", "val2");

        try (ActiveSpan span = uut.buildSpan("spanOperation").startActive()) {
            span.log("no timestamp");
            span.log("eventName", "eventValue");
            span.log(12345L, "with timestamp");
            span.log(mapWithoutTimestamp);
            span.log(54321L, mapWithTimestamp);
        }

        assertEquals(1, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(0);
        List<LogEvent> logEvents = spanData.getLogEvents();
        assertEquals(7, logEvents.size());
        List<String> logEventStrings = new ArrayList<>(logEvents.size());
        for (LogEvent logEvent : logEvents) {
            logEventStrings.add(logEvent.getEventName()
                    + "@" + logEvent.getTimeStamp(TimeUnit.MICROSECONDS)
                    + "=" + logEvent.getPayload());
        }
        assertTrue(logEventStrings.remove("event@12345=with timestamp"));
        assertTrue(logEventStrings.remove("with1@54321=val1"));
        assertTrue(logEventStrings.remove("with2@54321=val2"));
        for (String logEventString : logEventStrings) {
            if (logEventString.startsWith("eventName@"))
                assertTrue(logEventString.endsWith("=eventValue"));
            else if (logEventString.startsWith("without1@"))
                assertTrue(logEventString.endsWith("=val1"));
            else if (logEventString.startsWith("without2@"))
                assertTrue(logEventString.endsWith("=val2"));
            else if (logEventString.startsWith("event@"))
                assertTrue(logEventString.endsWith("=no timestamp"));
            else
                fail("Unknown string: " + logEventString);
        }
    }

    @Test
    public void spanStartAndFinishTimes() {
        whenAlwaysSampling();
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 1;
        }};

        Span span = uut.buildSpan("spanOperation").withStartTimestamp(12345L).startManual();
        span.finish(13682L);

        assertEquals(1, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(0);
        assertEquals(12345L, spanData.getStartTime(TimeUnit.MICROSECONDS));
        assertEquals(13682L, spanData.getFinishTime(TimeUnit.MICROSECONDS));
    }

    @Test
    public void spanUpdatedOperationName() {
        whenAlwaysSampling();
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 1;
        }};

        try (ActiveSpan span = uut.buildSpan("initial").startActive()) {
            span.setOperationName("expected");
        }

        assertEquals(1, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(0);
        assertEquals("expected", spanData.getOperationName());
    }

    @Test
    public void activeSpanWithinNestedSpan() throws Exception {
        whenAlwaysSampling();
        try (ActiveSpan outerSpan = uut.buildSpan("outer").startActive()) {
            try (ActiveSpan innerSpan = uut.buildSpan("inner").startActive()) {
                ActiveSpan actual = uut.activeSpan();
                assertSame(innerSpan, actual);
            }
        }
    }

    @Test
    public void sampledSpanIsReceived() {
        whenAlwaysSampling();

        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 1;
        }};
        try (ActiveSpan outerSpan = uut.buildSpan("spanOperation").startActive()) {
            // Do stuff
        }

        assertEquals(1, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(0);
        assertEquals("spanOperation", spanData.getOperationName());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void unsampledRootSpanIsNotReceived() {
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            sampleController.isSampled(withInstanceOf(SpanData.class));
            result = new Delegate<SpanData<TestTraceContext>>() {
                boolean isSampled(SpanData<TestTraceContext> spanData) {
                    return !"outerSpan".equals(spanData.getOperationName());
                }
            };

            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
        }};
        try (ActiveSpan outerSpan = uut.buildSpan("outerSpan").startActive()) {
            try (ActiveSpan middleSpan = uut.buildSpan("middleSpan").startActive()) {
                try (ActiveSpan innerSpan = uut.buildSpan("innerSpan").startActive()) {
                    // Do stuff
                }
            }
        }
        assertEquals(2, capturedSpanData.size());
        SpanData<TestTraceContext> spanData;

        spanData = capturedSpanData.get(0);
        assertEquals("innerSpan", spanData.getOperationName());

        spanData = capturedSpanData.get(1);
        assertEquals("middleSpan", spanData.getOperationName());
    }

    @Test
    public void unsampledMiddleAndSpansIsReceived() {
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            sampleController.isSampled(withInstanceOf(SpanData.class));
            result = new Delegate<SpanData<TestTraceContext>>() {
                boolean isSampled(SpanData<TestTraceContext> spanData) {
                    return !"middleSpan".equals(spanData.getOperationName());
                }
            };

            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
        }};
        try (ActiveSpan outerSpan = uut.buildSpan("outerSpan").startActive()) {
            try (ActiveSpan middleSpan = uut.buildSpan("middleSpan").startActive()) {
                try (ActiveSpan innerSpan = uut.buildSpan("innerSpan").startActive()) {
                    // Do stuff
                }
            }
        }

        assertEquals(3, capturedSpanData.size());
        SpanData<TestTraceContext> spanData;

        spanData = capturedSpanData.get(0);
        assertEquals("innerSpan", spanData.getOperationName());

        spanData = capturedSpanData.get(1);
        assertEquals("middleSpan", spanData.getOperationName());

        spanData = capturedSpanData.get(2);
        assertEquals("outerSpan", spanData.getOperationName());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void unsampledRootSpanBaggageIsPropagated() {
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            sampleController.isSampled(withInstanceOf(SpanData.class));
            result = new Delegate<SpanData<TestTraceContext>>() {
                boolean isSampled(SpanData<TestTraceContext> spanData) {
                    return !"outerSpan".equals(spanData.getOperationName());
                }
            };

            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
        }};
        try (ActiveSpan outerSpan = uut.buildSpan("outerSpan").startActive()) {
            outerSpan.setBaggageItem("test", "pass");
            try (ActiveSpan middleSpan = uut.buildSpan("middleSpan").startActive()) {
                try (ActiveSpan innerSpan = uut.buildSpan("innerSpan").startActive()) {
                    // Do stuff
                }
            }
        }

        assertEquals(2, capturedSpanData.size());
        for (SpanData<TestTraceContext> spanData : capturedSpanData) {
            String value = locateValue(spanData.getSpanContext().getBaggage().baggageItems(), "test");
            assertEquals("pass", value);
        }
    }

    @Test
    public void baggagePropagation() {
        whenAlwaysSampling();

        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 3;
        }};

        try (ActiveSpan outer = uut.buildSpan("outer").startActive()) {
            outer.setBaggageItem("outer", "1");
            assertEquals("1", outer.getBaggageItem("outer"));
            try (ActiveSpan middle = uut.buildSpan("middle").startActive()) {
                middle.setBaggageItem("middle", "2");
                assertEquals("1", middle.getBaggageItem("outer"));
                assertEquals("2", middle.getBaggageItem("middle"));
                assertNull(outer.getBaggageItem("middle"));
                try (ActiveSpan inner = uut.buildSpan("inner").startActive()) {
                    inner.setBaggageItem("inner", "3");
                    assertEquals("1", inner.getBaggageItem("outer"));
                    assertEquals("2", inner.getBaggageItem("middle"));
                    assertEquals("3", inner.getBaggageItem("inner"));
                    assertNull(outer.getBaggageItem("middle"));
                    assertNull(outer.getBaggageItem("inner"));
                    assertNull(middle.getBaggageItem("inner"));
                }
            }
        }

        assertEquals(3, capturedSpanData.size());
        for (SpanData<?> spanData : capturedSpanData) {
            String operationName = spanData.getOperationName();
            Iterable<Map.Entry<String, String>> baggageItems = spanData.getSpanContext().getBaggage().baggageItems();
            if (operationName.equals("outer")) {
                assertEquals(1, iterableSize(baggageItems));
                assertEquals("1", locateValue(baggageItems, "outer"));
            } else if (operationName.equals("middle")) {
                assertEquals(2, iterableSize(baggageItems));
                assertEquals("1", locateValue(baggageItems, "outer"));
                assertEquals("2", locateValue(baggageItems, "middle"));
            } else {
                assertEquals(3, iterableSize(baggageItems));
                assertEquals("1", locateValue(baggageItems, "outer"));
                assertEquals("2", locateValue(baggageItems, "middle"));
                assertEquals("3", locateValue(baggageItems, "inner"));
            }
        }
    }

    @Test
    public void spanTagsWork() {
        whenAlwaysSampling();

        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 1;
        }};
        try (ActiveSpan span = uut.buildSpan("spanOperation")
                .withTag("initial1", "1")
                .withTag("initial2", true)
                .withTag("initial3", 5)
                .startActive()) {
            span.setTag("tag1", "value1");
            span.setTag("tag2", true);
            span.setTag("tag3", 37);
        }

        assertEquals(1, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(0);
        Map<String, String> tags = spanData.getTags();
        assertEquals(6, iterableSize(tags.entrySet()));
        assertEquals("1", locateValue(tags.entrySet(), "initial1"));
        assertEquals("true", locateValue(tags.entrySet(), "initial2"));
        assertEquals("5", locateValue(tags.entrySet(), "initial3"));
        assertEquals("value1", locateValue(tags.entrySet(), "tag1"));
        assertEquals("true", locateValue(tags.entrySet(), "tag2"));
        assertEquals("37", locateValue(tags.entrySet(), "tag3"));
    }

    @SuppressWarnings("unchecked")
    private void whenAlwaysSampling() {
        new Expectations() {{
            sampleController.isSampled((SpanData<TestTraceContext>) any);
            result = true;
            minTimes = 0;
        }};
    }

    @Nullable
    private String locateValue(Iterable<Map.Entry<String, String>> entries, String key) {
        for (Map.Entry<String, String> entry : entries) {
            if (key.equals(entry.getKey()))
                return entry.getValue();
        }
        return null;
    }

    private int iterableSize(Iterable<?> entries) {
        int count = 0;
        for (Object entry : entries)
            count++;
        return count;
    }

}
