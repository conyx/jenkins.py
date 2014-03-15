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
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Annotation;

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
        createAbstrMethWrappers();
        // create wrappers for non-abstract methods
        /// TODO non-abstract
        // create super*() methods for all non-abstract methods
        /// TODO super*()
        // create execPython*() methods
        /// TODO execPython
        // create initPython() method
        /// TODO initPython()
    }
    
    private SimpleName getExecMethodName(MethodDeclaration md) {
        Type type = md.getReturnType2();
        String name = "execPython";
        if (type.isPrimitiveType()) {
            PrimitiveType ptype = (PrimitiveType)type;
            PrimitiveType.Code ptcode = ptype.getPrimitiveTypeCode();
            if (ptcode == PrimitiveType.BYTE) {
                name += "Byte";
            }
            else if (ptcode == PrimitiveType.SHORT) {
                name += "Short";
            }
            else if (ptcode == PrimitiveType.CHAR) {
                name += "Char";
            }
            else if (ptcode == PrimitiveType.INT) {
                name += "Int";
            }
            else if (ptcode == PrimitiveType.LONG) {
                name += "Long";
            }
            else if (ptcode == PrimitiveType.FLOAT) {
                name += "Float";
            }
            else if (ptcode == PrimitiveType.DOUBLE) {
                name += "Double";
            }
            else if (ptcode == PrimitiveType.BOOLEAN) {
                name += "Bool";
            }
            else if (ptcode == PrimitiveType.VOID) {
                name += "Void";
            }
        }
        return ast.newSimpleName(name);
    }
    
    private StringLiteral getPythonFuncName(MethodDeclaration md) {
        String methodName = md.getName().getFullyQualifiedName();
        StringLiteral sl = ast.newStringLiteral();
        sl.setLiteralValue(NameConvertor.javaMethToPythonFunc(methodName));
        return sl;
    }
    
    private void setExecArguments(MethodDeclaration md, MethodInvocation mi) {
        for (int i = 0; i < md.parameters().size(); i++) {
            SingleVariableDeclaration svd = (SingleVariableDeclaration)md.parameters().get(i);
            Type type = svd.getType();
            if (!type.isPrimitiveType()) {
                // arg -> arg
                mi.arguments().add((Name)ASTNode.copySubtree(ast, svd.getName()));
            }
            else {
                // arg -> DataConvertor.from*(arg)
                Name dataConvertor = ast.newSimpleName("DataConvertor");
                MethodInvocation mi2 = ast.newMethodInvocation();
                mi2.setExpression(dataConvertor);
                PrimitiveType ptype = (PrimitiveType)type;
                PrimitiveType.Code ptcode = ptype.getPrimitiveTypeCode();
                String methName = "from";
                if (ptcode == PrimitiveType.BYTE) {
                    methName += "Byte";
                }
                else if (ptcode == PrimitiveType.SHORT) {
                    methName += "Short";
                }
                else if (ptcode == PrimitiveType.CHAR) {
                    methName += "Char";
                }
                else if (ptcode == PrimitiveType.INT) {
                    methName += "Int";
                }
                else if (ptcode == PrimitiveType.LONG) {
                    methName += "Long";
                }
                else if (ptcode == PrimitiveType.FLOAT) {
                    methName += "Float";
                }
                else if (ptcode == PrimitiveType.DOUBLE) {
                    methName += "Double";
                }
                else if (ptcode == PrimitiveType.BOOLEAN) {
                    methName += "Bool";
                }
                mi2.setName(ast.newSimpleName(methName));
                mi2.arguments().add(ASTNode.copySubtree(ast, svd.getName()));
                mi.arguments().add(mi2);
            }
        }
    }
    
    /**
     * Creates a "return pexec.execPython*(params)" statement for the given method.
     */
    private Statement createExecStatement(MethodDeclaration md) {
        // pexec.
        Name pexec = ast.newSimpleName("pexec");
        // pexec.execPython*
        SimpleName methodName = getExecMethodName(md);
        // pexec.execPython*("function_name"
        StringLiteral pythonFunction = getPythonFuncName(md);
        MethodInvocation mi = ast.newMethodInvocation();
        mi.setExpression(pexec);
        mi.setName(methodName);
        mi.arguments().add(pythonFunction);
        // pexec.execPython*("function_name", all, arguments)
        setExecArguments(md, mi);
        Type type = md.getReturnType2();
        if (type.isPrimitiveType()) {
            PrimitiveType.Code ptcode = ((PrimitiveType)type).getPrimitiveTypeCode();
            if (ptcode == PrimitiveType.VOID) {
                // pexec.execPythonVoid("function_name", all, arguments)
                return ast.newExpressionStatement(mi);
            }
            else {
                // return pexec.execPython*("function_name", all, arguments)
                ReturnStatement rs = ast.newReturnStatement();
                rs.setExpression(mi);
                return rs;
            }
        }
        else {
            // return (Type)pexec.execPython*("function_name", all, arguments)
            CastExpression ce = ast.newCastExpression();
            ce.setType((Type)ASTNode.copySubtree(ast, type));
            ce.setExpression(mi);
            ReturnStatement rs = ast.newReturnStatement();
            rs.setExpression(ce);
            return rs;
        }
    }
    
    /**
     * Adds "@Override" annotation to the method declaration.
     */
    private void addOverride(MethodDeclaration md) {
        boolean found = false;
        for (int i = 0; i < md.modifiers().size(); i++) {
            if (((IExtendedModifier)md.modifiers().get(i)).isAnnotation()) {
                Annotation ann = (Annotation)md.modifiers().get(i);
                if (ann.getTypeName().getFullyQualifiedName().equals("Override")) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            Annotation ann = ast.newMarkerAnnotation();
            ann.setTypeName(ast.newSimpleName("Override"));
            md.modifiers().add(ann);
        }
    }
    
    private void deleteAbstract(MethodDeclaration md) {
        Modifier abstr = null;
        for (int i = 0; i < md.modifiers().size(); i++) {
            if (((IExtendedModifier)md.modifiers().get(i)).isModifier()) {
                Modifier mod = (Modifier)md.modifiers().get(i);
                if (mod.isAbstract()) {
                    abstr = mod;
                    break;
                }
            }
        }
        if (abstr != null) {
            abstr.delete();
        }
    }
    
    /**
     * Creates wrappers for abstract methods.
     */
    private void createAbstrMethWrappers() {
        for (int i = 0; i < abstractMethods.size(); i++) {
            MethodDeclaration md = (MethodDeclaration)ASTNode.copySubtree(ast, abstractMethods.get(i));
            // set public modifier
            setPublic(md);
            // add to the TD body declarations
            td.bodyDeclarations().add(md);
            // add @Override annotation
            addOverride(md);
            // delete abstract modifier
            deleteAbstract(md);
            // add initPython() call
            MethodInvocation mi = ast.newMethodInvocation();
            mi.setName(ast.newSimpleName("initPython"));
            md.getBody().statements().add(ast.newExpressionStatement(mi));
            // add pexec.execPython() call
            md.getBody().statements().add(createExecStatement(md));
        }
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
                        methods.add(newMD);
                        if (isAbstract(newMD)) {
                            abstractMethods.add(newMD);
                        }
                        else {
                            nonabstractMethods.add(newMD);
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
