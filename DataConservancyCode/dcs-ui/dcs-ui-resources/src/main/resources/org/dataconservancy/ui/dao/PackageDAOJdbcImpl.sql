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
CREATE TABLE PACKAGE (OBJECT_ID VARCHAR(1024) PRIMARY KEY, PACKAGE_TYPE VARCHAR(50), PACKAGE_FILE_NAME VARCHAR(1024))
CREATE TABLE PACKAGE_FILE_DATA (PACKAGE_ID VARCHAR(1024), DATA_SET_ID VARCHAR(1024), DATA_FILE_NAME VARCHAR(1024), PRIMARY KEY(PACKAGE_ID, DATA_SET_ID, DATA_FILE_NAME))
