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

package com.ning.billing.util.dao;

import java.util.UUID;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.mixins.CloseMe;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

import com.ning.billing.callcontext.InternalTenantContext;

@UseStringTemplate3StatementLocator
public interface NonEntitySqlDao extends Transactional<NonEntitySqlDao>, CloseMe {

    @SqlQuery
    public Long getRecordIdFromObject(@Bind("id") String id, @Define("tableName") final String tableName);

    @SqlQuery
    public UUID getIdFromObject(@Bind("recordId") Long recordId, @Define("tableName") final String tableName);

    @SqlQuery
    public Long getAccountRecordIdFromAccount(@Bind("id") String id);

    @SqlQuery
    public Long getAccountRecordIdFromAccountHistory(@Bind("id") String id);

    @SqlQuery
    public Long getAccountRecordIdFromObjectOtherThanAccount(@Bind("id") String id, @Define("tableName") final String tableName);

    @SqlQuery
    public Long getTenantRecordIdFromTenant(@Bind("id") String id);

    @SqlQuery
    public Long getTenantRecordIdFromObjectOtherThanTenant(@Bind("id") String id, @Define("tableName") final String tableName);

    @SqlQuery
    public Long getLastHistoryRecordId(@Bind("targetRecordId") Long targetRecordId, @Define("tableName") final String tableName);

    @SqlQuery
    public Long getHistoryTargetRecordId(@Bind("recordId") Long recordId, @Define("tableName") final String tableName);

    @SqlQuery
    public Iterable<RecordIdIdMappings> getHistoryRecordIdIdMappings(@Define("tableName") String tableName,
                                                                     @Define("historyTableName") String historyTableName,
                                                                     @BindBean final InternalTenantContext context);

    @SqlQuery
    public Iterable<RecordIdIdMappings> getHistoryRecordIdIdMappingsForAccountsTable(@Define("tableName") String tableName,
                                                                                     @Define("historyTableName") String historyTableName,
                                                                                     @BindBean final InternalTenantContext context);

    @SqlQuery
    public Iterable<RecordIdIdMappings> getHistoryRecordIdIdMappingsForTablesWithoutAccountRecordId(@Define("tableName") String tableName,
                                                                                                    @Define("historyTableName") String historyTableName,
                                                                                                    @BindBean final InternalTenantContext context);

    @SqlQuery
    public Iterable<RecordIdIdMappings> getRecordIdIdMappings(@Define("tableName") String tableName,
                                                              @BindBean final InternalTenantContext context);
}
