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

package com.ning.billing.entitlement.dao;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import org.skife.jdbi.v2.IDBI;

import com.ning.billing.account.api.Account;
import com.ning.billing.callcontext.InternalTenantContext;
import com.ning.billing.catalog.api.ProductCategory;
import com.ning.billing.clock.Clock;
import com.ning.billing.entitlement.EventsStream;
import com.ning.billing.entitlement.api.BlockingState;
import com.ning.billing.entitlement.api.BlockingStateType;
import com.ning.billing.entitlement.api.EntitlementApiException;
import com.ning.billing.entitlement.engine.core.EventsStreamBuilder;
import com.ning.billing.subscription.api.SubscriptionBase;
import com.ning.billing.subscription.api.SubscriptionBaseInternalApi;
import com.ning.billing.subscription.api.user.SubscriptionBaseBundle;
import com.ning.billing.util.cache.CacheControllerDispatcher;
import com.ning.billing.util.dao.NonEntityDao;

import com.google.common.collect.ImmutableList;

public class OptimizedProxyBlockingStateDao extends ProxyBlockingStateDao {

    public OptimizedProxyBlockingStateDao(final EventsStreamBuilder eventsStreamBuilder, final SubscriptionBaseInternalApi subscriptionBaseInternalApi,
                                          final IDBI dbi, final Clock clock, final CacheControllerDispatcher cacheControllerDispatcher,
                                          final NonEntityDao nonEntityDao) {
        super(eventsStreamBuilder, subscriptionBaseInternalApi, dbi, clock, cacheControllerDispatcher, nonEntityDao);
    }

    /**
     * Retrieve blocking states for a given subscription
     * <p/>
     * If the specified subscription is not an add-on, we already have the blocking states
     * (they are all on disk) - we simply return them and there is nothing to do.
     * Otherwise, for add-ons, we will need to compute the blocking states not on disk.
     * <p/>
     * This is a special method for EventsStreamBuilder to save some DAO calls.
     *
     * @param subscriptionBlockingStatesOnDisk
     *                                  blocking states on disk for that subscription
     * @param allBlockingStatesOnDiskForAccount
     *                                  all blocking states on disk for that account
     * @param account                   account associated with the subscription
     * @param bundle                    bundle associated with the subscription
     * @param baseSubscription          base subscription (ProductCategory.BASE) associated with that bundle
     * @param subscription              subscription for which to build blocking states
     * @param allSubscriptionsForBundle all subscriptions associated with that bundle
     * @param context                   call context
     * @return blocking states for that subscription
     * @throws EntitlementApiException
     */
    public List<BlockingState> getBlockingHistory(final List<BlockingState> subscriptionBlockingStatesOnDisk,
                                                  final List<BlockingState> allBlockingStatesOnDiskForAccount,
                                                  final Account account,
                                                  final SubscriptionBaseBundle bundle,
                                                  @Nullable final SubscriptionBase baseSubscription,
                                                  final SubscriptionBase subscription,
                                                  final List<SubscriptionBase> allSubscriptionsForBundle,
                                                  final InternalTenantContext context) throws EntitlementApiException {
        // blockable id points to a subscription, but make sure it's an add-on
        if (!ProductCategory.ADD_ON.equals(subscription.getCategory())) {
            // blockable id points to a base or standalone subscription, there is nothing to do
            return subscriptionBlockingStatesOnDisk;
        }

        // Find all base entitlements that we care about (for which we want to find future cancelled add-ons)
        final Iterable<EventsStream> eventsStreams = ImmutableList.<EventsStream>of(eventsStreamBuilder.buildForEntitlement(allBlockingStatesOnDiskForAccount,
                                                                                                                            account,
                                                                                                                            bundle,
                                                                                                                            baseSubscription,
                                                                                                                            allSubscriptionsForBundle,
                                                                                                                            context));

        return addBlockingStatesNotOnDisk(subscription.getId(),
                                          BlockingStateType.SUBSCRIPTION,
                                          new LinkedList<BlockingState>(subscriptionBlockingStatesOnDisk),
                                          ImmutableList.<SubscriptionBase>of(baseSubscription),
                                          eventsStreams);
    }
}
