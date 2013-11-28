/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.dcs.access.client.model;
  

import org.dataconservancy.dcs.access.client.SeadApp;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;  
import com.smartgwt.client.types.DSDataFormat;
  
public class ProvenanceDS extends DataSource {  
  
    private static ProvenanceDS instance = null;  
  
    public static ProvenanceDS getInstance(String submitterId) {
    	if (instance == null) {  
            instance = new ProvenanceDS(submitterId);  
        }  
        return instance;  
    }  
  
    public ProvenanceDS(String submitterId) {  
        setID(id);  
        setTitleField("name");  
        setDataFormat(DSDataFormat.JSON);
        setRecordXPath("/proveRec");  
        DataSourceTextField nameField = new DataSourceTextField("name", "Name", 128);  
  
        DataSourceIntegerField employeeIdField = new DataSourceIntegerField("id", "Identifier");  
        employeeIdField.setPrimaryKey(true);  
        employeeIdField.setRequired(true);  
  
        DataSourceIntegerField reportsToField = new DataSourceIntegerField("parentId", "Manager");  
        reportsToField.setRequired(true);  
        reportsToField.setForeignKey(id + ".id");  
//        reportsToField.setRootValue("de62ed08-d61e-4cf2-ad22-41bf142e881e");  
  
        DataSourceTextField jobField = new DataSourceTextField("status", "Status", 128);  
        DataSourceTextField emailField = new DataSourceTextField("date", "Date", 128);  
        DataSourceTextField statusField = new DataSourceTextField("type", "Type", 40);  
        
        setFields(nameField, employeeIdField, reportsToField, jobField, emailField,  
                statusField);  
  
        
//        setDataURL("ds/test_data/employees.data.json");
        setDataURL(SeadApp.GET_MONITOR_URL+"?email="+submitterId);
        setClientOnly(true);  
    }  
}  