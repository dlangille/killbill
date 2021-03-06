/*! SET storage_engine=INNODB */;

DROP TABLE IF EXISTS tenants;
CREATE TABLE tenants (
    record_id int(11) unsigned NOT NULL AUTO_INCREMENT,
    id char(36) NOT NULL,
    external_key varchar(128) NULL,
    api_key varchar(128) NULL,
    api_secret varchar(128) NULL,
    api_salt varchar(128) NULL,
    created_date datetime NOT NULL,
    created_by varchar(50) NOT NULL,
    updated_date datetime DEFAULT NULL,
    updated_by varchar(50) DEFAULT NULL,
    PRIMARY KEY(record_id)
) CHARACTER SET utf8 COLLATE utf8_bin;
CREATE UNIQUE INDEX tenants_id ON tenants(id);
CREATE UNIQUE INDEX tenants_api_key ON tenants(api_key);


DROP TABLE IF EXISTS tenant_kvs;
CREATE TABLE tenant_kvs (
   record_id int(11) unsigned NOT NULL AUTO_INCREMENT,
   id char(36) NOT NULL,
   tenant_record_id int(11) unsigned default null,
   tenant_key varchar(64) NOT NULL,
   tenant_value varchar(1024) NOT NULL,
   is_active bool DEFAULT 1,
   created_date datetime NOT NULL,
   created_by varchar(50) NOT NULL,
   updated_date datetime DEFAULT NULL,
   updated_by varchar(50) DEFAULT NULL,
   PRIMARY KEY(record_id)
) CHARACTER SET utf8 COLLATE utf8_bin;
CREATE INDEX tenant_kvs_key ON tenant_kvs(tenant_key);
