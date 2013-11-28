/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;

public class ESIPCitationFormatterImpl implements CitationFormatter{
    
      private List<PersonName> creators = new ArrayList<PersonName>();
      private Integer releaseYear;
      private Integer updatedYear;
      private String title;
      private String version;
      private String archive;
      private String resourceType;
      private String locator;
      private DateTime accessDate;
      private static String[] MONTHS =
         { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan"};


    public String formatHtml(Citation citation){
        creators = citation.getCreators() != null ? citation.getCreators() : null;
        releaseYear = citation.getReleaseDate() != null ? citation.getReleaseDate().getYear() : null;
        updatedYear = citation.getUpdatedDate() != null ? citation.getUpdatedDate().getYear() : null;
        title = citation.getTitle() != null ? citation.getTitle() : null;
        version = citation.getVersion() != null ? citation.getVersion() : null;
        archive = citation.getPublisher() != null ? citation.getPublisher() : null;
        resourceType = citation.getResourceType() != null ? citation.getResourceType() : null;
        locator = citation.getLocator() != null ? citation.getLocator() : null;
        accessDate = citation.getAccessDate() != null ? citation.getAccessDate() : null;

        StringBuilder sb  = new StringBuilder("");

        if (creators != null){
            for (PersonName creator : creators){
                if(creator != null){
                    if(creators.indexOf(creator) == 0){
                        //first author: last name, initials
                        sb.append(formatNamesAsString(creator.getFamilyNamesAsArray()));
                        sb.append(", ");
                        sb.append(formatNamesAsInitials(creator.getGivenNamesAsArray(), creator.getMiddleNamesAsArray()));
                    } else {
                        //first give the initials, then last name
                        sb.append(formatNamesAsInitials(creator.getGivenNamesAsArray(), creator.getMiddleNamesAsArray()));
                        sb.append(" ");
                        sb.append(formatNamesAsString(creator.getFamilyNamesAsArray()));
                    }
                    if(creators.indexOf(creator) < creators.size() - 2){
                        sb.append(", ");
                    } else if (creators.indexOf(creator) == creators.size() - 2) {
                        sb.append(" and ");
                    }
                }
            }
            if(creators.size()>1) {   //the final name is formatted as J.P. Morgan instead of Morgan, J.P. - need "."
                sb.append(".");
            }
        }
        if(releaseYear != null){
            sb.append(" (");
            sb.append(releaseYear);
            sb.append(")");
            if (updatedYear != null && updatedYear > releaseYear){
                sb.append( ", updated " );
                sb.append(updatedYear);
            }
        }
        if(title != null){
            sb.append(". <i>");
            sb.append(title);
            sb.append(".</i> ");
        }
        if (version != null && !version.isEmpty()){
            sb.append( "<i>Version ");
            sb.append(version);
            sb.append (".</i> ");
        }
        if (archive != null && !archive.isEmpty()){
            sb.append(archive);
            sb.append(". ");
        }
        if (resourceType != null && !resourceType.isEmpty()){
            sb.append(resourceType);
            sb.append(". ");
        }
        if (locator !=null && !locator.isEmpty()){
            sb.append(locator);
            sb.append(". ");
        }
        if (accessDate != null){
            sb.append("Accessed ");
            sb.append(accessDate.getDayOfMonth() + " "  + MONTHS[accessDate.getMonthOfYear()-1] + " " + accessDate.getYear());
            sb.append(".");
        }

        return sb.toString();
    }

    private String formatNamesAsString(String[] nameArray ){
        StringBuilder stringBuilder = new StringBuilder("");
        List<String> nameList = Arrays.asList(nameArray);
        nameList.removeAll(Collections.singleton(null));
        nameList.removeAll(Collections.singleton(""));

        if(nameList != null){
            for(String name : nameList){
                String trimmedName = name.trim();
                if(trimmedName.length()==0){
                    break;
                }
                trimmedName=trimmedName.replaceAll("\\s+"," ");
                stringBuilder.append(trimmedName);
                if(nameList.indexOf(name) < nameList.size() - 1){
                    stringBuilder.append(" ");
                }
            }
        }
        return stringBuilder.toString();      
    }

    /*
     *  We attempt to handle multiple names in a single input
     *  field, hyphenated names, and initials input in the name fields.
     *
     */

    private String formatNamesAsInitials(String[] nameArray1, String[] nameArray2){
        StringBuilder stringBuilder = new StringBuilder("");
        List<String> nameList1 = Arrays.asList(nameArray1);
        List<String> nameList2= Arrays.asList(nameArray2);
        String [] temp;
        //lump all the names together
        List<String> combinedList = new ArrayList<String>();
        combinedList.addAll(nameList1);
        combinedList.addAll(nameList2);
        combinedList.removeAll(Collections.singleton(null));
        combinedList.removeAll(Collections.singleton(""));

        if(combinedList != null){
            for(String name : combinedList){
                String trimmedName = name.trim();
                trimmedName=trimmedName.replaceAll("\\.", " ");  //handle initials with periods
                temp = trimmedName.split("\\s+");      //handle multiple names in field
                if(temp!= null){
                    for(String part : temp){
                        if(part.length()==0){
                            break;
                        }
                        Character character=part.charAt(0);
                        if(Character.isLetter(character)){
                            stringBuilder.append(character);
                            if(part.contains("-") && part.length() > (part.lastIndexOf("-"))){//handle hyphen
                                character = part.charAt(part.lastIndexOf("-")+1);
                                if(character.isLetter(character)){
                                    stringBuilder.append("-");
                                    stringBuilder.append(part.charAt(part.lastIndexOf("-")+1));
                                }
                            }
                        stringBuilder.append(".");
                        }
                    }
                }
            }
        }
        return stringBuilder.toString();
    }
}
