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
package org.dataconservancy.dcs.id.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Scanner;

/**
 * UID generator.
 * 
 * Temporary generator that will need to be properly implemented for Y1P
 *
 * @author Bill Steel
 * @version $Id: UidGenerator.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public class UidGenerator {

    //TODO: Implement a proper Uid generator.
    
    static String lastUIDFilePath = "lastGeneratedUID.txt";
    
    /**
     * STUB implementation.  All this currently does is provide a 
     * monotonically increasing number.
     * Starting from 0 each time the application is restarted.
     */
    public static Integer generateNextUID() {
        Integer lastUID = getLastGeneratedUID();
        Integer newUID;
        if(lastUID != null)
        {
            newUID = lastUID + 1;
        }
        else
        {
            newUID = 0;
        }
        setLastGeneratedUID(newUID);
        return newUID;
    }
    
    private static Integer getLastGeneratedUID()
    {
        try {
            Scanner lastUIDScanner = new Scanner(new File(lastUIDFilePath));
            Integer lastUID;
            String lastUIDString = lastUIDScanner.next();
            lastUID = Integer.valueOf(lastUIDString);
            lastUIDScanner.close();
            return lastUID;
        } catch (FileNotFoundException e) {
            return 0;
        }
    }
    
    private static void setLastGeneratedUID(Integer uidValue)
    {
        try {
            FileWriter uidFileWriter = new FileWriter(lastUIDFilePath, false);
            uidFileWriter.write(uidValue.toString());
            uidFileWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
