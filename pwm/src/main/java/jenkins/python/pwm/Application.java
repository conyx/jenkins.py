package jenkins.python.pwm;

import java.io.File;
import java.util.List;
import java.util.LinkedList;

import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Main class of the PWM tool.
 */
public class Application
{
    private static File sourceDir;
    
    public static void main(String args[]){
        try {
            // check parameters
            if (!checkParams(args)) {
                return;
            }
            // find all extension points and all of their superclasses
            List<List<TypeDeclaration>> expoints = findAllExpoints();
            Logger.info(new Integer(expoints.size()) + " EXTENSION POINTS FOUND");
            // find all descriptors and all of their superclasses
            List<List<TypeDeclaration>> descriptors = findAllDescriptors();
            Logger.info(new Integer(descriptors.size()) + " DESCRIPTORS FOUND");
            // create wrappers for extension points
            makeWrappers(expoints, new File("expoint"));
            // create wrappers for descriptors
            makeWrappers(descriptors, new File("descriptor"));
            Logger.info("WRAPPERS HAVE BEEN SUCCESSFULLY CREATED");
        }
        catch (WrapperMakerException e) {
            Logger.error(e.getMessage());
        }
    }
    
    public static File getSrcDir() {
        return sourceDir;
    }
    
    /**
     * Checks parameters and sets a path to the Jenkins source code directory.
     * The suffix ./core/src/main/java/ is added to the user defined input path.
     * Determines if the program should continue.
     */
    private static boolean checkParams(String args[]) throws WrapperMakerException {
        String incorrectArgs = "incorrect arguments, try 'java -jar pwm.jar -h' for the help";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-v") || args[i].equals("--verbose")) {
                Logger.setVerbose(true);
            }
            else if (args[i].equals("-h") || args[i].equals("--help")) {
                System.out.println(
                "Python Wrapper Maker version 1.0\n" +
                "--------------------------------\n" +
                "Creates wrapper classes for the python-wrapper plugin.\n" +
                "Usage: java -jar pwm.jar [-v] -i jenkins_source_code_directory\n" +
                "   or  java -jar pwm.jar -h");
                return false;
            }
            else if (args[i].equals("-i") || args[i].equals("--input-dir")) {
                if (i == args.length-1) {
                    throw new WrapperMakerException(incorrectArgs);
                }
                else {
                    sourceDir = new File(args[i+1]);
                    sourceDir = new File(sourceDir, "core/src/main/java");
                    if (!sourceDir.isDirectory()) {
                        throw new WrapperMakerException("input directory " +
                                                        sourceDir.getPath() +
                                                        " does not exist!");
                    }
                    i++;
                }
            }
            else {
                throw new WrapperMakerException(incorrectArgs);
            }
        }
        if (sourceDir == null) {
            Logger.error("you have to determine input directory!");
            throw new WrapperMakerException(incorrectArgs);
        }
        return true;
    }
    
    private static List<List<TypeDeclaration>> findAllExpoints() throws JavaParserException  {
        ExtensionPointFinder expointFinder = new ExtensionPointFinder();
        List<List<TypeDeclaration>> expoints = expointFinder.getAllDeclarations();
        return expoints;
    }
    
    private static List<List<TypeDeclaration>> findAllDescriptors() throws JavaParserException {
        DescriptorFinder descriptorFinder = new DescriptorFinder();
        List<List<TypeDeclaration>> descriptors = descriptorFinder.getAllDeclarations();
        return descriptors;
    }
    
    private static void makeWrappers(List<List<TypeDeclaration>> declars, File outputDir)
    throws WrapperMakerException {
        WrapperMaker wm = new WrapperMaker(declars, outputDir);
        wm.makeWrappers();
    }
}
