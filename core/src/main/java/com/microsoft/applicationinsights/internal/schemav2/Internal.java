/*
* ApplicationInsights-Java
* Copyright (c) Microsoft Corporation
* All rights reserved.
*
* MIT License
* Permission is hereby granted, free of charge, to any person obtaining a copy of this
* software and associated documentation files (the ""Software""), to deal in the Software
* without restriction, including without limitation the rights to use, copy, modify, merge,
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit
* persons to whom the Software is furnished to do so, subject to the following conditions:
* The above copyright notice and this permission notice shall be included in all copies or
* substantial portions of the Software.
* THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
* DEALINGS IN THE SOFTWARE.
*/
/*
 * Generated from ContextTagKeys.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.internal.schemav2;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import com.microsoft.applicationinsights.telemetry.JsonSerializable;
import com.microsoft.applicationinsights.telemetry.Duration;
import com.microsoft.applicationinsights.telemetry.JsonTelemetryDataSerializer;
import com.google.common.base.Preconditions;

/**
 * Data contract class Internal.
 */
public class Internal
    implements JsonSerializable
{
    /**
     * Backing field for property SdkVersion.
     */
    private String sdkVersion;
    
    /**
     * Backing field for property AgentVersion.
     */
    private String agentVersion;
    
    /**
     * Backing field for property NodeName.
     */
    private String nodeName;
    
    /**
     * Initializes a new instance of the Internal class.
     */
    public Internal()
    {
        this.InitializeFields();
    }
    
    /**
     * Gets the SdkVersion property.
     */
    public String getSdkVersion() {
        return this.sdkVersion;
    }
    
    /**
     * Sets the SdkVersion property.
     */
    public void setSdkVersion(String value) {
        this.sdkVersion = value;
    }
    
    /**
     * Gets the AgentVersion property.
     */
    public String getAgentVersion() {
        return this.agentVersion;
    }
    
    /**
     * Sets the AgentVersion property.
     */
    public void setAgentVersion(String value) {
        this.agentVersion = value;
    }
    
    /**
     * Gets the NodeName property.
     */
    public String getNodeName() {
        return this.nodeName;
    }
    
    /**
     * Sets the NodeName property.
     */
    public void setNodeName(String value) {
        this.nodeName = value;
    }
    

    /**
     * Adds all members of this class to a hashmap
     * @param map to which the members of this class will be added.
     */
    public void addToHashMap(HashMap<String, String> map)
    {
        if (this.sdkVersion != null) {
            map.put("sdkVersion", this.sdkVersion);
        }
        if (this.agentVersion != null) {
            map.put("agentVersion", this.agentVersion);
        }
        if (this.nodeName != null) {
            map.put("nodeName", this.nodeName);
        }
    }
    

    /**
     * Serializes the beginning of this object to the passed in writer.
     * @param writer The writer to serialize this object to.
     */
    @Override
    public void serialize(JsonTelemetryDataSerializer writer) throws IOException
    {
        Preconditions.checkNotNull(writer, "writer must be a non-null value");
        this.serializeContent(writer);
    }

    /**
     * Serializes the beginning of this object to the passed in writer.
     * @param writer The writer to serialize this object to.
     */
    protected void serializeContent(JsonTelemetryDataSerializer writer) throws IOException
    {
        writer.write("sdkVersion", sdkVersion);
        writer.write("agentVersion", agentVersion);
        writer.write("nodeName", nodeName);
    }
    
    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {
        
    }
}
