package jenkins.python.pwm;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodMaker {
    List<MethodDeclaration> methods;
    List<MethodDeclaration> nonabstractMethods;
    List<MethodDeclaration> abstractMethods;
    TypeDeclaration td;
    AST ast;
    List<TypeDeclaration> oldDeclars;
    
    public MethodMaker(List<TypeDeclaration> anOldDeclars, CompilationUnit cu) {
        methods = new ArrayList<MethodDeclaration>();
        nonabstractMethods = new ArrayList<MethodDeclaration>();
        abstractMethods = new ArrayList<MethodDeclaration>();
        td = (TypeDeclaration)cu.types().get(0);
        ast = td.getAST();
        oldDeclars = anOldDeclars;
    }
    
    /**
     * Creates all methods including constructors in the new type declaration.
     */
    public void createMethods() {
        // find all methods in original type declarations
        findAllMethods();
        // create constructors
        /// TODO constructors
        // create wrappers for abstract methods
        /// TODO abstract
        // create wrappers for non-abstract methods
        /// TODO non-abstract
        // create *Super() methods for all non-abstract methods
        /// TODO *Super()
        // create execPython*() methods
        /// TODO execPython
        // create initPython() method
        /// TODO initPython()
    }
    
    /**
     * Finds and saves all methods in original type declarations.
     */
    private void findAllMethods() {
        // for all original type declarations
        for (int i = 0; i < oldDeclars.size(); i++) {
            TypeDeclaration oldTD = oldDeclars.get(i);
            SimpleName tdName = oldTD.getName();
            for (int j = 0; j < oldTD.getMethods().length; j++) {
                MethodDeclaration md = oldTD.getMethods()[j];
                SimpleName mName = md.getName();
                // want public or protected, not a constructor and not static
                if (!areEqual(tdName, mName) && !isStatic(md) && (isPublic(md) || isProtected(md))) {
                    MethodDeclaration newMD = (MethodDeclaration)ASTNode.copySubtree(ast, md);
                    // erase statements
                    newMD.setBody(ast.newBlock());
                    // erase Javadoc
                    newMD.setJavadoc(null);
                    // add the method to the list if it is not already in
                    if (!isIn(newMD, methods)) {
                        methods.add(md);
                        if (isAbstract(md)) {
                            abstractMethods.add(md);
                        }
                        else {
                            nonabstractMethods.add(md);
                        }
                    }
                }
            }
        }
        /// TODO verbose
        Logger.info("methods found: " + new Integer(methods.size()));
        Logger.info("abstract: " + new Integer(abstractMethods.size()));
    }
    
    private boolean isIn(MethodDeclaration md, List<MethodDeclaration> list) {
        /// TODO isIn
        return false;
    }
    
    /**
     * Determines if nodes are equal.
     */
    private boolean areEqual(ASTNode node1, ASTNode node2) {
        return node1.subtreeMatch(new ASTMatcher(), node2);
    }
    
    /**
     * Determines if the method declaration is static.
     */
    private boolean isStatic(MethodDeclaration md) {
        if (Modifier.isStatic(md.getModifiers())) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Determines if the method declaration is public.
     */
    private boolean isPublic(MethodDeclaration md) {
        if (Modifier.isPublic(md.getModifiers())) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Determines if the method declaration is protected.
     */
    private boolean isProtected(MethodDeclaration md) {
        if (Modifier.isProtected(md.getModifiers())) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Determines if the method declaration is abstract.
     */
    private boolean isAbstract(MethodDeclaration md) {
        if (Modifier.isAbstract(md.getModifiers())) {
            return true;
        }
        else {
            return false;
        }
    }
}
