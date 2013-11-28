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


import org.dataconservancy.ui.model.Citation;
import org.dataconservancy.ui.model.CitationFormatter;
import org.dataconservancy.ui.model.ESIPCitationFormatterImpl;
import org.dataconservancy.ui.model.PersonName;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: jrm
 * Date: 12/19/11
 * Time: 9:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class ESIPCitationFormatterImplTest {

//    private List<PersonName> creators;
    PersonName personName1;
    PersonName personName2;
    PersonName personName3;
    PersonName personName4;
    PersonName personName5;
    PersonName personName6;
    PersonName personName7;
    
    private DateTime releaseDate;
    private String title;
    private String version;
    private String archive;
    private String locator;
    private String resourceType = "cool resource";
    private DateTime accessDate;
    private DateTime updatedDate;
    private CitationFormatter citationFormatter= (CitationFormatter) new ESIPCitationFormatterImpl();

    @Before
    public void setup(){
        
        String [] givenNames1 = {"Bertrand"};
        String [] middleNames1 = {" Låäñö "};
        String [] familyNames1 = {"   Zebub     "};
        String prefixes1 = "";
        String suffixes1 = "";

        String [] givenNames2 = {"Jean-Pierre"};
        String [] middleNames2 = {""};
        String [] familyNames2 = {"van", "der", "    Waerden"};
        String prefixes2 = "";
        String suffixes2 = "";

        String [] givenNames3 = {"Billie Jo"};
        String [] middleNames3 = {"Betty", "Jo"};
        String [] familyNames3 = {" van   der    Waerden "};
        String prefixes3 = "";
        String suffixes3 = "";

 
        String [] givenNames4 = {"J", "P"};
        String [] middleNames4 = {"J P"};
        String [] familyNames4 = {"Morgan"};
        String prefixes4 = "";
        String suffixes4 = "";

 
        String [] givenNames5 = {"J.", "Ô."};
        String [] middleNames5 = {"J. P."};
        String [] familyNames5 = {"Åñöä"};
        String prefixes5 = "";
        String suffixes5 = "";
       
 
        String [] givenNames6 = {"!#$%!#%#)#*%", "A@#@%^$@#&#%"};
        String [] middleNames6 = {"B4%@#%^@NTN%^", "#$^FAFSBA>Q@#$Q$GQ"};
        String [] familyNames6 = {"Å125 !@#$^V#^#%"};
        String prefixes6 = "";
        String suffixes6 = "";
        

        String [] givenNames7 = {"  ", "     "};   //these have spaces and tabs
        String [] middleNames7 = {"       "};
        String [] familyNames7 = {"       ", "    "};
        String prefixes7 = "";
        String suffixes7 = "";
               
              
        personName1 = new PersonName(prefixes1, givenNames1, middleNames1, familyNames1, suffixes1);
        personName2 = new PersonName(prefixes2, givenNames2, middleNames2, familyNames2, suffixes2);
        personName3 = new PersonName(prefixes3, givenNames3, middleNames3, familyNames3, suffixes3);
        personName4 = new PersonName(prefixes4, givenNames4, middleNames4, familyNames4, suffixes4);
        personName5 = new PersonName(prefixes5, givenNames5, middleNames5, familyNames5, suffixes5);
        personName6 = new PersonName(prefixes6, givenNames6, middleNames6, familyNames6, suffixes6);
        personName7 = new PersonName(prefixes7, givenNames7, middleNames7, familyNames7, suffixes7) ;
        
        releaseDate= new DateTime("2008-12-13T12:34:56.789-05:00");
        updatedDate= new DateTime("2011-12-13T18:57:54.321-05:00");
        accessDate= new DateTime("2011-12-30T20:10:44.993-05:00");
        title = "Gorillas in our Midst";
        version = "1.0";
        archive = "The Data Conservancy";
        locator = "doi:10.xxxx/officiallookingdoi.12345";

        
}

    @Test
    public void testSingleAuthorMinimal(){
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(personName1);
        //setup for minimal citation, ESIP required elements only
        Citation citation = new Citation(creators, releaseDate, title, null, archive, null, locator, accessDate, null);
        String result = citationFormatter.formatHtml(citation);
        assertEquals("Zebub, B.L. (2008). <i>Gorillas in our Midst.</i> The Data Conservancy. doi:10.xxxx/officiallookingdoi.12345. Accessed 30 Dec 2011.", result);

    }

    @Test
    public void testSingleAuthorFull(){
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(personName1);
        Citation citation = new Citation(creators, releaseDate, title, version, archive, resourceType, locator, accessDate, updatedDate);
        String result = citationFormatter.formatHtml(citation);
        assertEquals("Zebub, B.L. (2008), updated 2011. <i>Gorillas in our Midst.</i> <i>Version 1.0.</i> The Data Conservancy. cool resource. doi:10.xxxx/officiallookingdoi.12345. Accessed 30 Dec 2011.", result);

    }

    public void testSingleAuthorHyphenatedGivenName(){
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(personName2);
        Citation citation = new Citation(creators, releaseDate, title, version, archive, resourceType, locator, accessDate, updatedDate);
        String result = citationFormatter.formatHtml(citation);
        assertEquals("van der Waerden, J-P. (2008), updated 2011. <i>Gorillas in our Midst.</i> <i>Version 1.0.</i> The Data Conservancy. cool resource. doi:10.xxxx/officiallookingdoi.12345 Accessed 30 Dec 2011.", result);

    }    
   @Test
    public void testSingleAuthorCompoundNames(){
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(personName3);
        Citation citation = new Citation(creators, releaseDate, title, version, archive, resourceType, locator, accessDate, updatedDate);
        String result = citationFormatter.formatHtml(citation);
        assertEquals("van der Waerden, B.J.B.J. (2008), updated 2011. <i>Gorillas in our Midst.</i> <i>Version 1.0.</i> The Data Conservancy. cool resource. doi:10.xxxx/officiallookingdoi.12345. Accessed 30 Dec 2011.", result);
   }
    
   @Test
    public void testTwoAuthors(){
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(personName1);
        creators.add(personName2);
        Citation citation = new Citation(creators, releaseDate, title, version, archive, resourceType, locator, accessDate, updatedDate);
        String result = citationFormatter.formatHtml(citation);
        assertEquals("Zebub, B.L. and J-P. van der Waerden. (2008), updated 2011. <i>Gorillas in our Midst.</i> <i>Version 1.0.</i> The Data Conservancy. cool resource. doi:10.xxxx/officiallookingdoi.12345. Accessed 30 Dec 2011.", result);
        
    }

   @Test
    public void testThreeAuthors(){
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(personName1);
        creators.add(personName2);
        creators.add(personName3);
        Citation citation = new Citation(creators, releaseDate, title, version, archive, resourceType, locator, accessDate, updatedDate);
        String result = citationFormatter.formatHtml(citation);
        assertEquals("Zebub, B.L., J-P. van der Waerden and B.J.B.J. van der Waerden. (2008), updated 2011. <i>Gorillas in our Midst.</i> <i>Version 1.0.</i> The Data Conservancy. cool resource. doi:10.xxxx/officiallookingdoi.12345. Accessed 30 Dec 2011.", result);

    }
    @Test
    public void testInitials(){
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(personName4);
        Citation citation = new Citation(creators, releaseDate, title, version, archive, resourceType, locator, accessDate, updatedDate);
        String result = citationFormatter.formatHtml(citation);
        assertEquals("Morgan, J.P.J.P. (2008), updated 2011. <i>Gorillas in our Midst.</i> <i>Version 1.0.</i> The Data Conservancy. cool resource. doi:10.xxxx/officiallookingdoi.12345. Accessed 30 Dec 2011.", result);
    }    

    @Test
    public void testInitialsWithPeriods(){
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(personName5);
        Citation citation = new Citation(creators, releaseDate, title, version, archive, resourceType, locator, accessDate, updatedDate);
        String result = citationFormatter.formatHtml(citation);
        assertEquals("Åñöä, J.Ô.J.P. (2008), updated 2011. <i>Gorillas in our Midst.</i> <i>Version 1.0.</i> The Data Conservancy. cool resource. doi:10.xxxx/officiallookingdoi.12345. Accessed 30 Dec 2011.", result);
    }
    
    @Test
    public void testNullCitation(){
        Citation citation = new Citation(null, null, null, null, null, null, null, null, null);
        String result = citationFormatter.formatHtml(citation);
        assertEquals("", result);
    }
    
    @Test
    public void testGarbage(){
        title = "@#%BN$^N$^*N^";
        version = "!@#%VB%&N@$^";
        archive = "$$^&Bn673N^N^568";
        locator = "^*N%&N$%N$^*N$^";
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(personName6);        
        Citation citation = new Citation(creators, releaseDate, title, version, archive, resourceType, locator, accessDate, updatedDate);
        String result = citationFormatter.formatHtml(citation);
        assertEquals("Å125 !@#$^V#^#%, A.B. (2008), updated 2011. <i>@#%BN$^N$^*N^.</i> <i>Version !@#%VB%&N@$^.</i> $$^&Bn673N^N^568. cool resource. ^*N%&N$%N$^*N$^. Accessed 30 Dec 2011.", result);
    }
    
    @Test
    public void testSpacesOnly(){
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(personName7);
        Citation citation = new Citation(creators, releaseDate, title, version, archive, resourceType, locator, accessDate, updatedDate);
        String result = citationFormatter.formatHtml(citation);
        assertEquals(",  (2008), updated 2011. <i>Gorillas in our Midst.</i> <i>Version 1.0.</i> The Data Conservancy. cool resource. doi:10.xxxx/officiallookingdoi.12345. Accessed 30 Dec 2011.", result);

    }
}