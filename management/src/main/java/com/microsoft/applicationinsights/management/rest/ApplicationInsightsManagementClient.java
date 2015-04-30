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

package com.microsoft.applicationinsights.management.rest;

import java.io.IOException;
import java.util.List;

import com.microsoft.applicationinsights.management.authentication.Settings;
import com.microsoft.applicationinsights.management.rest.client.RestOperationException;
import com.microsoft.applicationinsights.management.rest.client.RestClient;
import com.microsoft.applicationinsights.management.rest.model.Resource;
import com.microsoft.applicationinsights.management.rest.model.ResourceGroup;
import com.microsoft.applicationinsights.management.rest.model.Subscription;
import com.microsoft.applicationinsights.management.rest.operations.*;
import com.microsoftopentechnologies.aad.adal4j.AuthenticationContext;
import com.microsoftopentechnologies.aad.adal4j.AuthenticationResult;

/**
 * Created by yonisha on 4/19/2015.
 *
 * The client to use for Application Insights resource management.
 */
public class ApplicationInsightsManagementClient implements ManagementClient {

    private static final String DEFAULT_AUTHENTICATION_TENANT = "common";
    private AuthenticationResult authenticationResult;
    private final String userAgent;
    private RestClient restClient;

    /**
     * Constructs new Application Insights management client.
     * @param authenticationResult The authentication result.
     * @param userAgent The user agent.
     */
    public ApplicationInsightsManagementClient(AuthenticationResult authenticationResult, String userAgent) {
        this.authenticationResult = authenticationResult;
        this.userAgent = userAgent;
        this.restClient = new RestClient(authenticationResult, userAgent);
    }

    /**
     * Gets a list of available subscriptions.
     * @return The list of subscriptions available.
     */
    public List<Subscription> getSubscriptions() throws IOException, RestOperationException {
        renewAccessTokenIfExpired();

        GetSubscriptionsOperation getSubscriptionsOperation = new GetSubscriptionsOperation();
        List<Subscription> subscriptions = getSubscriptionsOperation.execute(this.restClient);

        return subscriptions;
    }

    /**
     * Gets a list of resources for a given subscription.
     * @param subscriptionId The subscription ID.
     * @return The resources list.
     */
    public List<Resource> getResources(String subscriptionId) throws IOException, RestOperationException {
        renewAccessTokenIfExpired();

        GetResourcesOperation getResourcesOperation = new GetResourcesOperation(subscriptionId);
        List<Resource> resources = getResourcesOperation.execute(this.restClient);

        return resources;
    }

    /**
     * Creates a new resource.
     * @param subscriptionId The subscription which the resource will be created in.
     * @param resourceGroupName The resource group name.
     * @param resourceName The resource name.
     * @return The resource created.
     */
    public Resource createResource(String subscriptionId, String resourceGroupName, String resourceName, String location) throws IOException, RestOperationException {
        renewAccessTokenIfExpired();

        CreateResourceOperation createResourceOperation = new CreateResourceOperation(subscriptionId, resourceGroupName, resourceName, location);
        Resource resource = createResourceOperation.execute(this.restClient);

        return resource;
    }

    /**
     * Create new resources group.
     * @param subscriptionId The subsription ID.
     * @param resourceGroupName The resource group name.
     * @param location The location.
     * @return The new resource group created.
     */
    @Override
    public ResourceGroup createResourceGroup(String subscriptionId, String resourceGroupName, String location) throws IOException, RestOperationException {
        renewAccessTokenIfExpired();

        CreateResourceGroupOperation createResourceGroupOperation = new CreateResourceGroupOperation(subscriptionId, resourceGroupName, location);
        ResourceGroup resourceGroup = createResourceGroupOperation.execute(this.restClient);

        return resourceGroup;
    }

    /**
     * Gets all resource groups in the given subscription.
     *
     * @param subscriptionId The subscription ID.
     * @return Collection of resource groups.
     */
    @Override
    public List<ResourceGroup> getResourceGroups(String subscriptionId) throws IOException, RestOperationException {
        GetResourceGroupsOperation getResourceGroupsOperation = new GetResourceGroupsOperation(subscriptionId);
        List<ResourceGroup> resourceGroups = getResourceGroupsOperation.execute(this.restClient);

        return resourceGroups;
    }

    /**
     * Gets all the available geo-locations.
     *
     * @return Collection of available geo-locations.
     */
    @Override
    public List<String> getAvailableGeoLocations() throws IOException, RestOperationException {
        renewAccessTokenIfExpired();

        GetAvailableGeoLocations getAvailableGeoLocations = new GetAvailableGeoLocations();
        List<String> locations = getAvailableGeoLocations.execute(this.restClient);

        return locations;
    }

    private void renewAccessTokenIfExpired() throws IOException {
        if (this.authenticationResult.getExpiresOn() > 0) {
            return;
        }

        if (this.authenticationResult.getRefreshToken() == null || this.authenticationResult.getRefreshToken().equalsIgnoreCase("")) {
            // TODO: log.

            return;
        }

        AuthenticationContext context = new AuthenticationContext(Settings.getAdAuthority());
        try {
            this.authenticationResult = context.acquireTokenByRefreshToken(
                    this.authenticationResult,
                    DEFAULT_AUTHENTICATION_TENANT,
                    Settings.getAzureServiceManagementUri(),
                    Settings.getClientId());
        } finally {
            context.dispose();
        }

         this.restClient = new RestClient(this.authenticationResult, userAgent);
    }
}
