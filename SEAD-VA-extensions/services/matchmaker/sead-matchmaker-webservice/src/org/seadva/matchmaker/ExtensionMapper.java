
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

/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:34:40 IST)
 */

        
            package org.seadva.matchmaker;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://seadva.org/MatchMaker".equals(namespaceURI) &&
                  "ruleType".equals(typeName)){
                   
                            return  org.seadva.matchmaker.RuleType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://seadva.org/MatchMaker".equals(namespaceURI) &&
                  "characteristicsType".equals(typeName)){
                   
                            return  org.seadva.matchmaker.CharacteristicsType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://seadva.org/MatchMaker".equals(namespaceURI) &&
                  "characteristicType".equals(typeName)){
                   
                            return  org.seadva.matchmaker.CharacteristicType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://seadva.org/MatchMaker".equals(namespaceURI) &&
                  "preferencesType".equals(typeName)){
                   
                            return  org.seadva.matchmaker.PreferencesType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://seadva.org/MatchMaker".equals(namespaceURI) &&
                  "classAdType".equals(typeName)){
                   
                            return  org.seadva.matchmaker.ClassAdType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://seadva.org/MatchMaker".equals(namespaceURI) &&
                  "requirementsType".equals(typeName)){
                   
                            return  org.seadva.matchmaker.RequirementsType.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    