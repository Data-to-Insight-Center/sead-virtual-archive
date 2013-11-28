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
package org.dataconservancy.dcs.id.impl.idstore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.URL;

import java.util.HashMap;

import org.dataconservancy.dcs.id.api.Identifier;

public class HashMapStore {

    static String path = "";

    static String extension = ".ser";

    static void storeUIDHashMap(HashMap<String, Identifier> mapToStore,
                                String identifier) {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(path + identifier + extension);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(mapToStore);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    static HashMap<String, Identifier> retrieveUIDHashMap(String identifier) {
        HashMap<String, Identifier> mapToRetrieve = null;

        FileInputStream fileIn = null;

        try {
            fileIn = new FileInputStream(path + identifier + extension);
        } catch (FileNotFoundException e) {
            storeUIDHashMap(new HashMap<String, Identifier>(), identifier);
            try {
                fileIn = new FileInputStream(path + identifier + extension);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }

        if (fileIn != null) {
            try {
                ObjectInputStream in = new ObjectInputStream(fileIn);
                mapToRetrieve = (HashMap<String, Identifier>) in.readObject();
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return mapToRetrieve;
    }

    @SuppressWarnings("unchecked")
    public static HashMap<URL, Identifier> retrieveURIHashMap(String identifier) {
        HashMap<URL, Identifier> mapToRetrieve = null;

        FileInputStream fileIn = null;

        try {
            fileIn = new FileInputStream(path + identifier + extension);
        } catch (FileNotFoundException e) {
            storeUIDHashMap(new HashMap<String, Identifier>(), identifier);
            try {
                fileIn = new FileInputStream(path + identifier + extension);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }

        if (fileIn != null) {
            try {
                ObjectInputStream in = new ObjectInputStream(fileIn);
                mapToRetrieve = (HashMap<URL, Identifier>) in.readObject();
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return mapToRetrieve;
    }

    public static void storeURIHashMap(HashMap<URL, Identifier> mapToStore,
                                       String identifier) {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(path + identifier + extension);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(mapToStore);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
