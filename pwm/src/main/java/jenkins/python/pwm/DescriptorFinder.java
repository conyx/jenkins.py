package jenkins.python.pwm;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Name;

public class DescriptorFinder extends AbstractTypeDeclFinder {
    
    public DescriptorFinder() {
        super();
    }
    
    /**
     * Determines if a given TypeDeclaration node extends the class Descriptor.
     */
    protected boolean isWanted(TypeDeclaration typeDecl) {
        if (!isAbstract(typeDecl)) {
            return false;
        }
        String nodeName = typeDecl.getName().getIdentifier();
        Type superClass = typeDecl.getSuperclassType();
        if (superClass != null && superClass.isParameterizedType()) {
            Type superClass2 = ((ParameterizedType)superClass).getType();
            if (superClass2.isSimpleType()) {
                Name name = ((SimpleType)superClass2).getName();
                String fullName = name.getFullyQualifiedName();
                if (fullName.equals("Descriptor") || fullName.endsWith(".Descriptor")) {
                    if (!nodeName.equals("DescriptorImpl")) {
                        Logger.info("descriptor " + nodeName + " found");
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
