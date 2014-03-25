DROP TABLE IF EXISTS entity;
CREATE TABLE entity (
   entity_id                 VARCHAR(127) NOT NULL,             
   entity_name                           VARCHAR(256) NOT NULL,                          
   entity_created_time    TIMESTAMP NOT NULL,
   entity_last_updated_time    TIMESTAMP NOT NULL,
   PRIMARY KEY (entity_id)
);

DROP TABLE IF EXISTS entity_type;
CREATE TABLE entity_type (
   entity_type_id                 VARCHAR(127) NOT NULL,
   entity_id                   VARCHAR(127) NOT NULL,
   entity_type_name               VARCHAR(256) NOT NULL,
   PRIMARY KEY (entity_id, entity_type_id),
   FOREIGN KEY (entity_id) REFERENCES entity(entity_id)
);

DROP TABLE IF EXISTS aggregation;
CREATE TABLE aggregation (
  parent_id                  VARCHAR(127) NOT NULL,       
  child_id                   VARCHAR(127) NOT NULL,
  PRIMARY KEY (parent_id, child_id),
  FOREIGN KEY (parent_id) REFERENCES entity(entity_id),
  FOREIGN KEY (child_id) REFERENCES entity(entity_id)                              
);

DROP TABLE IF EXISTS relation;
CREATE TABLE relation (
 cause_id                  VARCHAR(127) NOT NULL,       
 relation                   VARCHAR(127) NOT NULL,
 effect_id                   VARCHAR(127) NOT NULL,
 PRIMARY KEY (cause_id, relation, effect_id),
 FOREIGN KEY (cause_id) REFERENCES entity(entity_id),
 FOREIGN KEY (effect_id) REFERENCES entity(entity_id)                              
);

DROP TABLE IF EXISTS entity_content;
CREATE TABLE entity_content (
 entity_content_id INTEGER NOT NULL AUTO_INCREMENT,
 entity_content_data LONGBLOB,
 entity_id VARCHAR(127) NOT NULL,    
 PRIMARY KEY (entity_content_id),
 FOREIGN KEY (entity_id) REFERENCES entity(entity_id)
);

DROP TABLE IF EXISTS property;
CREATE TABLE property (
  property_id               BIGINT NOT NULL AUTO_INCREMENT,
  entity_id                VARCHAR(127) NOT NULL,       
  name                     VARCHAR(127) NOT NULL,
  valueStr                 VARCHAR(127) NOT NULL,
  PRIMARY KEY (property_id),
  FOREIGN KEY (entity_id) REFERENCES entity(entity_id)
);