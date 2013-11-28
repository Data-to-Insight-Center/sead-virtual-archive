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
package org.dataconservancy.ui.stripes;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.ValidationError;

/**
 * Convert a String (local file path) into a FileBean.
 */
public class FileBeanTypeConverter implements TypeConverter<FileBean>{
    public FileBean convert(String path, Class<? extends FileBean> arg1,
            Collection<ValidationError> arg2) {
        File file = new File(path);
        
        return new FileBean(file, "application/octet-stream", file.getName());
    }

    public void setLocale(Locale locale) {
    }
}
