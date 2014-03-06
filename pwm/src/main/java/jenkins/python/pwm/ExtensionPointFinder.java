package jenkins.python.pwm;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Name;

public class ExtensionPointFinder extends AbstractTypeDeclFinder {
    
    public ExtensionPointFinder(File sourceCodeDir) {
        super(sourceCodeDir);
    }
    
    /**
     * Determines if a given TypeDeclaration node implements the interface ExtensionPoint.
     */
    protected boolean isWanted(TypeDeclaration typeDecl) {
        String nodeName = typeDecl.getName().getIdentifier();
        List<Type> interfaces = (List<Type>)typeDecl.superInterfaceTypes();
        for (int i = 0; i < interfaces.size(); i++) {
            Type iface_ = interfaces.get(i);
            if (iface_.isSimpleType()) {
                SimpleType iface = (SimpleType)iface_;
                Name name = iface.getName();
                String fullName = name.getFullyQualifiedName();
                if (fullName.equals("ExtensionPoint")) {
                    Logger.verbose("type " + nodeName + " implements interface " + fullName);
                    return true;
                }
            }
        }
        return false;
    }
}
