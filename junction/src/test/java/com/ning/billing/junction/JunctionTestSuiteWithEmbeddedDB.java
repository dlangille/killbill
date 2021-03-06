/*
 * Copyright 2010-2013 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.junction;

import java.net.URL;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.ning.billing.GuicyKillbillTestSuiteWithEmbeddedDB;
import com.ning.billing.account.api.AccountData;
import com.ning.billing.account.api.AccountUserApi;
import com.ning.billing.api.TestApiListener;
import com.ning.billing.api.TestListenerStatus;
import com.ning.billing.bus.api.PersistentBus;
import com.ning.billing.catalog.DefaultCatalogService;
import com.ning.billing.catalog.api.Catalog;
import com.ning.billing.catalog.api.CatalogService;
import com.ning.billing.catalog.api.Currency;
import com.ning.billing.clock.ClockMock;
import com.ning.billing.entitlement.DefaultEntitlementService;
import com.ning.billing.entitlement.EntitlementService;
import com.ning.billing.entitlement.api.EntitlementApi;
import com.ning.billing.junction.glue.TestJunctionModuleWithEmbeddedDB;
import com.ning.billing.mock.MockAccountBuilder;
import com.ning.billing.subscription.api.SubscriptionBaseInternalApi;
import com.ning.billing.subscription.api.SubscriptionBaseService;
import com.ning.billing.subscription.engine.core.DefaultSubscriptionBaseService;
import com.ning.billing.util.callcontext.InternalCallContextFactory;
import com.ning.billing.util.svcsapi.bus.BusService;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public abstract class JunctionTestSuiteWithEmbeddedDB extends GuicyKillbillTestSuiteWithEmbeddedDB {

    protected static final Logger log = LoggerFactory.getLogger(JunctionTestSuiteWithEmbeddedDB.class);

    // Be generous...
    protected static final Long DELAY = 20000L;

    @Inject
    protected AccountUserApi accountApi;
    @Inject
    protected BlockingInternalApi blockingInternalApi;
    @Inject
    protected EntitlementApi entitlementApi;
    @Inject
    protected BillingInternalApi billingInternalApi;
    @Inject
    protected CatalogService catalogService;
    @Inject
    protected SubscriptionBaseInternalApi subscriptionInternalApi;
    @Inject
    protected PersistentBus bus;
    @Inject
    protected TestApiListener testListener;
    @Inject
    protected TestListenerStatus testListenerStatus;
    @Inject
    protected BusService busService;
    @Inject
    protected SubscriptionBaseService subscriptionBaseService;
    @Inject
    protected EntitlementService entitlementService;
    @Inject
    protected InternalCallContextFactory internalCallContextFactory;

    protected Catalog catalog;

    private void loadSystemPropertiesFromClasspath(final String resource) {
        final URL url = JunctionTestSuiteWithEmbeddedDB.class.getResource(resource);
        Assert.assertNotNull(url);

        configSource.merge(url);
    }

    @BeforeClass(groups = "slow")
    protected void beforeClass() throws Exception {
        loadSystemPropertiesFromClasspath("/junction.properties");
        final Injector injector = Guice.createInjector(Stage.PRODUCTION, new TestJunctionModuleWithEmbeddedDB(configSource));
        injector.injectMembers(this);
    }

    @BeforeMethod(groups = "slow")
    public void beforeMethod() throws Exception {
        super.beforeMethod();
        startTestFamework();
        this.catalog = initCatalog(catalogService);

        // Make sure we start with a clean state
        assertListenerStatus();
    }

    @AfterMethod(groups = "slow")
    public void afterMethod() throws Exception {
        // Make sure we finish in a clean state
        assertListenerStatus();

        stopTestFramework();
    }

    private Catalog initCatalog(final CatalogService catalogService) throws Exception {
        ((DefaultCatalogService) catalogService).loadCatalog();
        final Catalog catalog = catalogService.getFullCatalog();
        assertNotNull(catalog);
        return catalog;
    }

    private void startTestFamework() throws Exception {
        log.debug("STARTING TEST FRAMEWORK");

        resetTestListener(testListener, testListenerStatus);

        resetClockToStartOfTest(clock);

        startBusAndRegisterListener(busService, testListener);

        restartSubscriptionService(subscriptionBaseService);
        restartEntitlementService(entitlementService);

        log.debug("STARTED TEST FRAMEWORK");
    }

    private void stopTestFramework() throws Exception {
        log.debug("STOPPING TEST FRAMEWORK");
        stopBusAndUnregisterListener(busService, testListener);
        stopSubscriptionService(subscriptionBaseService);
        stopEntitlementService(entitlementService);
        log.debug("STOPPED TEST FRAMEWORK");
    }

    private void resetTestListener(final TestApiListener testListener, final TestListenerStatus testListenerStatus) {
        // RESET LIST OF EXPECTED EVENTS
        if (testListener != null) {
            testListener.reset();
            testListenerStatus.resetTestListenerStatus();
        }
    }

    private void resetClockToStartOfTest(final ClockMock clock) {
        clock.resetDeltaFromReality();

        // Date at which all tests start-- we create the date object here after the system properties which set the JVM in UTC have been set.
        final DateTime testStartDate = new DateTime(2012, 5, 7, 0, 3, 42, 0);
        clock.setDeltaFromReality(testStartDate.getMillis() - clock.getUTCNow().getMillis());
    }

    private void startBusAndRegisterListener(final BusService busService, final TestApiListener testListener) throws Exception {
        busService.getBus().start();
        busService.getBus().register(testListener);
    }

    private void restartSubscriptionService(final SubscriptionBaseService subscriptionBaseService) {
        // START NOTIFICATION QUEUE FOR SUBSCRIPTION
        ((DefaultSubscriptionBaseService) subscriptionBaseService).initialize();
        ((DefaultSubscriptionBaseService) subscriptionBaseService).start();
    }

    private void restartEntitlementService(final EntitlementService entitlementService) {
        // START NOTIFICATION QUEUE FOR ENTITLEMENT
        ((DefaultEntitlementService) entitlementService).initialize();
        ((DefaultEntitlementService) entitlementService).start();
    }

    private void stopBusAndUnregisterListener(final BusService busService, final TestApiListener testListener) throws Exception {
        busService.getBus().unregister(testListener);
        busService.getBus().stop();
    }

    private void stopSubscriptionService(final SubscriptionBaseService subscriptionBaseService) throws Exception {
        ((DefaultSubscriptionBaseService) subscriptionBaseService).stop();
    }

    private void stopEntitlementService(final EntitlementService entitlementService) throws Exception {
        ((DefaultEntitlementService) entitlementService).stop();
    }

    protected AccountData getAccountData(final int billingDay) {
        return new MockAccountBuilder().name(UUID.randomUUID().toString().substring(1, 8))
                                       .firstNameLength(6)
                                       .email(UUID.randomUUID().toString().substring(1, 8))
                                       .phone(UUID.randomUUID().toString().substring(1, 8))
                                       .migrated(false)
                                       .isNotifiedForInvoices(false)
                                       .externalKey(UUID.randomUUID().toString().substring(1, 8))
                                       .billingCycleDayLocal(billingDay)
                                       .currency(Currency.USD)
                                       .paymentMethodId(UUID.randomUUID())
                                       .timeZone(DateTimeZone.UTC)
                                       .build();
    }

    protected void assertListenerStatus() {
        assertTrue(testListener.isCompleted(DELAY));
        ((JunctionTestListenerStatus) testListenerStatus).assertListenerStatus();
    }
}
