package com.microsoft.applicationinsights.channel;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;

public class TelemetryBufferTest {
    private static class MockSender implements TelemetriesTransmitter {
        private static class ScheduledSendResult {
            public final boolean result;

            public final String message;

            private ScheduledSendResult(boolean result, String message) {
                this.result = result;
                this.message = message;
            }
        }

        private AtomicInteger sendNowCallCounter = new AtomicInteger(0);

        private AtomicInteger scheduleSendCallCounter = new AtomicInteger(0);

        private AtomicInteger scheduleSendActualCallCounter = new AtomicInteger(0);

        private int expectedTelemetriesNumberInSendNow;
        private int expectedTelemetriesNumberInScheduleSend;
        private int expectedNumberOfSendNowCalls;
        private int expectedNumberOfScheduleSendCalls;

        private ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

        private final BlockingQueue<ScheduledSendResult> queue = new ArrayBlockingQueue<ScheduledSendResult>(4);

        public MockSender setExpectedTelemetriesNumberInSendNow(int expectedTelemetriesNumberInSendNow) {
            this.expectedTelemetriesNumberInSendNow = expectedTelemetriesNumberInSendNow;
            return this;
        }

        public MockSender setExpectedTelemetriesNumberInScheduleSend(int expectedTelemetriesNumberInScheduleSend) {
            this.expectedTelemetriesNumberInScheduleSend = expectedTelemetriesNumberInScheduleSend;
            return this;
        }

        public MockSender setExpectedNumberOfSendNowCalls(int expectedNumberOfSendNowCalls) {
            this.expectedNumberOfSendNowCalls = expectedNumberOfSendNowCalls;
            return this;
        }

        public MockSender setExpectedNumberOfScheduleSendCalls(int expectedNumberOfScheduleSendCalls) {
            this.expectedNumberOfScheduleSendCalls = expectedNumberOfScheduleSendCalls;
            return this;
        }

