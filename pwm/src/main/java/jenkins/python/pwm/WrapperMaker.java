package jenkins.python.pwm;

import java.io.File;
import java.util.List;
import java.util.LinkedList;

import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Main class of the pwm tool.
 */
public class WrapperMaker
{
    public static void main(String args[]){
        try {
            // check input directory
            File inputDir = getInputDir(args);
            // find all extension points and all of their superclasses
            List<List<TypeDeclaration>> expoints = findAllExpoints(inputDir);
            Logger.info(new Integer(expoints.size()) + " EXTENSION POINTS FOUND");
            // find all descriptors and all of their superclasses
            List<List<TypeDeclaration>> descriptors = findAllDescriptors(inputDir);
            Logger.info(new Integer(descriptors.size()) + " DESCRIPTORS FOUND");
            // create wrappers for extension points
            makeExpointWrappers(expoints, new File("expoint"));
            // create wrappers for descriptors
            makeDescrWrappers(descriptors, new File("descriptor"));
            Logger.info("WRAPPERS HAVE BEEN SUCCESSFULLY CREATED");
        }
        catch (WrapperMakerException e) {
            Logger.error(e.getMessage());
        }
	}
    
    /**
     * Checks and returns a path to the Jenkins source code directory.
     * The suffix ./core/src/main/java/ is added to the user defined input path.
     */
    private static File getInputDir(String args[]) {
        /// TODO resolve args
        return new File("/home/tomas/repos/jenkins/core/src/main/java");
    }
    
    private static List<List<TypeDeclaration>> findAllExpoints(File srcDir) throws JavaParserException  {
        ExtensionPointFinder expointFinder = new ExtensionPointFinder(srcDir);
        List<List<TypeDeclaration>> expoints = expointFinder.getAllDeclarations();
        return expoints;
    }
    
    private static List<List<TypeDeclaration>> findAllDescriptors(File srcDir) throws JavaParserException {
        DescriptorFinder descriptorFinder = new DescriptorFinder(srcDir);
        List<List<TypeDeclaration>> descriptors = descriptorFinder.getAllDeclarations();
        return descriptors;
    }
    
    private static void makeExpointWrappers(List<List<TypeDeclaration>> expoints, File outputDir) {
        /// TODO create wrappers
    }
    
    private static void makeDescrWrappers(List<List<TypeDeclaration>> descriptors, File outputDir) {
        /// TODO create wrappers
    }
}
