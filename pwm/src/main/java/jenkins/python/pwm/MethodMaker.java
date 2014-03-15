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
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.ExpressionStatement;

public class MethodMaker {
    List<MethodDeclaration> methods;
    List<MethodDeclaration> nonabstractMethods;
    List<MethodDeclaration> abstractMethods;
    List<MethodDeclaration> constructors;
    TypeDeclaration td;
    AST ast;
    List<TypeDeclaration> oldDeclars;
    
    public MethodMaker(List<TypeDeclaration> anOldDeclars, CompilationUnit cu) {
        methods = new ArrayList<MethodDeclaration>();
        nonabstractMethods = new ArrayList<MethodDeclaration>();
        abstractMethods = new ArrayList<MethodDeclaration>();
        constructors = new ArrayList<MethodDeclaration>();
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
        createConstructors();
        // create wrappers for abstract methods
        createAbstractMethodWrappers();
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
     * Creates wrappers for abstract methods.
     */
    private void createAbstractMethodWrappers() {
        /// TODO abstract
    }
    
    /**
     * Creates constructors which just call super constructors.
     */
    private void createConstructors() {
        for (int i = 0; i < constructors.size(); i++) {
            MethodDeclaration md = (MethodDeclaration)ASTNode.copySubtree(ast, constructors.get(i));
            // set name
            SimpleName name =  (SimpleName)ASTNode.copySubtree(ast, td.getName());
            md.setName(name);
            // set public modifier
            setPublic(md);
            // add to the TD body declarations
            td.bodyDeclarations().add(md);
            // call a parent constructor
            SuperConstructorInvocation superCall = ast.newSuperConstructorInvocation();
            for (int j = 0; j < md.parameters().size(); j++) {
                SingleVariableDeclaration argument = (SingleVariableDeclaration)md.parameters().get(j);
                Name argName = (Name)ASTNode.copySubtree(ast, argument.getName());
                superCall.arguments().add(argName);
            }
            md.getBody().statements().add(superCall);
        }
            
    }
        
    
    /**
     * Finds and saves all methods in original type declarations.
     */
    private void findAllMethods() {
        // for all original type declarations
        for (int i = 0; i < oldDeclars.size(); i++) {
            TypeDeclaration oldTD = oldDeclars.get(i);
            for (int j = 0; j < oldTD.getMethods().length; j++) {
                MethodDeclaration md = oldTD.getMethods()[j];
                // want public or protected method, not a constructor and not static
                if (!md.isConstructor() && !isStatic(md) && (isPublic(md) || isProtected(md))) {
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
                // want public or protected constructor
                else if (i == 0 && md.isConstructor() && (isPublic(md) || isProtected(md))) {
                    MethodDeclaration newMD = (MethodDeclaration)ASTNode.copySubtree(ast, md);
                    // erase statements
                    newMD.setBody(ast.newBlock());
                    // erase Javadoc
                    newMD.setJavadoc(null);
                    // add the constructor to the list
                    constructors.add(newMD);
                }
            }
        }
        /// TODO verbose
        Logger.info("methods found: " + new Integer(methods.size()));
        Logger.info("abstract: " + new Integer(abstractMethods.size()));
        Logger.info("constructors: " + new Integer(constructors.size()));
    }
    
    /**
     * Sets the public modifier and deletes the protected one.
     */
    private void setPublic(MethodDeclaration md) {
        // if already public, do nothing
        if (isPublic(md)) {
            return;
        }
        // set public
        Modifier modifier = ast.newModifier(ModifierKeyword.fromFlagValue(Modifier.PUBLIC));
        md.modifiers().add(0, modifier);
        // delete protected
        for (int i = 0; i < md.modifiers().size(); i++) {
            if (((IExtendedModifier)md.modifiers().get(i)).isModifier()) {
                Modifier m = (Modifier)md.modifiers().get(i);
                if (m.isProtected()) {
                    m.delete();
                    break;
                }
            }
        }
    }
    
    /**
     * Deletes all annotations in the given method declaration.
     */
    private void deleteAnnotations(MethodDeclaration md) {
        /// TODO delete annotations
    }
    
    private boolean isIn(MethodDeclaration md, List<MethodDeclaration> list) {
        for (int i = 0; i < list.size(); i++) {
            MethodDeclaration md2 = list.get(i);
            if (!md.getName().subtreeMatch(new ASTMatcher(), md2.getName())) {
                continue;
            }
            List<SingleVariableDeclaration> varDeclars = md.parameters();
            List<SingleVariableDeclaration> varDeclars2 = md2.parameters();
            if (varDeclars.size() != varDeclars2.size()) {
                continue;
            }
            boolean identical = true;
            for (int j = 0; j < varDeclars.size(); j++) {
                Type type = varDeclars.get(j).getType();
                Type type2 = varDeclars2.get(j).getType();
                if (!type.subtreeMatch(new ASTMatcher(), type2)) {
                    identical = false;
                    break;
                }
            }
            if (identical) {
                return true;
            }
        }
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
