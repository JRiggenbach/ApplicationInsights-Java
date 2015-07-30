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

package com.microsoft.applicationinsights.web.spring;

import com.microsoft.applicationinsights.test.framework.ApplicationTelemetryManager;
import com.microsoft.applicationinsights.test.framework.HttpRequestClient;
import com.microsoft.applicationinsights.test.framework.TestEnvironment;
import com.microsoft.applicationinsights.test.framework.UriWithExpectedResult;
import com.microsoft.applicationinsights.test.framework.telemetries.RequestTelemetryItem;
import com.microsoft.applicationinsights.test.framework.telemetries.TelemetryItem;
import com.microsoft.applicationinsights.internal.util.LocalStringsUtils;
import com.microsoft.applicationinsights.web.utils.HttpHelper;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.text.ParseException;
import java.util.*;

/**
 * Created by moralt on 05/05/2015.
 */
public class WebTelemetryTests {

    // region Tests run IDs

    private static String testRequestCommonPropertiesRunId = LocalStringsUtils.generateRandomId(false);
    private static String testPageNotFoundResponseCodeRunId = LocalStringsUtils.generateRandomId(false);
    private static String testRequestCorrelationRunId = LocalStringsUtils.generateRandomId(false);

    // endregion Tests run IDs

    private static TestEnvironment testEnv;
    private static ApplicationTelemetryManager applicationTelemetryManager;
    private static List<TelemetryItem> allExpectedTelemetries = new ArrayList<TelemetryItem>();

    @BeforeClass
    public static void classInitialization() throws Exception {
        testEnv = new TestEnvironment();
        TestSettings testSettings = new TestSettings();

        applicationTelemetryManager = new ApplicationTelemetryManager(
                testEnv.getApplicationStorageConnectionString(),
                testEnv.getApplicationStorageExportQueueName(),
                testSettings.getMaxWaitTime(),
                testSettings.getPollingInterval(),
                testSettings.getMessageBatchSize());

        // It takes time to telemetries to be exported. Therefore, we're sending requests required for all tests in order
        // to save several minutes during tests execution.
        sendRequestsForAllTests();
    }

    /**
     * Sends GET requests to server and expects that will telemetry from app insights and it will include the correct information about the request
     * @throws Exception
     */
    @Test
    public void testHttpRequestCommonProperties() throws Exception {
        List<TelemetryItem> expectedTelemetriesForRunId = getExpectedTelemetriesForRunId(testRequestCommonPropertiesRunId);
        List<TelemetryItem> realTelemetries = applicationTelemetryManager.getApplicationTelemetries(testRequestCommonPropertiesRunId, 1);

        Assert.assertEquals(1, realTelemetries.size());
        Assert.assertEquals(expectedTelemetriesForRunId.get(0), realTelemetries.get(0));
    }

    @Test
    public void testPageNotFoundReturnsCorrectResponseCode() throws Exception {
        List<TelemetryItem> expectedTelemetriesForRunId = getExpectedTelemetriesForRunId(testPageNotFoundResponseCodeRunId);
        List<TelemetryItem> realTelemetries = applicationTelemetryManager.getApplicationTelemetries(testPageNotFoundResponseCodeRunId, 1);

        Assert.assertEquals(1, realTelemetries.size());
        Assert.assertEquals(expectedTelemetriesForRunId.get(0), realTelemetries.get(0));
    }

    @Test
    public void testRequestCorrelationWithCustomTelemetry() throws Exception {
        final int expectedTelemetries = 2;

        List<TelemetryItem> realTelemetries = applicationTelemetryManager.getApplicationTelemetries(testRequestCorrelationRunId, expectedTelemetries);

        Assert.assertEquals(expectedTelemetries, realTelemetries.size());

        TelemetryItem firstTelemetryItem = realTelemetries.get(0);
        TelemetryItem secondTelemetryItem = realTelemetries.get(1);

        // Validating that the two telemetries are correlated.
        Assert.assertEquals(firstTelemetryItem.getProperty("operationId"), secondTelemetryItem.getProperty("operationId"));
        Assert.assertEquals(firstTelemetryItem.getProperty("operationName"), secondTelemetryItem.getProperty("operationName"));

        RequestTelemetryItem requestTelemetryItem =
                (RequestTelemetryItem) (firstTelemetryItem instanceof RequestTelemetryItem ? firstTelemetryItem : secondTelemetryItem);

        // Validating that the operation name equals the request name.
        Assert.assertEquals(requestTelemetryItem.getProperty("requestName"), requestTelemetryItem.getProperty("operationName"));
    }

