/*
 * Copyright 2010-2012 Ning, Inc.
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

package com.ning.billing.util.audit.api;

import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ning.billing.ObjectType;
import com.ning.billing.util.api.AuditLevel;
import com.ning.billing.util.audit.AuditLog;
import com.ning.billing.util.audit.AuditLogsTestBase;
import com.ning.billing.util.audit.dao.MockAuditDao;
import com.ning.billing.util.dao.TableName;

import com.google.common.collect.ImmutableList;

public class TestDefaultAuditUserApi extends AuditLogsTestBase {

    private List<AuditLog> auditLogs;
    private List<UUID> objectIds;

    @Override
    @BeforeClass(groups = "fast")
    public void beforeClass() throws Exception {
        super.beforeClass();
        auditLogs = ImmutableList.<AuditLog>of(createAuditLog(), createAuditLog(), createAuditLog(), createAuditLog());
        objectIds = ImmutableList.<UUID>of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        for (final TableName tableName : TableName.values()) {
            for (final UUID objectId : objectIds) {
                for (final AuditLog auditLog : auditLogs) {
                    ((MockAuditDao) auditDao).addAuditLogForId(tableName, objectId, auditLog);
                }
            }
        }
    }

    @Test(groups = "fast")
    public void testForObject() throws Exception {
        for (final ObjectType objectType : ObjectType.values()) {
            for (final UUID objectId : objectIds) {
                for (final AuditLevel level : AuditLevel.values()) {
                    if (AuditLevel.NONE.equals(level)) {
                        Assert.assertEquals(auditUserApi.getAuditLogs(objectId, objectType, level, callContext).size(), 0);
                    } else if (AuditLevel.MINIMAL.equals(level)) {
                        Assert.assertEquals(auditUserApi.getAuditLogs(objectId, objectType, level, callContext), ImmutableList.<AuditLog>of(auditLogs.get(0)));
                    } else {
                        Assert.assertEquals(auditUserApi.getAuditLogs(objectId, objectType, level, callContext), auditLogs);
                    }
                }
            }
        }
    }
}
