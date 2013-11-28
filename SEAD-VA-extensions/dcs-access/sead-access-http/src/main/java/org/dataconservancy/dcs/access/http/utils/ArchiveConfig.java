package org.dataconservancy.dcs.access.http.utils;

import org.dataconservancy.dcs.access.http.dataPackager.PackageCreator;
import org.dataconservancy.dcs.access.http.dataPackager.ZipPackageCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * A singleton holding configuration info for a DcsEntity index service.
 * <p>
 * If the attribute dcpindex.impl is set, its value is used as the name of a
 * class implementing IndexService<Dcp>. Otherwise the context
 * initialization parameter dcpquery.instance must be set to an
 * implementation of IndexService<Dcp>.
 * <p>
 */
public class ArchiveConfig {

    private static final String CONFIG_ATTR = "archiveConfig";
    private static final String PACKAGECREATOR_INSTANCE = "packageCreator";

    private PackageCreator packageCreator = null;

    public static synchronized ArchiveConfig instance(ServletContext context)
            throws ServletException {
        ArchiveConfig instance = (ArchiveConfig) context.getAttribute(CONFIG_ATTR);

        if (instance == null) {
            instance = new ArchiveConfig(context);
            context.setAttribute(CONFIG_ATTR, instance);
        }

        return instance;
    }

    private static String get_param(ServletContext context, String name)
            throws ServletException {
        String value = context.getInitParameter(name);

        if (value == null) {
            // Fail over to attribute

            Object o = context.getAttribute(name);

            if (o != null) {
                value = o.toString();
            }

            if (value == null) {
                throw new ServletException("Required context param " + name
                        + " not set.");
            }
        }

        return value;
    }

    private static final Logger log =
            LoggerFactory.getLogger(org.dataconservancy.dcs.index.dcpsolr.utils.Config.class);

    @SuppressWarnings("unchecked")
    private ArchiveConfig(ServletContext context) throws ServletException {
        Object o = context.getAttribute(PACKAGECREATOR_INSTANCE);

        log.debug("instanceOf = "+o.getClass()+"\n");
        if (o != null) {
            packageCreator = (PackageCreator) o;
        } else {
            String classname = get_param(context, PACKAGECREATOR_INSTANCE);

            log.debug("classname = "+classname+"\n");
            try {
                Class<?> klass = Class.forName(classname);

                packageCreator =
                        klass.asSubclass(ZipPackageCreator.class)
                        .newInstance();
            } catch (ClassNotFoundException e) {
                throw new ServletException(e);
            } catch (InstantiationException e) {
                throw new ServletException(e);
            } catch (IllegalAccessException e) {
                throw new ServletException(e);
            }
        }


    }

    public PackageCreator packageCreator() {
        return packageCreator;
    }
}
