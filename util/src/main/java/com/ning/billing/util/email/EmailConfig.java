/*
 * Copyright 2010-2011 Ning, Inc.
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

package com.ning.billing.util.email;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.DefaultNull;
import org.skife.config.Description;

import com.ning.billing.util.config.KillbillConfig;

public interface EmailConfig extends KillbillConfig {

    @Config("killbill.mail.smtp.host")
    @DefaultNull
    @Description("MTA host used for email notifications")
    public String getSmtpServerName();

    @Config("killbill.mail.smtp.port")
    @DefaultNull
    @Description("MTA port used for email notifications")
    public int getSmtpPort();

    @Config("killbill.mail.smtp.auth")
    @Default("false")
    @Description("Whether to authenticate against the MTA")
    public boolean useSmtpAuth();

    @Config("killbill.mail.smtp.user")
    @DefaultNull
    @Description("Username to use to authenticate against the MTA")
    public String getSmtpUserName();

    @Config("killbill.mail.smtp.password")
    @DefaultNull
    @Description("Password to use to authenticate against the MTA")
    public String getSmtpPassword();

    @Config("killbill.mail.from")
    @Default("support@example.com")
    @Description("Default From: field for email notifications")
    String getDefaultFrom();

    @Config("killbill.mail.useSSL")
    @Default("false")
    @Description("Whether to use secure SMTP")
    boolean useSSL();

    @Config("killbill.mail.invoiceEmailSubject")
    @Default("Your invoice")
    @Description("Default Subject: field for invoice notifications")
    String getInvoiceEmailSubject();
}
