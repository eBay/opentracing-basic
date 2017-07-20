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
