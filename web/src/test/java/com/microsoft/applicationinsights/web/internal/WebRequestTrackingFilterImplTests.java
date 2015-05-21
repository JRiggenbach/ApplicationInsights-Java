package com.microsoft.applicationinsights.web.internal;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.*;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by yonisha on 2/3/2015.
 */
public class WebRequestTrackingFilterImplTests {

    private class StubTelemetryClient extends TelemetryClient {
        public int trackExceptionCalled;

        public boolean shouldThrow;

        @Override
        public void trackException(Exception exception) {
            ++trackExceptionCalled;
            if (shouldThrow) {
                throw new RuntimeException();
            }
        }
    }

    private static class FilterAndTelemetryClientMock {
        public final Filter filter;
        public final StubTelemetryClient mockTelemetryClient;

        private FilterAndTelemetryClientMock(Filter filter, StubTelemetryClient mockTelemetryClient) {
            this.filter = filter;
            this.mockTelemetryClient = mockTelemetryClient;
        }
    }

    @Test
    public void testFilterInitializedSuccessfullyFromConfiguration() throws ServletException {
        Filter filter = createInitializedFilter();
        WebModulesContainer container = com.microsoft.applicationinsights.web.utils.ServletUtils.getWebModuleContainer(filter);

        Assert.assertNotNull("Container shouldn't be null", container);
        Assert.assertTrue("Modules container shouldn't be empty", container.getModulesCount() > 0);
    }

    @Test
    public void testFiltersChainWhenExceptionIsThrownOnModulesInvocation() throws Exception {
        Filter filter = createInitializedFilter();

        // mocking
        WebModulesContainer containerMock = com.microsoft.applicationinsights.web.utils.ServletUtils.setMockWebModulesContainer(filter);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                throw new Exception("FATAL!");
            }
        }).when(containerMock).invokeOnBeginRequest(any(ServletRequest.class), any(ServletResponse.class));

        FilterChain chain = mock(FilterChain.class);

        ServletRequest request = com.microsoft.applicationinsights.web.utils.ServletUtils.generateDummyServletRequest();

        // execute
        filter.doFilter(request, null, chain);

        // validate
        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void testUnhandledRuntimeExceptionWithTelemetryClient() throws IllegalAccessException, NoSuchFieldException, ServletException {
        FilterAndTelemetryClientMock createdData = createInitializedFilterWithTelemetryClient();
        testException(createdData, new RuntimeException());
    }

    @Test
    public void testUnhandledRuntimeExceptionWithoutTelemetryClient() throws IllegalAccessException, NoSuchFieldException, ServletException {
        FilterAndTelemetryClientMock createdData = createInitializedFilterWithoutTelemetryClient();
        testException(createdData, new RuntimeException());
    }

    @Test
    public void testUnhandledRuntimeExceptionWithTelemetryClientThatThrows() throws IllegalAccessException, NoSuchFieldException, ServletException {
        FilterAndTelemetryClientMock createdData = createInitializedFilterWithTelemetryClientThatThrows();
        testException(createdData, new RuntimeException());
    }

    @Test
    public void testUnhandledServletExceptionWithTelemetryClient() throws IllegalAccessException, NoSuchFieldException, ServletException {
        FilterAndTelemetryClientMock createdData = createInitializedFilterWithTelemetryClient();
        testException(createdData, new ServletException());
    }

    @Test
    public void testUnhandledServletExceptionWithoutTelemetryClient() throws IllegalAccessException, NoSuchFieldException, ServletException {
        FilterAndTelemetryClientMock createdData = createInitializedFilterWithoutTelemetryClient();
        testException(createdData, new ServletException());
    }

    @Test
    public void testUnhandledServletExceptionWithTelemetryClientThatThrows() throws IllegalAccessException, NoSuchFieldException, ServletException {
        FilterAndTelemetryClientMock createdData = createInitializedFilterWithTelemetryClientThatThrows();
        testException(createdData, new ServletException());
    }

    @Test
    public void testUnhandledIOExceptionWithTelemetryClient() throws IllegalAccessException, NoSuchFieldException, ServletException {
        FilterAndTelemetryClientMock createdData = createInitializedFilterWithTelemetryClient();
        testException(createdData, new IOException());
    }

    @Test
    public void testUnhandledIOExceptionWithoutTelemetryClient() throws IllegalAccessException, NoSuchFieldException, ServletException {
        FilterAndTelemetryClientMock createdData = createInitializedFilterWithoutTelemetryClient();
        testException(createdData, new IOException());
    }

    @Test
    public void testUnhandledIOExceptionWithTelemetryClientThatThrows() throws IllegalAccessException, NoSuchFieldException, ServletException {
        FilterAndTelemetryClientMock createdData = createInitializedFilterWithTelemetryClientThatThrows();
        testException(createdData, new IOException());
    }

    // region Private methods

    private void testException(FilterAndTelemetryClientMock createdData, Exception expectedException) throws NoSuchFieldException, IllegalAccessException, ServletException {

        try {
            FilterChain chain = mock(FilterChain.class);

            ServletRequest request = com.microsoft.applicationinsights.web.utils.ServletUtils.generateDummyServletRequest();
            Mockito.doThrow(expectedException).when(chain).doFilter(request, null);

            // execute
            createdData.filter.doFilter(request, null, chain);

            Assert.assertFalse("doFilter should have throw", true);
        } catch (Exception se) {
            Assert.assertSame(se, expectedException);

            if (createdData.mockTelemetryClient != null) {
                Assert.assertTrue(createdData.mockTelemetryClient.trackExceptionCalled == 1);
            }
        }
    }

    private Filter createInitializedFilter() throws ServletException {
        Filter filter = new WebRequestTrackingFilterImpl();
        filter.init(null);

        return filter;
    }

    private FilterAndTelemetryClientMock createInitializedFilterWithTelemetryClientThatThrows() throws ServletException, NoSuchFieldException, IllegalAccessException {
        return createInitializedFilterWithMockTelemetryClient(true, true);
    }

    private FilterAndTelemetryClientMock createInitializedFilterWithTelemetryClient() throws ServletException, NoSuchFieldException, IllegalAccessException {
        return createInitializedFilterWithMockTelemetryClient(true, false);
    }

    private FilterAndTelemetryClientMock createInitializedFilterWithoutTelemetryClient() throws ServletException, NoSuchFieldException, IllegalAccessException {
        return createInitializedFilterWithMockTelemetryClient(false, false);
    }

    private FilterAndTelemetryClientMock createInitializedFilterWithMockTelemetryClient(boolean withTelemetryClient, boolean clientThrows) throws ServletException, NoSuchFieldException, IllegalAccessException {
        Filter filter = createInitializedFilter();


        Field field = WebRequestTrackingFilterImpl.class.getDeclaredField("telemetryClient");
        field.setAccessible(true);

        StubTelemetryClient mockTelemetryClient = null;
        if (withTelemetryClient) {
            mockTelemetryClient = new StubTelemetryClient();
            if (clientThrows) {
                mockTelemetryClient.shouldThrow = true;
            }
        }
        field.set(filter, mockTelemetryClient);

        return new FilterAndTelemetryClientMock(filter, mockTelemetryClient);
    }
    // endregion Private methods
}
