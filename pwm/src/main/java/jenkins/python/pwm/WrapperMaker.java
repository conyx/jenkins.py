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
        File inputDir = getInputDir(args);
        File outputDir = getOutputDir(args);
        List<List<TypeDeclaration>> expoints = findAllExpoints(inputDir);
        List<List<TypeDeclaration>> descriptors = findAllDescriptors(inputDir);
        makeExpointWrappers(expoints, outputDir);
        makeDescrWrappers(descriptors, outputDir);
	}
    
    /**
     * Checks and returns a path to the Jenkins source code directory.
     */
    private static File getInputDir(String args[]) {
        return new File("/");
    }
    
    /**
     * Checks and returns a path to the python-wrapper plugin source code directory.
     */
    private static File getOutputDir(String args[]) {
        return new File("/");
    }
    
    private static List<List<TypeDeclaration>> findAllExpoints(File srcDir) {
        return new LinkedList<List<TypeDeclaration>>();
    }
    
    private static List<List<TypeDeclaration>> findAllDescriptors(File srcDir) {
        return new LinkedList<List<TypeDeclaration>>();
    }
    
    private static void makeExpointWrappers(List<List<TypeDeclaration>> expoints, File outputDir) {
        
    }
    
    private static void makeDescrWrappers(List<List<TypeDeclaration>> descriptors, File outputDir) {
        
    }
}
