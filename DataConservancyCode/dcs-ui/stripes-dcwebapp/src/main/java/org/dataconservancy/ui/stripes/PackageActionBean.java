/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.ui.stripes;

/**
 *
 */

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.UrlBinding;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.SerializationImpl;
import org.dataconservancy.packaging.model.impl.TripleImpl;
import static org.dataconservancy.packaging.model.Metadata.*;

/**
 * {@code PackageActionBean } handles requests to handle
 * information on a (@code Package}
 */
@UrlBinding("/package/view_package.action")
public class PackageActionBean extends BaseActionBean {

    /**
     * The path used to render the information in the package
     */
    private final static String PACKAGE_VIEW_PATH =
            "/pages/view_package.jsp";
    private PackageImpl pkg;

    @DefaultHandler
    public ForwardResolution renderPackage(){
         pkg = makeDemoPackage();
         return new ForwardResolution(PACKAGE_VIEW_PATH);
    }

    private PackageImpl makeDemoPackage(){
        DescriptionImpl pkgDescription = new DescriptionImpl();
        SerializationImpl pkgSerialization = new SerializationImpl();
        //create serialization
        String filePathOne="mammals/bovines/cow";
        String filePathTwo="mammals/bovines/bull";
        String filePathThree="birds/dodo";
        String filePathFour="birds/sparrow";

        pkgSerialization.addChecksum("demo", filePathOne, "A4560202");
        pkgSerialization.addChecksum("demo", filePathTwo, "BF421E44");
        pkgSerialization.addChecksum("demo", filePathThree, "CD341640");
        pkgSerialization.addChecksum("demo", filePathFour, "542355A2");

        pkgSerialization.addPackageMetadata(TOTAL_SIZE_BYTES.toString(), "4572228354" );
        pkgSerialization.addPackageMetadata(TOTAL_COUNT_FILES.toString(), "4");

        //create Description
        pkgDescription.addRelationship(new TripleImpl("Scissors", "cuts", "paper"));

        //create the package to be used for demonstration
        PackageImpl demoPkg = new  PackageImpl(pkgDescription, pkgSerialization);

        return demoPkg;
    }

    public PackageImpl getPkg(){
        return pkg;
    }

    public void setPkg(PackageImpl pkg){
        this.pkg = pkg;
    }
}