    private static List<TelemetryItem> sendHttpGetRequests(List<UriWithExpectedResult> uriPathsToRequest) throws Exception {
        List<TelemetryItem> expectedTelemetries = new ArrayList<TelemetryItem>();

        for (UriWithExpectedResult uriWithExpectedResult : uriPathsToRequest) {
            String requestUri = uriWithExpectedResult.getUri();
            boolean isFirstParameter = !requestUri.contains("?");

            String runId = uriWithExpectedResult.getRunId();
            String uriWithRunId = requestUri.concat(isFirstParameter ? "?" : "&").concat("runId=").concat(runId);

            // TODO: request ID can can be generated by the HTTP client.
            String requestId = LocalStringsUtils.generateRandomId(false);
            String uriWithRequestId = uriWithRunId.concat("&requestId=").concat(requestId);

            URI fullRequestUri = HttpRequestClient.constructUrl(
                    testEnv.getApplicationServer(),
                    testEnv.getApplicationServerPort(),
                    testEnv.getApplicationName(), uriWithRequestId);

            List<String> requestHeaders = generateUserAndSessionCookieHeader();
            int responseCode = HttpRequestClient.sendHttpRequest(fullRequestUri, requestHeaders);

            int expectedResponseCode = uriWithExpectedResult.getExpectedResponseCode();
            if (responseCode != expectedResponseCode) {
                String errorMessage = String.format(
                        "Unexpected response code '%s' for URI: %s. Expected: %s.", responseCode, uriWithExpectedResult.getUri(), expectedResponseCode);

                Assert.fail(errorMessage);
            }

            TelemetryItem expectedTelemetry = createExpectedResult(fullRequestUri, runId, requestId, responseCode, uriWithExpectedResult.getExpectedRequestName());
            expectedTelemetries.add(expectedTelemetry);
        }

        return expectedTelemetries;
    }

    private static List<String> generateUserAndSessionCookieHeader() throws ParseException {
        String formattedUserCookieHeader = HttpHelper.getFormattedUserCookieHeader();
        String formattedSessionCookie = HttpHelper.getFormattedSessionCookie(false);

        List<String> cookiesList = new ArrayList<String>();
        cookiesList.add(formattedUserCookieHeader);
        cookiesList.add(formattedSessionCookie);

        return cookiesList;
    }

    private static TelemetryItem createExpectedResult(URI uri, String runId, String requestId, int responseCode, String requestName) {
        final String expectedUserAndSessionId = "00000000-0000-0000-0000-000000000000";

        TelemetryItem telemetryItem = new RequestTelemetryItem();
        telemetryItem.setProperty("requestId", requestId);
        telemetryItem.setProperty("runId", runId);
        telemetryItem.setProperty("port", Integer.toString(uri.getPort()));
        telemetryItem.setProperty("responseCode", Integer.toString(responseCode));
        telemetryItem.setProperty("uri", uri.toString());
        telemetryItem.setProperty("sessionId", expectedUserAndSessionId);
        telemetryItem.setProperty("userId", expectedUserAndSessionId);
        telemetryItem.setProperty("requestName", requestName);

        String[] params = uri.getQuery().split("&");
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            telemetryItem.setProperty("queryParameter." + name, value);
        }

        return telemetryItem;
    }

    private static List<TelemetryItem> getExpectedTelemetriesForRunId(String runId) {
        List<TelemetryItem> telemetryItems = new ArrayList<TelemetryItem>();

        for (TelemetryItem telemetry : allExpectedTelemetries) {
            if (telemetry.getProperty("runId").equalsIgnoreCase(runId)) {
                telemetryItems.add(telemetry);
            }
        }

        return telemetryItems;
    }

    private static void sendRequestsForAllTests() throws Exception {
        System.out.println("Sending requests...");
        ArrayList<UriWithExpectedResult> uriPathsToRequest = new ArrayList<UriWithExpectedResult>();
        UriWithExpectedResult booksRequest =
                new UriWithExpectedResult(
                        "books?id=Thriller",
                        testRequestCommonPropertiesRunId,
                        HttpStatus.OK_200,
                        "GET BooksController/showBooksByCategory");

        UriWithExpectedResult nonExistingPageRequest =
                new UriWithExpectedResult(
                        "nonExistingWebFage",
                        testPageNotFoundResponseCodeRunId,
                        HttpStatus.NOT_FOUND_404,
                        "GET /bookstore-spring/nonExistingWebFage");

        UriWithExpectedResult categoriesRequest =
                new UriWithExpectedResult(
                        "categories",
                        testRequestCorrelationRunId,
                        HttpStatus.OK_200,
                        "GET CategoriesController/listCategories");

        uriPathsToRequest.add(booksRequest);
        uriPathsToRequest.add(nonExistingPageRequest);
        uriPathsToRequest.add(categoriesRequest);

        List<TelemetryItem> expectedTelemetries = sendHttpGetRequests(uriPathsToRequest);
        allExpectedTelemetries.addAll(expectedTelemetries);
    }
}
