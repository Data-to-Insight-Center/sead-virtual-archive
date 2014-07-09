package org.seadva.registry.database.model.obj.vaRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Constants like single valued attributes
 */
public class Constants {
    public static List<String> singleValuedMetadataTypes = new ArrayList<String>();

    //Single Valued Metadata types could be loaded from a Configuration File
    static {
        singleValuedMetadataTypes.add("title");
        singleValuedMetadataTypes.add("abstract");
    }
}
