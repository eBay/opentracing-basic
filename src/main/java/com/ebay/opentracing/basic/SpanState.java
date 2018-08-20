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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Object which encapsulates the mutable span state.  It also doubles as the implemnentation of the publicly facing
 * {@link SpanData} interface which is used to expose this information at the API surface.
 *
 * @param <T> trace context type
 */
final class SpanState<T> implements MutableSpanData<T> {

    private final InternalSpanContext<T> spanContext;
    private final TimeUnit startTimeUnit;
    private final long startTimeStamp;
    private final Map<String, List<InternalSpanContext<T>>> references;
    private String operationName;

    @Nullable
    private Map<String, String> tags;

    @Nullable
    private List<LogEvent> logs;

    @Nullable
    private TimeUnit finishTimeUnit;
    private long finishTimeStamp;

    SpanState(
            InternalSpanContext<T> spanContext,
            String operationName,
            TimeUnit startTimeUnit,
            long startTimeStamp,
            @Nullable Map<String, String> tags,
            Map<String, List<InternalSpanContext<T>>> references
    ) {
        this.spanContext = Objects.requireNonNull(spanContext);
        this.operationName = Objects.requireNonNull(operationName);
        this.startTimeUnit = Objects.requireNonNull(startTimeUnit);
        this.startTimeStamp = startTimeStamp;
        this.tags = tags;
        this.references = Objects.requireNonNull(references);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InternalSpanContext<T> getSpanContext() {
        return spanContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getStartTime(TimeUnit timeUnit) {
        return timeUnit.convert(startTimeStamp, startTimeUnit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFinishTime(TimeUnit timeUnit) {
        return timeUnit.convert(finishTimeStamp, finishTimeUnit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOperationName() {
        return operationName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOperationName(String operationName) {
        this.operationName = Objects.requireNonNull(
                operationName, "operationName may not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getTags() {
        return (tags == null) ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends InternalSpanContext<T>> getReferences(String referenceType) {
        return references.get(referenceType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LogEvent> getLogEvents() {
        return (logs == null) ? Collections.<LogEvent>emptyList() : logs;
    }

    /**
     * {@inheritDoc}
     * <p>
     * NOTE: This implementation dumps most of the span state but avoid logging values as these may contain
     * sensitive information and inadvertently end up in logs.
     */
    public String toString() {
        StringBuilder builder = new StringBuilder("Span{operationName='")
                .append(operationName)
                .append("'");

        List<InternalSpanContext<T>> childOfList = references.get(References.CHILD_OF);
        if (childOfList != null && !childOfList.isEmpty()) {
            builder.append(",childOf=[");
            for (int i = 0; i < childOfList.size(); i++) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(childOfList.get(i));
            }
            builder.append("]");
        }
        List<InternalSpanContext<T>> followsFromList = references.get(References.FOLLOWS_FROM);
        if (followsFromList != null && !followsFromList.isEmpty()) {
            builder.append(",followsFrom=[");
            for (int i = 0; i < followsFromList.size(); i++) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(followsFromList.get(i));
            }
            builder.append("]");
        }

        builder.append(",startTimeMs=")
                .append(TimeUnit.MILLISECONDS.convert(startTimeStamp, startTimeUnit));
        if (finishTimeUnit != null) {
            builder.append(",finishTimeMs=")
                    .append(TimeUnit.MILLISECONDS.convert(finishTimeStamp, finishTimeUnit));
        }

        if (tags != null) {
            Set<String> tagKeys = tags.keySet();
            if (!tags.isEmpty()) {
                builder.append(",tags=[");
                Iterator<String> tagIterator = tagKeys.iterator();
                while (tagIterator.hasNext()) {
                    builder.append(tagIterator.next());
                    if (tagIterator.hasNext()) {
                        builder.append(",");
                    }
                }
                builder.append("]");
            }
        }

        return builder.append("}").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFinishTime(TimeUnit finishTimeUnit, long finishTimeStamp) {
        this.finishTimeUnit = Objects.requireNonNull(finishTimeUnit, "finishTimeUnit may not be null");
        this.finishTimeStamp = finishTimeStamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putTag(String key, String value) {
        if (tags == null) {
            tags = Collections.synchronizedMap(new HashMap<String, String>());
        }
        tags.put(key, value);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLogEvent(LogEvent logEvent) {
        if (logs == null) {
            logs = Collections.synchronizedList(new ArrayList<LogEvent>(5));
        }
        logs.add(logEvent);
    }

}
