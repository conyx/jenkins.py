package jenkins.python.pwm;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;

/**
 * Resolves fully qualified name of TypeDeclaration, Type and Name objects.
 */
public class NameResolver {
    
    public static String resolveName(TypeDeclaration decl) {
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
    
    public static String resolveName(Type t) {
        if (t.isArrayType()) {
            ArrayType t0 = (ArrayType)t;
            return resolveName(t0.getElementType());
        }
        else if (t.isParameterizedType()) {
            ParameterizedType t0 = (ParameterizedType)t;
            return resolveName(t0.getType());
        }
        else if (t.isPrimitiveType()) {
            PrimitiveType t0 = (PrimitiveType)t;
            return t0.getPrimitiveTypeCode().toString();
        }
        else if (t.isQualifiedType()) {
            QualifiedType t0 = (QualifiedType)t;
            return resolveName(t0.getQualifier()) + "." + t0.getName().getIdentifier();
        }
        else if (t.isSimpleType()) {
            SimpleType t0 = (SimpleType)t;
            return resolveName(t0.getName());
        }
        else if (t.isUnionType()) {
            UnionType t0 = (UnionType)t;
            return resolveName(((List<Type>)t0.types()).get(0));
        }
        else if (t.isWildcardType()) {
            WildcardType t0 = (WildcardType)t;
            if (t0.getBound() != null) {
                return resolveName(t0.getBound());
            }
            else {
                return "?";
            }
        }
        else {
            Logger.error("cannot resolve a name, unknown type");
            return "";
        }
    }
    
    public static String resolveName(Name name) {
        if (name.getRoot().getClass() != CompilationUnit.class) {
            // cannot resolve full name, root node is missing
            Logger.error("cannot resolve a name, CompilationUnit does not exist");
            return name.getFullyQualifiedName();
        }
        CompilationUnit root = (CompilationUnit)name.getRoot();
        List<ImportDeclaration> imports = root.imports();
        for (int i = 0; i < imports.size(); i++) {
            String importName = imports.get(i).getName().getFullyQualifiedName();
            if (importName.endsWith("." + name.getFullyQualifiedName())) {
                // the qualified name some.package.A is imported
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
                    // the qualified name some.package.A.B is imported
                    Logger.error("TYPE IMPORTED 2"); /// TODO remove
                    return fullName;
                }
            }
        }
        TypeDeclVisitor visitor = new TypeDeclVisitor(name.getFullyQualifiedName());
		root.accept(visitor);
        if (visitor.getFound()) {
            // the name is the use of TypeDeclaration in the same file
            Logger.error("TYPE DECLARATION"); /// TODO remove
            return resolveName(visitor.getTypeDecl());
        }
        /// TODO search in the package folder
        /// TODO search in .* imports
        // could be class from java.lang (String) or param name (T)
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
            if (resolveName(node).endsWith("." + name)) {
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
