package jenkins.python;

import java.io.File;
import java.io.IOException;
import java.util.jar.*;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;

/**
 * This class takes care of unpacking "python" folder from the plugin's "classes.jar" file
 */
public class JARUnpacker
{
    private static List<String> unpackedJARs = new LinkedList<String>();
    
    /**
     * Unpack "python" folder with all files from the given JAR file
     */
    public static synchronized void unpackPythonFiles(File jarFile) throws PythonWrapperError {
        if (unpackedJARs.contains(jarFile.getPath())) {
            // this file has been already unpacked
            return;
        }
        File destDir = jarFile.getParentFile();
        File pythonDir = new File(destDir, "python");
        if (pythonDir.isDirectory()) {
            // this file has been already unpacked in some of previous Jenkins runs
            unpackedJARs.add(jarFile.getPath());
            return;
        }
        // unpack "python" directory
        JarFile jar;
        try {
            jar = new JarFile(jarFile, false, JarFile.OPEN_READ);
        }
        catch (IOException e) {
            throw new PythonWrapperError("Cannot open JAR file: " + e.getMessage());
        }
        Enumeration entries = jar.entries();
        while (entries.hasMoreElements()) {
            try {
                JarEntry file = (JarEntry)entries.nextElement();
                if (file.getName().startsWith("python")) {
                    File f = new File(destDir, file.getName());
                    if (file.isDirectory()) {
                        f.mkdirs();
                        if (!f.isDirectory()) {
                            throw new PythonWrapperError("Cannot create directory path: " + f.getPath());
                        }
                        continue;
                    }
                    File dir = f.getParentFile();
                    dir.mkdirs();
                    if (!dir.isDirectory()) {
                        throw new PythonWrapperError("Cannot create directory path: " + dir.getPath());
                    }
                    java.io.InputStream is = jar.getInputStream(file);
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                    while (is.available() > 0) {
                        fos.write(is.read());
                    }
                    fos.close();
                    is.close();
                }
            }
            catch (IOException e) {
                throw new PythonWrapperError("Cannot unpack JAR file: " + e.getMessage());
            }
        }
        // mark this file as unpacked
        unpackedJARs.add(jarFile.getPath());
    }
}
