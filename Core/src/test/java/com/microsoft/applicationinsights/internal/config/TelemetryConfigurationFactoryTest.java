package com.microsoft.applicationinsights.internal.config;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import com.microsoft.applicationinsights.TelemetryClientConfiguration;
import com.microsoft.applicationinsights.extensibility.ContextInitializer;
import com.microsoft.applicationinsights.extensibility.TelemetryInitializer;
import com.microsoft.applicationinsights.internal.channel.stdout.StdOutChannel;
import com.microsoft.applicationinsights.channel.TelemetryChannel;

import com.microsoft.applicationinsights.internal.config.ConfigFileParser;
import com.microsoft.applicationinsights.internal.config.TelemetryConfigurationFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

public class TelemetryConfigurationFactoryTest {

    private final static String MOCK_CONF_FILE = "mockFileName";
    private final static String MOCK_IKEY = "mockikey";
    private final static String MOCK_XML_IKEY = "A-test-instrumentation-key";
    private final static String MOCK_XML_ENDPOINT = "test-endpoint";
    private final static String MOCK_ENDPOINT = "MockEndpoint";
    private final static String FACTORY_INSTRUMENTATION_KEY = "InstrumentationKey";

    private final static class StubTelemetryConfiguration implements TelemetryClientConfiguration {
        private final ArrayList<ContextInitializer> contextInitializers = new ArrayList<ContextInitializer>();
        private final ArrayList<TelemetryInitializer> telemetryInitializers = new ArrayList<TelemetryInitializer>();
        private boolean developerMode;
        private boolean trackingIsDisabled;
        private String instrumentationKey;
        private String endpoint;
        private TelemetryChannel channel;

        public String getInstrumentationKey() {
            return instrumentationKey;
        }

        @Override
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public String getEndpoint() {
            return endpoint;
        }

        @Override
        public void setChannel(TelemetryChannel channel) {
            this.channel = channel;
        }

        @Override
        public void setTrackingIsDisabled(boolean trackingIsDisabled) {
            this.trackingIsDisabled = trackingIsDisabled;
        }

        @Override
        public void setDeveloperMode(boolean developerMode) {
            this.developerMode = developerMode;
        }

        @Override
        public void setInstrumentationKey(String instrumentationKey) {
            this.instrumentationKey = instrumentationKey;
        }

        @Override
        public List<ContextInitializer> getContextInitializers() {
            return contextInitializers;
        }

        @Override
        public List<TelemetryInitializer> getTelemetryInitializers() {
            return telemetryInitializers;
        }

        public boolean isDeveloperMode() {
            return developerMode;
        }

        public boolean isTrackingIsDisabled() {
            return trackingIsDisabled;
        }

        public TelemetryChannel getChannel() {
            return channel;
        }
    }

    @Ignore("This test uses test file we need first to make sure it is accessible in other build machines")
    @Test
    public void testInitializeWithXML() throws Exception {
        StubTelemetryConfiguration mockConfiguration = new StubTelemetryConfiguration();

        TelemetryConfigurationFactory.INSTANCE.initialize(mockConfiguration);

        assertFalse(mockConfiguration.isDeveloperMode());
        assertFalse(mockConfiguration.isTrackingIsDisabled());
        assertEquals(mockConfiguration.contextInitializers.size(), 3);
        assertEquals(mockConfiguration.telemetryInitializers.size(), 0);
        assertEquals(mockConfiguration.getInstrumentationKey(), MOCK_XML_IKEY);
        assertTrue(mockConfiguration.getChannel() instanceof StdOutChannel);
        assertEquals(mockConfiguration.getEndpoint(), MOCK_XML_ENDPOINT);
    }

    @Test
    public void testInitializeWithFailingParse() throws Exception {
        ConfigFileParser mockParser = createMockParser(false);

        TelemetryClientConfiguration mockConfiguration = Mockito.mock(TelemetryClientConfiguration.class);

        initializeWithFactory(mockParser, mockConfiguration);

        Mockito.verify(mockParser, Mockito.times(1)).parse(MOCK_CONF_FILE);
        Mockito.verify(mockConfiguration, Mockito.times(1)).setTrackingIsDisabled(false);
        Mockito.verify(mockConfiguration, Mockito.times(1)).setChannel((TelemetryChannel)anyObject());
    }

    @Test
    public void testInitializeWithNullGetInstrumentationKey() throws Exception {
        ConfigFileParser mockParser = createMockParser(false);
        Mockito.doReturn(null).when(mockParser).getTrimmedValue(FACTORY_INSTRUMENTATION_KEY);

        TelemetryClientConfiguration mockConfiguration = Mockito.mock(TelemetryClientConfiguration.class);

        initializeWithFactory(mockParser, mockConfiguration);

        Mockito.verify(mockParser, Mockito.times(1)).parse(MOCK_CONF_FILE);
        Mockito.verify(mockConfiguration, Mockito.times(1)).setTrackingIsDisabled(false);
        Mockito.verify(mockConfiguration, Mockito.times(1)).setChannel((TelemetryChannel)anyObject());
    }

