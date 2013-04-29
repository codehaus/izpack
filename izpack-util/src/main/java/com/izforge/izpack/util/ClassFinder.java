package com.izforge.izpack.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassFinder {

    private static final Logger LOGGER = Logger.getLogger(ClassFinder.class.getName());

    public static final String CLASS_FILE_SUFFIX = ".class";


    public static Set<Class<?>> findClassesImplementing(final Class<?> interfaceClass, final Package fromPackage) {

        if (interfaceClass == null) {
            LOGGER.severe("Unknown subclass");
            return null;
        }

        if (fromPackage == null) {
            LOGGER.severe("Unknown package");
            return null;
        }

        final Set<Class<?>> rVal = new HashSet<Class<?>>();
        try {
            final Set<Class<?>> classes = getAllClassesFromPackage(fromPackage.getName());
            LOGGER.fine("---> Package: " + fromPackage.getName());
            LOGGER.fine("---> Number of classes: " + classes.size());
            for (Class<?> c : classes)
            {
                if (c.equals(interfaceClass)) {
                    LOGGER.info("Found the interface definition " + interfaceClass.getCanonicalName() + " itself");
                    continue;
                }
                else if (!interfaceClass.isAssignableFrom(c)) {
                    continue;
                }
                else {
                    rVal.add(c);
                    LOGGER.fine("---> Class " + c.getCanonicalName() + " overrides " + interfaceClass);
                }
            }
        }
        catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Error reading package name", e);
        }
        catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading classes in package", e);
        }

        return rVal;
    }

    /**
     * Load all classes from a package.
     *
     * @param packageName
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Set<Class<?>> getAllClassesFromPackage(final String packageName) throws ClassNotFoundException, IOException {
        Set<String> classNames = scanClasses(packageName);
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for (String className : classNames)
        {
            classes.add(Class.forName(className));
        }
        return classes;
    }


    /**
     * Scan the classpath and calls the registered consumers for each class found.
     * Quietly ignores files listed on the classpath that do not exist.
     */
    public static Set<String> scanClasses(final String packageName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        URL packageUrl = classLoader.getResource(packageName.replace('.', '/'));
        URLClassLoader ucl = new URLClassLoader(new URL[] { packageUrl });
        Set<String> classNames = new HashSet<String>();
        URL[] urls = ((URLClassLoader) ucl).getURLs();
        for (int i = 0; i < urls.length; i++) {
            Set<String> classNamesInFile;
            if (urls[i].getProtocol().equals("jar")) {
                // process jar file
                classNamesInFile = processJarFile(urls[i]);
            } else {
                // process as root dir
                File f = new File(urls[i].getPath());
                classNamesInFile = processFileDir(f, "", f.getPath());
            }
            classNames.addAll(classNamesInFile);
        }
        return classNames;
    }

    /**
     * Scan the jar file for .class files to check
     * @param f the jar file to scan
     */
    protected static Set<String> processJarFile(URL url) {
        Set<String> classNames = new HashSet<String>();
        try {
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            conn.setUseCaches(false);
            JarFile jarFile = conn.getJarFile();
            Enumeration<JarEntry> e = jarFile.entries();
            String packageName = url.getFile().substring(url.getPath().indexOf("!/") + 2);
            while (e.hasMoreElements())
            {
                JarEntry jarEntry = e.nextElement();
                if (
                        (!jarEntry.isDirectory()) &&
                        jarEntry.getName().endsWith(CLASS_FILE_SUFFIX) &&
                        jarEntry.getName().startsWith(packageName)) {
                    String className = jarEntry.getName();
                    className = className.substring(0, className.lastIndexOf(CLASS_FILE_SUFFIX));
                    className = className.replace('/', '.');
                    classNames.add(className);
                }
            }
        } catch (Throwable t) {
            if (!(t instanceof RuntimeException)) {
                throw new RuntimeException(t.toString(), t);
            } else {
                throw (RuntimeException) t;
            }
        }
        return classNames;
    }

    /**
     * Process a file or directory. Recursively processes directories found.
     * @param f the file or directory to process
     * @param packagePath the current package name
     * @param rootDir the root directory
     * @param top true if this is the top of the package.
     */
    protected static Set<String> processFileDir(File f, String packagePath, String rootDir) {
        Set<String> classNames = new HashSet<String>();
        if (f.isDirectory()) {
            // iterate files and dirs
            if (f.getPath().equals(rootDir)) {
                packagePath = "";
            } else {
                packagePath = packagePath.length() == 0 ? f.getName() : packagePath + "." + f.getName();
            }
            String[] files = f.list();
            for (String fileName : files) {
                File dirFile = new File(f, fileName);
                processFileDir(dirFile, packagePath, rootDir);
            }
        } else if (f.isFile() && f.getName().endsWith(CLASS_FILE_SUFFIX)) {
            // only recognize lower class .class
            String className = f.getName();
            // strip .class
            className = className.substring(0, className.lastIndexOf(CLASS_FILE_SUFFIX));
            className = packagePath.length() == 0 ? className : packagePath + "." + className;
            classNames.add(className);
        }
        return classNames;
    }

}