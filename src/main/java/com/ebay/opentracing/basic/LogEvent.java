/*
 * Copyright (c) 2017 eBay Inc.
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

import java.util.concurrent.TimeUnit;

/**
 * An individual record of something being logged.  This is a tuple of a key/value pair and an associated
 * timestamp.
 */
@SuppressWarnings("WeakerAccess") // This is an API class
public final class LogEvent {
    private final TimeUnit timeStampUnit;
    private final long timeStampValue;
    private final String eventName;
    private final Object payload;

    LogEvent(TimeUnit timeStampUnit, long timeStampValue, String eventName, Object payload) {
        this.timeStampUnit = timeStampUnit;
        this.timeStampValue = timeStampValue;
        this.eventName = eventName;
        this.payload = payload;
    }

    /**
     * Get the time at which the event took place.
     *
     * @param unit time unit
     * @return time value
     */
    public long getTimeStamp(TimeUnit unit) {
        return unit.convert(timeStampValue, timeStampUnit);
    }

    /**
     * Get the event name.
     *
     * @return event name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Get the event payload.
     *
     * @return event payload
     */
    public Object getPayload() {
        return payload;
    }

}
