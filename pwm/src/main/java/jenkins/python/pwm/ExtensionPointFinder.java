package jenkins.python.pwm;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.IBinding;

public class ExtensionPointFinder extends AbstractTypeDeclFinder {
    
    public ExtensionPointFinder(File sourceCodeDir) {
        super(sourceCodeDir);
    }
    
    /**
     * Determines if given TypeDeclaration implements interface ExtensionPoint.
     */
    protected boolean isWanted(TypeDeclaration typeDecl) {
        List<Type> interfaces = (List<Type>)typeDecl.superInterfaceTypes();
        for (int i = 0; i < interfaces.size(); i++) {
            Type iface_ = interfaces.get(i);
            if (iface_.isSimpleType()) {
                SimpleType iface = (SimpleType)iface_;
                Name name = iface.getName();
                String fullName = name.getFullyQualifiedName();
                Logger.verbose("type implements interface " + fullName);
                IBinding binding = name.resolveBinding();
                Logger.verbose("binding of this interface is " + binding.getName());
                if (fullName.equals("ExtensionPoint")) {
                    return true;
                }
            }
        }
        return false;
    }
}
