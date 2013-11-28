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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersonNameTest {
    
    private final static String prefixOne = "Mr.";
    private final static String prefixTwo = "Captain Cool";
    
    private final static String givenNameOne = "Kareem";
    private final static String givenNameTwo = "Wendel";
    private final static String givenNameThree = "Doogie";
    
    private final static String middleNameOne = "Toblerone";
    private final static String middleNameTwo = "Herbert";
    private final static String middleNameThree = "Walker";
    
    private final static String familyNameOne = "Abdul-Jabbar";
    private final static String familyNameTwo = "Howser";
    private final static String familyNameThree = "Chocolate";
    
    private final static String suffixOne = "M.D.";
    private final static String suffixTwo = "Jr.";
    
    private PersonName personSingleStrings;
    private PersonName personEmptyFields;
    private PersonName personArrayNames;
    private PersonName personArrayEmptyFields;
    
    @Before
    public void setup(){
        personSingleStrings = new PersonName(prefixTwo, givenNameTwo, middleNameOne, familyNameThree, suffixTwo);
        personEmptyFields = new PersonName("", givenNameThree, "", familyNameTwo, suffixOne);
        
        String [] givenNames = {givenNameOne, givenNameThree };
        String [] middleNames = {middleNameTwo, middleNameThree };
        String [] familyNames = {familyNameOne, familyNameTwo };
        
        String [] empty = new String[] {};
        personArrayNames = new PersonName(prefixOne, givenNames, middleNames, familyNames, suffixTwo);
        personArrayEmptyFields = new PersonName(prefixTwo, givenNames, empty, familyNames, "");        
    }
    
    @Test
    public void testCopy(){
        PersonName copiedSingleString = new PersonName(personSingleStrings);
        Assert.assertNotNull(copiedSingleString);
        Assert.assertEquals(copiedSingleString, personSingleStrings);
        
        PersonName copiedEmptyFields = new PersonName(personEmptyFields);
        Assert.assertEquals(copiedEmptyFields, personEmptyFields);
        
        PersonName copiedArrayNames = new PersonName(personArrayNames);
        Assert.assertEquals(copiedArrayNames, personArrayNames);
        
        PersonName copiedArrayEmptyields = new PersonName(personArrayEmptyFields);
        Assert.assertEquals(copiedArrayEmptyields, personArrayEmptyFields);        
        
    }
    
    @Test
    public void testEquality(){
        //Test that an object is equal to itself
        Assert.assertEquals(personSingleStrings, personSingleStrings);
        Assert.assertEquals(personArrayNames, personArrayNames);
        
        String[] newMiddleNames = {"Foo", "Bar"};
        PersonName newMiddleName = new PersonName(personSingleStrings);
        newMiddleName.setMiddleNames(newMiddleNames);
        Assert.assertEquals(newMiddleName, newMiddleName);
        
        Assert.assertFalse("Expected PersonName newMiddleName to be different.", personSingleStrings.equals(newMiddleName));
        
    }

    @Test
    public void testEqualHashcode() {
        String [] givenNames = {givenNameOne, givenNameThree };
        String [] middleNames = {middleNameTwo, middleNameThree };
        String [] familyNames = {familyNameOne, familyNameTwo };
        PersonName personName1 =  new PersonName(prefixOne, givenNames, middleNames, familyNames, suffixTwo);
        PersonName personName2 =  new PersonName(prefixOne, givenNames, middleNames, familyNames, suffixTwo);

        Assert.assertEquals(personName1, personName2);
    }

            
}