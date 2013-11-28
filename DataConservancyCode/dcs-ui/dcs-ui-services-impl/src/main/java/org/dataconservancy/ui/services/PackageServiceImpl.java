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
package org.dataconservancy.ui.services;


import org.dataconservancy.ui.exceptions.PackageException;
import org.dataconservancy.ui.model.Package;
import org.dataconservancy.ui.dao.PackageDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

/**
 * An implementation of {@link PackageService}.
 */
public class PackageServiceImpl implements PackageService {

    private Package pkg;
    private PackageDAO packageDao;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Constructs a new PackageService
     *
     * @param packageDao Package DAO, must not be null
     * @throws IllegalArgumentException if any parameters are null.
     */
    public PackageServiceImpl(PackageDAO packageDao){
        if (packageDao == null) {
            throw new IllegalArgumentException("PackageDAO must not be null.");
        }
        this.packageDao = packageDao;
        log.trace("Instantiated {} with {}", this, packageDao);
    }
    
    @Override
    public Package get(String id) {
        log.debug("Obtaining package for id {}", id);
        return packageDao.selectPackage(id);
    }

    @Override
    public Package create(Package pkg) throws PackageException {
        try{
            packageDao.insertPackage(pkg);
        } catch (DuplicateKeyException e){
           throw(new PackageException (e));
        }  catch (DataIntegrityViolationException e){
            throw(new PackageException(e));
        }
        log.debug("Creating package {}", pkg);
        return packageDao.selectPackage(pkg.getId());
    }

    @Override
    public Package update(Package pkg) throws PackageException{
        try{
            packageDao.updatePackage(pkg);
        } catch (DuplicateKeyException e){
            throw(new PackageException (e));
        } catch (DataIntegrityViolationException e){
            throw(new PackageException(e));
        }

        log.debug("Updating package {}", pkg);
        // in case the DB updates fields, return a new Package object.
        return packageDao.selectPackage(pkg.getId());
    }
    
    @Override
    public void remove(Package pkg) {
        packageDao.deletePackage(pkg.getId());
        log.debug("Deleting package {}", pkg);
    } 
    
    @Override
    public List<Package> findAllPackages() {
        return packageDao.selectPackage();
    }
}
