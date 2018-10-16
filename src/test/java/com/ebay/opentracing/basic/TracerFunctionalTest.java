/*
 * Copyright (c) 2017-2018 eBay Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ebay.opentracing.basic;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Nullable;
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
    private FinishedSpanReceiver<TestTraceContext> finishedSpanReceiver;

    private Tracer uut;

    @Before
    public void before() throws Exception {
        uut = new BasicTracerBuilder<>(new TestTraceContextHandler(), finishedSpanReceiver)
                .build();
    }

    @Test
    public void noSpanOutsideOfTrace() {
        Span actual = uut.activeSpan();
        assertNull(actual);
    }

    @Test
    public void activeSpanWithinSpan() {
        try (Scope scope = uut.buildSpan("outer").startActive(true)) {
            Span actual = uut.activeSpan();
            assertSame(scope.span(), actual);
        }
    }

    @Test
    public void spanContextToStringDoesNotBlowUp() {
        try (Scope scope = uut.buildSpan("span").startActive(true)) {
            String string = scope.span().context().toString();
            assertNotNull(string);
        }
    }

    @Test
    public void spanAutoCloseCausesFinish() {
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 1;
        }};

        try (Scope scope = uut.buildSpan("spanOperation").startActive(true)) {
            // Do stuff
        }

        assertEquals(1, capturedSpanData.size());
    }

    @Test
    public void spanDataToStringDoesNotBlowUp() {
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
        }};

        try (Scope outerScope = uut.buildSpan("outerSpan").startActive(true)) {
            Span anotherSpan = uut.buildSpan("anotherSpan").start();
            anotherSpan.finish();

            try (Scope scope = uut.buildSpan("childSpan")
                    .asChildOf(outerScope.span())
                    .addReference(References.FOLLOWS_FROM, anotherSpan.context())
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .startActive(true)) {
                // Do stuff
            }
        }

        assertEquals(3, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(1);
        String string = spanData.toString();
        assertNotNull(string);
    }

    @Test
    public void spanBuilderAsChildOfNullSpanContextShouldNoop() {
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
        }};
        try (Scope scope = uut.buildSpan("outer")
                .asChildOf((SpanContext) null)
                .startActive(true)) {
        }
        assertEquals(1, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(0);
        List<? extends InternalSpanContext<TestTraceContext>> references = spanData.getReferences(References.CHILD_OF);
        assertNull(references);
    }

    @Test
    public void spanBuilderAsChildOfNullSpanShouldNoop() {
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
        }};
        try (Scope scope = uut.buildSpan("outer")
                .asChildOf((Span) null)
                .startActive(true)) {
        }
        assertEquals(1, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(0);
        List<? extends InternalSpanContext<TestTraceContext>> references = spanData.getReferences(References.CHILD_OF);
        assertNull(references);
    }

    @Test
    public void spanBuilderAddNullReferenceShouldNoop() {
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
        }};
        try (Scope scope = uut.buildSpan("outer")
                .addReference(References.CHILD_OF, null)
                .startActive(true)) {
        }
        assertEquals(1, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(0);
        List<? extends InternalSpanContext<TestTraceContext>> references = spanData.getReferences(References.CHILD_OF);
        assertNull(references);
    }

    @SuppressWarnings("deprecation") // Testing deprecated method in official API
    @Test
    public void spanLogEvents() {
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

        try (Scope scope = uut.buildSpan("spanOperation").startActive(true)) {
            Span span = scope.span();
            span.log("no timestamp");
            span.log(12345L, "with timestamp");
            span.log(mapWithoutTimestamp);
            span.log(54321L, mapWithTimestamp);
        }

        assertEquals(1, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(0);
        List<LogEvent> logEvents = spanData.getLogEvents();
        assertEquals(6, logEvents.size());
        List<String> logEventStrings = new ArrayList<>(logEvents.size());
        for (LogEvent logEvent : logEvents) {
            String eventString = logEvent.getEventName()
                    + "@" + logEvent.getTimeStamp(TimeUnit.MICROSECONDS)
                    + "=" + logEvent.getPayload();
            System.out.println("Saw event: " + eventString);
            logEventStrings.add(eventString);
        }
        assertTrue(logEventStrings.remove("event@12345=with timestamp"));
        assertTrue(logEventStrings.remove("with1@54321=val1"));
        assertTrue(logEventStrings.remove("with2@54321=val2"));
        for (String logEventString : logEventStrings) {
            if (logEventString.startsWith("without1@")) {
                assertTrue(logEventString.endsWith("=val1"));
            } else if (logEventString.startsWith("without2@")) {
                assertTrue(logEventString.endsWith("=val2"));
            } else if (logEventString.startsWith("event@")) {
                assertTrue(logEventString.endsWith("=no timestamp"));
            } else {
                fail("Unknown string: " + logEventString);
            }
        }
    }

    @Test
    public void spanStartAndFinishTimes() {
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 1;
        }};

        Span span = uut.buildSpan("spanOperation").withStartTimestamp(12345L).start();
        span.finish(13682L);

        assertEquals(1, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(0);
        assertEquals(12345L, spanData.getStartTime(TimeUnit.MICROSECONDS));
        assertEquals(13682L, spanData.getFinishTime(TimeUnit.MICROSECONDS));
    }

    @Test
    public void spanUpdatedOperationName() {
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 1;
        }};

        try (Scope scope = uut.buildSpan("initial").startActive(true)) {
            scope.span().setOperationName("expected");
        }

        assertEquals(1, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(0);
        assertEquals("expected", spanData.getOperationName());
    }

    @Test
    public void activeSpanWithinNestedSpan() throws Exception {
        try (Scope outerScope = uut.buildSpan("outer").startActive(true)) {
            try (Scope scope = uut.buildSpan("inner").startActive(true)) {
                Span actual = uut.activeSpan();
                assertSame(scope.span(), actual);
            }
        }
    }

    @Test
    public void sampledSpanIsReceived() {
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 1;
        }};
        try (Scope scope = uut.buildSpan("spanOperation").startActive(true)) {
            // Do stuff
        }

        assertEquals(1, capturedSpanData.size());
        SpanData<TestTraceContext> spanData = capturedSpanData.get(0);
        assertEquals("spanOperation", spanData.getOperationName());
    }

    @Test
    public void baggagePropagation() {
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 3;
        }};

        try (Scope outerScope= uut.buildSpan("outer").startActive(true)) {
            Span outer = outerScope.span();
            outer.setBaggageItem("outer", "1");
            assertEquals("1", outer.getBaggageItem("outer"));
            try (Scope middleScope = uut.buildSpan("middle").startActive(true)) {
                Span middle = middleScope.span();
                middle.setBaggageItem("middle", "2");
                assertEquals("1", middle.getBaggageItem("outer"));
                assertEquals("2", middle.getBaggageItem("middle"));
                assertNull(outer.getBaggageItem("middle"));
                try (Scope scope = uut.buildSpan("inner").startActive(true)) {
                    Span inner = scope.span();
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
            if ("outer".equals(operationName)) {
                assertEquals(1, iterableSize(baggageItems));
                assertEquals("1", locateValue(baggageItems, "outer"));
            } else if ("middle".equals(operationName)) {
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
        final ArrayList<SpanData<TestTraceContext>> capturedSpanData = new ArrayList<>();
        new Expectations() {{
            finishedSpanReceiver.spanFinished(withCapture(capturedSpanData));
            times = 1;
        }};
        try (Scope scope = uut.buildSpan("spanOperation")
                .withTag("initial1", "1")
                .withTag("initial2", true)
                .withTag("initial3", 5)
                .startActive(true)) {
            Span span = scope.span();
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

    @Test
    public void spanBuilderWithReferenceShouldCallHandlerWithReferencesWhenNoSpan() {
        final TestTraceContextHandler traceContextHandler = new TestTraceContextHandler();
        new Expectations(traceContextHandler)
        {{
            traceContextHandler.createNew();
            times = 1;

            traceContextHandler.createForContext((Map<String, List<InternalSpanContext<TestTraceContext>>>) any);
            times = 1;
        }};
        uut = new BasicTracerBuilder<>(traceContextHandler, finishedSpanReceiver)
                .build();

        Span parent = uut.buildSpan("parent").start();
        Span child = uut.buildSpan("child").asChildOf(parent.context()).start();

        child.finish();
        parent.finish();
    }

    @Test
    public void spanBuilderWithReferenceWhenIgnoreSpanShouldCallHandlerWithReferences() {
        final TestTraceContextHandler traceContextHandler = new TestTraceContextHandler();
        new Expectations(traceContextHandler)
        {{
            traceContextHandler.createNew();
            times = 1;

            traceContextHandler.createForContext((Map<String, List<InternalSpanContext<TestTraceContext>>>) any);
            times = 1;
        }};
        uut = new BasicTracerBuilder<>(traceContextHandler, finishedSpanReceiver)
                .build();

        try (Scope parentScope = uut.buildSpan("parent").startActive(true))
        {
            try (Scope childScope = uut.buildSpan("child")
                    .ignoreActiveSpan()
                    .asChildOf(parentScope.span().context())
                    .startActive(true))
            {
                // Empty
            }
        }
    }

    @Test
    public void spanInitiatorCanInterceptSpanCreation(
            @Mocked final SpanInitiator<TestTraceContext> spanInitiator,
            @Mocked final Span expected) {
        TestTraceContextHandler traceContextHandler = new TestTraceContextHandler();
        uut = new BasicTracerBuilder<>(traceContextHandler, finishedSpanReceiver)
                .spanInitiator(spanInitiator)
                .build();

        new Expectations() {{
            spanInitiator.initiateSpan(
                    withInstanceOf(SpanInitiatorContext.class),
                    withInstanceOf(MutableSpanData.class));
            result = expected;
        }};
        Span actual = uut.buildSpan("operation").start();
        assertSame(expected, actual);
    }

    @Test
    public void spanInitiatorCanManipulateSpanCreation()
    {
        TestTraceContextHandler traceContextHandler = new TestTraceContextHandler();
        SpanInitiator<TestTraceContext> testInitiator = new SpanInitiator<TestTraceContext>() {
            @Override
            public Span initiateSpan(SpanInitiatorContext<TestTraceContext> initiatorContext, MutableSpanData<TestTraceContext> spanData) {
                ScopeManager scopeManager = initiatorContext.getScopeManager();
                assertNotNull(scopeManager);

                spanData.putTag("manipulated", "yes");

                Span span = initiatorContext.createSpan(spanData);
                assertNotNull(span);
                return span;
            }
        };
        uut = new BasicTracerBuilder<>(traceContextHandler, finishedSpanReceiver)
                .spanInitiator(testInitiator)
                .build();

        uut.buildSpan("operation").start().finish();
        new Verifications() {{
            SpanData<TestTraceContext> captured;
            finishedSpanReceiver.spanFinished(captured = withCapture());

            Map<String, String> tags = captured.getTags();
            String actual = tags.get("manipulated");
            assertEquals("yes", actual);
        }};
    }

    @Nullable
    private String locateValue(Iterable<Map.Entry<String, String>> entries, String key) {
        for (Map.Entry<String, String> entry : entries) {
            if (key.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private int iterableSize(Iterable<?> entries) {
        int count = 0;
        for (Object entry : entries) {
            count++;
        }
        return count;
    }

}
