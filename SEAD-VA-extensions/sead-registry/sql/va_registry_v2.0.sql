
DROP TABLE IF EXISTS base_entity;
CREATE TABLE base_entity (
   entity_id                 VARCHAR(127) NOT NULL,             
   entity_name                           VARCHAR(256) NOT NULL,                          
   entity_created_time    TIMESTAMP NOT NULL,
   entity_last_updated_time    TIMESTAMP NOT NULL,
   PRIMARY KEY (entity_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS entity_type;
CREATE TABLE entity_type (
   entity_type_id                 VARCHAR(127) NOT NULL,
   entity_id                   VARCHAR(127) NOT NULL,
   entity_type_name               VARCHAR(256) NOT NULL,
   PRIMARY KEY (entity_id, entity_type_id),
   FOREIGN KEY (entity_id) REFERENCES base_entity(entity_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS entity_content;
CREATE TABLE entity_content (
 entity_content_id INTEGER NOT NULL AUTO_INCREMENT,
 entity_content_data LONGBLOB,
 entity_id VARCHAR(127) NOT NULL,
 PRIMARY KEY (entity_content_id),
 FOREIGN KEY (entity_id) REFERENCES base_entity(entity_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS metadata_type;
CREATE TABLE metadata_type (
   metadata_id                 VARCHAR(127) NOT NULL,
   metadata_schema               VARCHAR(256) NOT NULL,
   metadata_element               VARCHAR(256) NOT NULL,
   PRIMARY KEY (metadata_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS metadata_reference;
CREATE TABLE metadata_reference (
  metadata_ref_id               BIGINT NOT NULL AUTO_INCREMENT,
  subject_entity_id                VARCHAR(127) NOT NULL,
  metadata_id                     VARCHAR(127) NOT NULL,
  object_entity_id                 VARCHAR(127) NOT NULL,
  PRIMARY KEY (metadata_ref_id),
  FOREIGN KEY (subject_entity_id) REFERENCES base_entity(entity_id),
  FOREIGN KEY (metadata_id) REFERENCES metadata_type(metadata_id),
  FOREIGN KEY (object_entity_id) REFERENCES base_entity(entity_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS property;
CREATE TABLE property (
  property_id              BIGINT NOT NULL AUTO_INCREMENT,
  entity_id                VARCHAR(127) NOT NULL,
  metadata_id              VARCHAR(127) NOT NULL,
  valueStr                 VARCHAR(10000) NOT NULL,
  PRIMARY KEY (property_id),
  FOREIGN KEY (metadata_id) REFERENCES metadata_type(metadata_id),
  FOREIGN KEY (entity_id) REFERENCES base_entity(entity_id)
) ENGINE=INNODB;


DROP TABLE IF EXISTS relation_type;
CREATE TABLE relation_type (
   relation_type_id                 VARCHAR(127) NOT NULL,
   relation_schema               VARCHAR(256) NOT NULL,
   relation_element               VARCHAR(256) NOT NULL,
   PRIMARY KEY (relation_type_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS aggregation;
CREATE TABLE aggregation (
  parent_id                  VARCHAR(127) NOT NULL,       
  child_id                   VARCHAR(127) NOT NULL,
  PRIMARY KEY (parent_id, child_id),
  FOREIGN KEY (parent_id) REFERENCES base_entity(entity_id),
  FOREIGN KEY (child_id) REFERENCES base_entity(entity_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS relation;
CREATE TABLE relation (
 cause_id                  VARCHAR(127) NOT NULL,       
 relation_type_id                   VARCHAR(127) NOT NULL,
 effect_id                   VARCHAR(127) NOT NULL,
 PRIMARY KEY (cause_id, relation_type_id, effect_id),
 FOREIGN KEY (cause_id) REFERENCES base_entity(entity_id),
 FOREIGN KEY (effect_id) REFERENCES base_entity(entity_id),
 FOREIGN KEY (relation_type_id) REFERENCES relation_type(relation_type_id)
) ENGINE=INNODB;



DROP TABLE IF EXISTS file;
CREATE TABLE file (
    entity_id      VARCHAR(127) NOT NULL,
    size_bytes     BIGINT,
    version_num    VARCHAR(127) NOT NULL,
    file_name      VARCHAR(127) NOT NULL,
    is_obsolete    INT(1),
    PRIMARY KEY (entity_id),
    FOREIGN KEY (entity_id) REFERENCES base_entity(entity_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS fixity;
CREATE TABLE fixity (
    entity_id     VARCHAR(127) NOT NULL,
    type          VARCHAR(127),
    valueStr      VARCHAR(127) NOT NULL,
    PRIMARY KEY (entity_id, type),
    FOREIGN KEY (entity_id) REFERENCES file(entity_id)
) ENGINE=INNODB;


DROP TABLE IF EXISTS format;
CREATE TABLE format (
    format_id     BIGINT NOT NULL AUTO_INCREMENT,
    entity_id     VARCHAR(127) NOT NULL,
    type          VARCHAR(127),
    valueStr      VARCHAR(127) NOT NULL,
    PRIMARY KEY (format_id),
    FOREIGN KEY (entity_id) REFERENCES file(entity_id)
) ENGINE=INNODB;


DROP TABLE IF EXISTS event_type;
CREATE TABLE event_type (
   event_type_id                 VARCHAR(127) NOT NULL,
   event_name                  VARCHAR(127) NOT NULL,
   event_description               VARCHAR(256) NOT NULL,
   PRIMARY KEY (event_type_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS event;
CREATE TABLE event (
   event_id        VARCHAR(127) NOT NULL,
   event_type_id      VARCHAR(127) NOT NULL,
   event_detail    VARCHAR(256) NOT NULL,
   PRIMARY KEY (event_id),
   FOREIGN KEY (event_id) REFERENCES base_entity(entity_id),
   FOREIGN KEY (event_type_id) REFERENCES event_type(event_type_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS state;
CREATE TABLE state (
    state_id     VARCHAR(127) NOT NULL,
    state_name   VARCHAR(127) NOT NULL,
    state_type   VARCHAR(127) NOT NULL,
    PRIMARY KEY (state_id),
    FOREIGN KEY (state_id) REFERENCES base_entity(entity_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS transition;
CREATE TABLE transition (
  start_state_id                  VARCHAR(127) NOT NULL,
  event_type_id                   VARCHAR(127) NOT NULL,
  next_state_id                   VARCHAR(127) NOT NULL,
  PRIMARY KEY (start_state_id, event_type_id, next_state_id),
  FOREIGN KEY (start_state_id) REFERENCES state(state_id),
  FOREIGN KEY (event_type_id) REFERENCES event_type(event_type_id),
  FOREIGN KEY (next_state_id) REFERENCES state(state_id)
) ENGINE=INNODB;


DROP TABLE IF EXISTS collection;
CREATE TABLE collection (
    entity_id      VARCHAR(127) NOT NULL,
    state_id      VARCHAR(127) NOT NULL,
    version_num    VARCHAR(127) NOT NULL,
    name      VARCHAR(127) NOT NULL,
    is_obsolete    INT(1),
    PRIMARY KEY (entity_id),
    FOREIGN KEY (entity_id) REFERENCES base_entity(entity_id),
    FOREIGN KEY (state_id) REFERENCES state(state_id)
) ENGINE=INNODB;


DROP TABLE IF EXISTS data_identifier_type;
CREATE TABLE data_identifier_type (
   data_identifier_type_id                 VARCHAR(127) NOT NULL,
   data_identifier_type_name                  VARCHAR(127) NOT NULL,
   schema_uri               VARCHAR(256) NOT NULL,
   PRIMARY KEY (data_identifier_type_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS data_identifier;
CREATE TABLE data_identifier (
   entity_id        VARCHAR(127) NOT NULL,
   data_identifier_type_id      VARCHAR(127) NOT NULL,
   data_identifier_value    VARCHAR(256) NOT NULL,
   PRIMARY KEY (entity_id, data_identifier_type_id),
   FOREIGN KEY (entity_id) REFERENCES base_entity(entity_id),
   FOREIGN KEY (data_identifier_type_id) REFERENCES data_identifier_type(data_identifier_type_id)
) ENGINE=INNODB;


DROP TABLE IF EXISTS repository;
CREATE TABLE repository (
   repository_id                 VARCHAR(127) NOT NULL,
   repository_name                  VARCHAR(127) NOT NULL,
   software_type               VARCHAR(256) NOT NULL,
   affiliation               VARCHAR(256) NOT NULL,
   PRIMARY KEY (repository_id)
) ENGINE=INNODB;


DROP TABLE IF EXISTS data_location;
CREATE TABLE data_location (
   entity_id        VARCHAR(127) NOT NULL,
   location_type_id      VARCHAR(127) NOT NULL,
   location_value    VARCHAR(256) NOT NULL,
   is_master_copy    INT(1),
   PRIMARY KEY (entity_id, location_type_id),
   FOREIGN KEY (entity_id) REFERENCES base_entity(entity_id),
   FOREIGN KEY (location_type_id) REFERENCES repository(repository_id)
) ENGINE=INNODB;


DROP TABLE IF EXISTS agent;
CREATE TABLE agent (
   agent_id                 VARCHAR(127) NOT NULL,
   first_name                           VARCHAR(256) NOT NULL,
   last_name                           VARCHAR(256) NOT NULL,
   PRIMARY KEY (agent_id),
   FOREIGN KEY (agent_id) REFERENCES base_entity(entity_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS profile_type;
CREATE TABLE profile_type (
   profile_type_id                 VARCHAR(127) NOT NULL,
   profile_type_name                  VARCHAR(127) NOT NULL,
   profile_type_schema               VARCHAR(256) NOT NULL,
   PRIMARY KEY (profile_type_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS agent_profile;
CREATE TABLE agent_profile (
   agent_id        VARCHAR(127) NOT NULL,
   profile_type_id      VARCHAR(127) NOT NULL,
   profile_value    VARCHAR(256) NOT NULL,
   PRIMARY KEY (agent_id, profile_type_id),
   FOREIGN KEY (agent_id) REFERENCES agent(agent_id),
   FOREIGN KEY (profile_type_id) REFERENCES profile_type(profile_type_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS role_type;
CREATE TABLE role_type (
   role_type_id                 VARCHAR(127) NOT NULL,
   role_type_name                  VARCHAR(127) NOT NULL,
   role_description               VARCHAR(256) NOT NULL,
   PRIMARY KEY (role_type_id)
) ENGINE=INNODB;


DROP TABLE IF EXISTS agent_role;
CREATE TABLE agent_role (
   agent_id        VARCHAR(127) NOT NULL,
   role_type_id      VARCHAR(127) NOT NULL,
   PRIMARY KEY (agent_id, role_type_id),
   FOREIGN KEY (agent_id) REFERENCES agent(agent_id),
   FOREIGN KEY (role_type_id) REFERENCES role_type(role_type_id)
) ENGINE=INNODB;
