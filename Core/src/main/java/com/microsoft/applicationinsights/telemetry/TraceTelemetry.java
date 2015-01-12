package com.microsoft.applicationinsights.telemetry;

import com.microsoft.applicationinsights.internal.schemav2.MessageData;
import com.microsoft.applicationinsights.internal.util.LocalStringsUtils;

/**
 * Telemetry used to track events.
 */
public final class TraceTelemetry extends BaseTelemetry<MessageData> {
    private final MessageData data;

    public TraceTelemetry() {
        super();
        this.data = new MessageData();
        initialize(this.data.getProperties());
    }

    public TraceTelemetry(String message) {
        this();
        this.setMessage(message);
    }

    public void setMessage(String message) {
        this.data.setMessage(message);
    }

    @Override
    protected void additionalSanitize() {
        this.data.setMessage(LocalStringsUtils.sanitize(this.data.getMessage(), 32768));
    }

    @Override
    protected MessageData getData() {
        return data;
    }
}
