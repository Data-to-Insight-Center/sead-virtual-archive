---
--- Copyright 2012 Johns Hopkins University
---
--- Licensed under the Apache License, Version 2.0 (the "License");
--- you may not use this file except in compliance with the License.
--- You may obtain a copy of the License at
---
---     http://www.apache.org/licenses/LICENSE-2.0
---
--- Unless required by applicable law or agreed to in writing, software
--- distributed under the License is distributed on an "AS IS" BASIS,
--- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--- See the License for the specific language governing permissions and
--- limitations under the License.
---
--- Can't yet insert into the project table because the DAO tests will complain
--- INSERT INTO project VALUES ('project:/1', '1234', 'Project One', 'Project seeded by BootstrapData.sql', '0', '0', '0', '0', 'NSF')
--- INSERT INTO relationships VALUES ('admin', 'project:/1', 'IS_ADMINISTRATOR_FOR')
--- INSERT INTO relationships VALUES ('project:/1', 'admin', 'IS_ADMINISTERED_BY')
--- INSERT INTO relationships VALUES ('chunkymonkey@benandjerrys.com', 'project:/1', 'IS_ADMINISTRATOR_FOR')
--- INSERT INTO relationships VALUES ('project:/1', 'chunkymonkey@benandjerrys.com', 'IS_ADMINISTERED_BY')
---

---
--- Disciplines
---
INSERT INTO DISCIPLINE VALUES ('dc:discipline:Biology', 'Biology')
INSERT INTO DISCIPLINE VALUES ('dc:discipline:Astronomy', 'Astronomy')
INSERT INTO DISCIPLINE VALUES ('dc:discipline:EarthScience', 'Earth Science')
INSERT INTO DISCIPLINE VALUES ('dc:discipline:SocialScience', 'Social Science')
INSERT INTO DISCIPLINE VALUES ('dc:discipline:None', 'None')

---
--- Relationships between Disciplines and MetadataFormats (Obverse)
---
INSERT INTO relationships VALUES ('dc:discipline:Biology', 'dc:format:metadata/TaxonX', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:Biology', 'dc:format:metadata/EML', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:Biology', 'dc:format:metadata/DarwinCore', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:Biology', 'dc:format:metadata/DarwinCoreArchive', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:Biology', 'dc:format:metadata/TaxonConceptSchema', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:Biology', 'dc:format:metadata/AudubonCore', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:EarthScience', 'dc:format:metadata/CGDSMFGDC', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:EarthScience', 'dc:format:metadata/ISO19115', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:EarthScience', 'dc:format:metadata/NASADIF', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:EarthScience', 'dataconservancy.org:formats:file:metadata:fgdc:xml', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:Astronomy', 'dc:format:metadata/AVM', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:Astronomy', 'dc:format:metadata/STC', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:Astronomy', 'dc:format:metadata/FITS', 'AGGREGATES')
INSERT INTO relationships VALUES ('dc:discipline:None', 'dc:format:metadata/None', 'AGGREGATES')

---
--- Relationships between Disciplines and MetadataFormats (Inverse)
---
INSERT INTO relationships VALUES ('dc:format:metadata/TaxonX', 'dc:discipline:Biology', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dc:format:metadata/EML', 'dc:discipline:Biology', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dc:format:metadata/DarwinCore', 'dc:discipline:Biology', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dc:format:metadata/DarwinCoreArchive', 'dc:discipline:Biology', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dc:format:metadata/TaxonConceptSchema', 'dc:discipline:Biology', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dc:format:metadata/AudubonCore', 'dc:discipline:Biology', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dc:format:metadata/CGDSMFGDC', 'dc:discipline:EarthScience', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dc:format:metadata/ISO19115', 'dc:discipline:EarthScience', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dc:format:metadata/NASADIF', 'dc:discipline:EarthScience', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dataconservancy.org:formats:file:metadata:fgdc:xml', 'dc:discipline:EarthScience', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dc:format:metadata/AVM', 'dc:discipline:Astronomy', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dc:format:metadata/STC', 'dc:discipline:Astronomy', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dc:format:metadata/FITS', 'dc:discipline:Astronomy', 'IS_AGGREGATED_BY')
INSERT INTO relationships VALUES ('dc:format:metadata/None', 'dc:discipline:None', 'IS_AGGREGATED_BY')

---
--- Metadata Format business properties
---
INSERT INTO METADATA_FORMAT_PROPERTIES VALUES ('dataconservancy.org:formats:file:xsd:2004', '10', true)
INSERT INTO METADATA_FORMAT_PROPERTIES VALUES ('dataconservancy.org:formats:file:metadata:fgdc:xml', '10', true)
