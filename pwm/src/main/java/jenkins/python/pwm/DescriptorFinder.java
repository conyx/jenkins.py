package jenkins.python.pwm;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Name;

public class DescriptorFinder extends AbstractTypeDeclFinder {
    
    public DescriptorFinder(File sourceCodeDir) {
        super(sourceCodeDir);
    }
    
    /**
     * Determines if a given TypeDeclaration node extends the class Descriptor.
     */
    protected boolean isWanted(TypeDeclaration typeDecl) {
        String nodeName = typeDecl.getName().getIdentifier();
        Type superClass = typeDecl.getSuperclassType();
        if (superClass != null) {
            /// TODO check if it extends Descriptor
        }
        return false;
    }
}
