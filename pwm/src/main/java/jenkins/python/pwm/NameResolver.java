package jenkins.python.pwm;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;

/**
 * Evaluates fully qualified name of TypeDeclaration, Type and Name objects.
 */
public class NameResolver {
    
    public static String getFullName(TypeDeclaration decl) {
        String name = decl.getName().getIdentifier();
        ASTNode parent = decl.getParent();
        // resolve full name e.g.: A.B
        while (parent != null && parent.getClass() == TypeDeclaration.class) {
            name = ((TypeDeclaration)parent).getName().getIdentifier() + "." + name;
            parent = parent.getParent();
        }
        // resolve fully qualified name e.g.: some.package.A.B
        if (decl.getRoot().getClass() == CompilationUnit.class) {
            CompilationUnit root = (CompilationUnit)decl.getRoot();
            if (root.getPackage() != null) {
                PackageDeclaration pack = root.getPackage();
                name = pack.getName().getFullyQualifiedName() + "." + name;
            }
        }
        return name;
    }
    
    public static String getFullName(Type t) {
        if (t.isParameterizedType()) {
            ParameterizedType t0 = (ParameterizedType)t;
            return getFullName(t0.getType());
        }
        else if (t.isQualifiedType()) {
            QualifiedType t0 = (QualifiedType)t;
            return getFullName(t0.getQualifier()) + "." + t0.getName().getIdentifier();
        }
        else if (t.isSimpleType()) {
            SimpleType t0 = (SimpleType)t;
            return getFullName(t0.getName());
        }
        else {
            Logger.error("cannot resolve a name, unknown type");
            return "?";
        }
    }
    
    public static String getFullName(Name name) {
        if (name.getRoot().getClass() != CompilationUnit.class) {
            // cannot resolve full name, root node is missing
            Logger.error("cannot resolve a name, CompilationUnit root does not exist");
            return name.getFullyQualifiedName();
        }
        CompilationUnit root = (CompilationUnit)name.getRoot();
        List<ImportDeclaration> imports = root.imports();
        for (int i = 0; i < imports.size(); i++) {
            String importName = imports.get(i).getName().getFullyQualifiedName();
            if (importName.endsWith("." + name.getFullyQualifiedName())) {
                // A -> some.package.A (some.package.A imported)
                Logger.error("TYPE IMPORTED"); /// TODO remove
                return importName;
            }
            if (name.getFullyQualifiedName().contains(".")) {
                String[] names = name.getFullyQualifiedName().split("\\.");
                if (importName.endsWith("." + names[0])) {
                    String fullName = importName;
                    for (int j = 1; j < names.length; j++) {
                        fullName += "." + names[j];
                    }
                    // A.B -> some.package.A.B (some.package.A imported)
                    Logger.error("TYPE IMPORTED 2"); /// TODO remove
                    return fullName;
                }
            }
        }
        TypeDeclVisitor visitor = new TypeDeclVisitor(name.getFullyQualifiedName());
		root.accept(visitor);
        if (visitor.getFound()) {
            // the name is the use of the TypeDeclaration in the same file
            Logger.error("TYPE DECLARATION"); /// TODO remove
            return getFullName(visitor.getTypeDecl());
        }
        /// TODO search in the package folder
        /// TODO search in .* imports
        // still could be a class from the java.lang (String) or a param name (T, E,...)
        Logger.error("TYPE GENERIC"); /// TODO remove
        return name.getFullyQualifiedName();
    }
    
    private static class TypeDeclVisitor extends ASTVisitor {
        private boolean found = false;
        private TypeDeclaration typeDecl;
        private String name;
        
        TypeDeclVisitor(String aName) {
            super();
            name = aName;
        }
        
        public boolean visit(TypeDeclaration node) {
            if (getFullName(node).endsWith("." + name)) {
                found = true;
                typeDecl = node;
            }
            return true;
        }
        
        public boolean getFound() {
            return found;
        }
        
        public TypeDeclaration getTypeDecl() {
            return typeDecl;
        }
    }
}
