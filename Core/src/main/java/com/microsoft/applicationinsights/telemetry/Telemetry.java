package com.microsoft.applicationinsights.telemetry;

import com.microsoft.applicationinsights.telemetry.JsonSerializable;
import com.microsoft.applicationinsights.telemetry.JsonTelemetryDataSerializer;
import com.microsoft.applicationinsights.telemetry.TelemetryContext;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * The base telemetry type interface for application insights.
 */
public interface Telemetry extends JsonSerializable
{
    /**
     * Gets the time when telemetry was recorded
     */
    Date getTimestamp();
    /**
     * Sets the time when telemetry was recorded
     */
    void setTimestamp(Date date);

    /**
     * Gets the context associated with this telemetry instance.
     */
    TelemetryContext getContext();

    /**
     * Gets the map of application-defined property names and values.
     */
    Map<String,String> getProperties();

    /**
     * Sanitizes the properties of the telemetry item based on DP constraints.
     */
    void sanitize();

    /**
     * Serializes itself to Json using the {@link JsonTelemetryDataSerializer}
     * @param writer The writer that helps with serializing into Json format
     * @throws IOException a possible exception
     */
    void serialize(JsonTelemetryDataSerializer writer) throws IOException;
}