    @Test
    public void testInitializeWithEmptyGetInstrumentationKey() throws Exception {
        ConfigFileParser mockParser = createMockParser(false);
        Mockito.doReturn("").when(mockParser).getTrimmedValue(FACTORY_INSTRUMENTATION_KEY);

        TelemetryClientConfiguration mockConfiguration = Mockito.mock(TelemetryClientConfiguration.class);

        initializeWithFactory(mockParser, mockConfiguration);

        Mockito.verify(mockParser, Mockito.times(1)).parse(MOCK_CONF_FILE);
        Mockito.verify(mockConfiguration, Mockito.times(1)).setTrackingIsDisabled(false);
        Mockito.verify(mockConfiguration, Mockito.times(1)).setChannel((TelemetryChannel)anyObject());
    }

    @Test
    public void testInitializeAllDefaults() throws Exception {
        ConfigFileParser mockParser = createMockParser(true);
        Mockito.doReturn(MOCK_IKEY).when(mockParser).getTrimmedValue(FACTORY_INSTRUMENTATION_KEY);


        TelemetryClientConfiguration mockConfiguration = Mockito.mock(TelemetryClientConfiguration.class);

        initializeWithFactory(mockParser, mockConfiguration);

        Mockito.verify(mockParser, Mockito.times(1)).parse(MOCK_CONF_FILE);
        Mockito.verify(mockConfiguration, Mockito.times(1)).setInstrumentationKey(MOCK_IKEY);
        Mockito.verify(mockConfiguration, Mockito.times(1)).setTrackingIsDisabled(false);
        Mockito.verify(mockConfiguration, Mockito.times(1)).getContextInitializers();
        Mockito.verify(mockConfiguration, Mockito.times(1)).getTelemetryInitializers();
        Mockito.verify(mockConfiguration, Mockito.times(1)).setDeveloperMode(false);
        Mockito.verify(mockConfiguration, Mockito.times(1)).setChannel(any(StdOutChannel.class));
    }

    @Test
    public void testInitializeWithInitializers() throws Exception {
        ConfigFileParser mockParser = createMockParser(true);
        Mockito.doReturn(MOCK_IKEY).when(mockParser).getTrimmedValue(FACTORY_INSTRUMENTATION_KEY);

        List<String> mockContextInitializers = new ArrayList<String>();
        mockContextInitializers.add("com.microsoft.applicationinsights.extensibility.initializer.SdkVersionContextInitializer");
        Mockito.doReturn(mockContextInitializers).
                when(mockParser).getList("ContextInitializers", "Add", "Type");
        StubTelemetryConfiguration mockConfiguration = new StubTelemetryConfiguration();

        TelemetryConfigurationFactory.INSTANCE.setParserData(mockParser, MOCK_CONF_FILE);
        TelemetryConfigurationFactory.INSTANCE.initialize(mockConfiguration);

        Mockito.verify(mockParser, Mockito.times(1)).parse(MOCK_CONF_FILE);

        assertFalse(mockConfiguration.isDeveloperMode());
        assertFalse(mockConfiguration.isTrackingIsDisabled());
        assertEquals(mockConfiguration.contextInitializers.size(), 4);
        assertEquals(mockConfiguration.telemetryInitializers.size(), 0);
        assertEquals(mockConfiguration.getInstrumentationKey(), MOCK_IKEY);
    }

    // Suppress non relevant warning due to mockito internal stuff
    @SuppressWarnings("unchecked")
    private ConfigFileParser createMockParser(boolean withChannel) {
        ConfigFileParser mockParser = Mockito.mock(ConfigFileParser.class);
        Mockito.doReturn(true).when(mockParser).parse(MOCK_CONF_FILE);

        if (withChannel) {
            Map<String, String> mockChannel = new HashMap<String, String>();
            mockChannel.put("Type", "com.microsoft.applicationinsights.internal.channel.stdout.StdOutChannel");
            mockChannel.put("EndpointAddress", MOCK_ENDPOINT);

            Mockito.doReturn(mockChannel).when(mockParser).getStructuredData(anyString(), (Set<String>) any());
        }

        return mockParser;
    }

    private void initializeWithFactory(ConfigFileParser mockParser, TelemetryClientConfiguration mockConfiguration) {
        TelemetryConfigurationFactory.INSTANCE.setParserData(mockParser, MOCK_CONF_FILE);
        TelemetryConfigurationFactory.INSTANCE.initialize(mockConfiguration);
    }
}