        @Override
        public void scheduleSend(final TelemetriesTransmitter.TelemetriesFetcher telemetriesFetcher, long value, TimeUnit timeUnit) {
            assertNotNull(telemetriesFetcher);

            int called = scheduleSendCallCounter.incrementAndGet();
            assertEquals(called, 1);

            assertEquals(value, 10);

            assertEquals(timeUnit, TimeUnit.SECONDS);

            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    scheduleSendActualCallCounter.incrementAndGet();

                    Collection<Telemetry> telemetries = telemetriesFetcher.fetch();
                    if (telemetries == null) {
                        queue.offer(new ScheduledSendResult(false, "Telemetries is null"));
                        return;
                    }

                    if (telemetries.size() != expectedTelemetriesNumberInScheduleSend) {
                        queue.offer(new ScheduledSendResult(false, "Telemetries size is wrong"));
                        return;
                    }

                    queue.offer(new ScheduledSendResult(true, ""));
                }
            }, value, timeUnit);
        }

        @Override
        public void sendNow(Collection<Telemetry> telemetries) {
            int called = sendNowCallCounter.incrementAndGet();
            assertEquals("Wrong number of scheduled sends by the TransmissionBuffer", called, expectedNumberOfSendNowCalls);

            assertNotNull("Unexpected null value for telemetries container", telemetries);
            assertEquals("Wrong size of telemetries container", telemetries.size(), expectedTelemetriesNumberInSendNow);
        }

        @Override
        public void stop(long timeout, TimeUnit timeUnit) {

        }

        public void waitForFinish(long timeToWaitInSeconds) {
            try {
                ScheduledSendResult result = queue.poll(timeToWaitInSeconds, TimeUnit.SECONDS);
                scheduler.shutdownNow();
                assertTrue(result.message, result.result);

                assertEquals("Wrong number of calls by timer", scheduleSendActualCallCounter.get(), expectedNumberOfScheduleSendCalls);
                assertEquals("Wrong number of calls of send now", sendNowCallCounter.get(), expectedNumberOfSendNowCalls);

                assertEquals("Wrong number of scheduled sends by the TransmissionBuffer", scheduleSendCallCounter.get(), 1);
                assertEquals("Wrong number of sending full buffers by the TransmissionBuffer", sendNowCallCounter.get(), expectedNumberOfSendNowCalls);
            } catch (InterruptedException e) {
                assertTrue(false);
            }
        }
    };

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeBufferSizeSenderIsSet() throws Exception {
        TelemetriesTransmitter mockSender = Mockito.mock(TelemetriesTransmitter.class);
        TelemetryBuffer testedBuffer = new TelemetryBuffer(mockSender, -1, 20, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroBufferSizeSenderIsSet() throws Exception {
        TelemetriesTransmitter mockSender = Mockito.mock(TelemetriesTransmitter.class);
        TelemetryBuffer testedBuffer = new TelemetryBuffer(mockSender, 0, 20, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeBufferTimeoutSenderIsSet() throws Exception {
        TelemetriesTransmitter mockSender = Mockito.mock(TelemetriesTransmitter.class);
        TelemetryBuffer testedBuffer = new TelemetryBuffer(mockSender, 1, -20, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroBufferTimeoutSenderIsSet() throws Exception {
        TelemetriesTransmitter mockSender = Mockito.mock(TelemetriesTransmitter.class);
        TelemetryBuffer testedBuffer = new TelemetryBuffer(mockSender, 20, 0, false);
    }

    @Test(expected = NullPointerException.class)
    public void testNoSenderIsSet() throws Exception {
        TelemetryBuffer testedBuffer = new TelemetryBuffer(null, false);
    }

    @Test
    public void testAddOneTelemetry() throws Exception {
        TelemetriesTransmitter mockSender = Mockito.mock(TelemetriesTransmitter.class);

        TelemetryBuffer testedBuffer = new TelemetryBuffer(mockSender, false);

        Telemetry mockTelemetry = Mockito.mock(Telemetry.class);
        testedBuffer.add(mockTelemetry);

        Mockito.verify(mockSender, Mockito.times(1)).scheduleSend((TelemetriesTransmitter.TelemetriesFetcher)any(), anyLong(), (TimeUnit)anyObject());
    }

    @Test
    public void testSendWhenBufferIsFullInNonDeveloperMode() throws Exception {
        TelemetriesTransmitter mockSender = Mockito.mock(TelemetriesTransmitter.class);

        TelemetryBuffer testedBuffer = new TelemetryBuffer(mockSender, 2, 1200, false);

        for (int i = 0; i < 2; ++i) {
            Telemetry mockTelemetry = Mockito.mock(Telemetry.class);
            testedBuffer.add(mockTelemetry);
        }

        Mockito.verify(mockSender, Mockito.times(1)).scheduleSend((TelemetriesTransmitter.TelemetriesFetcher) any(), anyLong(), (TimeUnit) anyObject());
        Mockito.verify(mockSender, Mockito.times(1)).sendNow(anyCollectionOf(Telemetry.class));
    }

    @Test
    public void testSendWhenBufferIsFullInDeveloperMode() throws Exception {
        TelemetriesTransmitter mockSender = Mockito.mock(TelemetriesTransmitter.class);

        TelemetryBuffer testedBuffer = new TelemetryBuffer(mockSender, 2, 1200, true);

        for (int i = 0; i < 2; ++i) {
            Telemetry mockTelemetry = Mockito.mock(Telemetry.class);
            testedBuffer.add(mockTelemetry);
        }

        Mockito.verify(mockSender, Mockito.never()).scheduleSend((TelemetriesTransmitter.TelemetriesFetcher)any(), anyLong(), (TimeUnit)anyObject());
        Mockito.verify(mockSender, Mockito.times(2)).sendNow(anyCollectionOf(Telemetry.class));
    }

    @Test
    public void testSendBufferAfterTimeoutExpires() throws Exception {

        MockSender mockSender = new MockSender()
                .setExpectedNumberOfScheduleSendCalls(1)
                .setExpectedNumberOfSendNowCalls(0)
                .setExpectedTelemetriesNumberInScheduleSend(1)
                .setExpectedTelemetriesNumberInSendNow(1);

        // Create a buffer with max buffer size of 10 and timeout of 10 seconds
        TelemetryBuffer testedBuffer = new TelemetryBuffer(mockSender, 10, 10, false);

        for (int i = 0; i < 1; ++i) {
            Telemetry mockTelemetry = Mockito.mock(Telemetry.class);
            testedBuffer.add(mockTelemetry);
        }

        mockSender.waitForFinish(20L);
    }

    @Test
    public void testSendBufferAfterTimeoutExpiresButBufferWasAlreadySent() throws Exception {
        MockSender mockSender = new MockSender()
                .setExpectedNumberOfScheduleSendCalls(1)
                .setExpectedNumberOfSendNowCalls(1)
                .setExpectedTelemetriesNumberInScheduleSend(0)
                .setExpectedTelemetriesNumberInSendNow(10);

        // Create a buffer with max buffer size of 10 and timeout of 10 seconds
        TelemetryBuffer testedBuffer = new TelemetryBuffer(mockSender, 10, 10, false);

        for (int i = 0; i < 10; ++i) {
            Telemetry mockTelemetry = Mockito.mock(Telemetry.class);
            testedBuffer.add(mockTelemetry);
        }

        mockSender.waitForFinish(20L);
    }
}
