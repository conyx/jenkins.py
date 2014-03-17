package jenkins.python.pwm;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.ParameterizedType;
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
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;

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
        createMethWrappers();
        // create super*() methods for all non-abstract methods
        createSuperMethods();
        // create execPython*() methods
        createExecPythonMeths();
        // create initPython() method
        createInitPython();
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
    private Statement getExecStatement(MethodDeclaration md) {
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
            if (type.isParameterizedType()) {
                ce.setType((Type)ASTNode.copySubtree(ast, ((ParameterizedType)type).getType()));
            }
            else {
                ce.setType((Type)ASTNode.copySubtree(ast, type));
            }
            ce.setExpression(mi);
            ReturnStatement rs = ast.newReturnStatement();
            rs.setExpression(ce);
            return rs;
        }
    }
    
    private boolean isDeprecated(MethodDeclaration md) {
        boolean found = false;
        for (int i = 0; i < md.modifiers().size(); i++) {
            if (((IExtendedModifier)md.modifiers().get(i)).isAnnotation()) {
                Annotation ann = (Annotation)md.modifiers().get(i);
                if (ann.getTypeName().getFullyQualifiedName().equals("Deprecated")) {
                    found = true;
                    break;
                }
            }
        }
        return found;
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
            md.modifiers().add(0, ann);
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
     * Creates a pexec.isImplemented(id) expression.
     */
    private Expression getIsImplementedExpr(int id) {
        // pexec.
        Name pexec = ast.newSimpleName("pexec");
        // pexec.isImplemented
        SimpleName methodName = ast.newSimpleName("isImplemented");
        // pexec.isImplemented(id)
        NumberLiteral idArg = ast.newNumberLiteral(new Integer(id).toString());
        MethodInvocation mi = ast.newMethodInvocation();
        mi.setExpression(pexec);
        mi.setName(methodName);
        mi.arguments().add(idArg);
        return mi;
    }
    
    /**
     * Creates a "return super.method(args)" statement for the given method
     */
    private Statement getSuperMethCall(MethodDeclaration md) {
        SuperMethodInvocation smi = ast.newSuperMethodInvocation();
        smi.setName((SimpleName)ASTNode.copySubtree(ast, md.getName()));
        for (int i = 0; i < md.parameters().size(); i++) {
            SingleVariableDeclaration svd = (SingleVariableDeclaration)md.parameters().get(i);
            smi.arguments().add((Name)ASTNode.copySubtree(ast, svd.getName()));
        }
        Type type = md.getReturnType2();
        if (md.getReturnType2().isPrimitiveType()) {
            PrimitiveType.Code ptcode = ((PrimitiveType)type).getPrimitiveTypeCode();
            if (ptcode == PrimitiveType.VOID) {
                return ast.newExpressionStatement(smi);
            }
        }
        ReturnStatement rs = ast.newReturnStatement();
        rs.setExpression(smi);
        return rs;
    }
    
    /**
     * Creates statements for the checking abstract methods implementation.
     */
    private void createCheckAbstract(Block b) {
        // add String[] jMethods = new String[size] statement
        VariableDeclarationFragment vdf1 = ast.newVariableDeclarationFragment();
        VariableDeclarationStatement vds1 = ast.newVariableDeclarationStatement(vdf1);
        vds1.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("String")), 1));
        vdf1.setName(ast.newSimpleName("jMethods"));
        ArrayCreation ac1 = ast.newArrayCreation();
        vdf1.setInitializer(ac1);
        ac1.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("String")), 1));
        ac1.dimensions().add(ast.newNumberLiteral(new Integer(abstractMethods.size()).toString()));
        b.statements().add(vds1);
        // add jMethods[i] = "methodName" statements
        for (int i = 0; i < abstractMethods.size(); i++) {
            ArrayAccess aa = ast.newArrayAccess();
            aa.setArray(ast.newSimpleName("jMethods"));
            aa.setIndex(ast.newNumberLiteral(new Integer(i).toString()));
            StringLiteral sl = ast.newStringLiteral();
            sl.setLiteralValue(abstractMethods.get(i).getName().getFullyQualifiedName());
            Assignment assignment = ast.newAssignment();
            assignment.setLeftHandSide(aa);
            assignment.setOperator(Assignment.Operator.ASSIGN);
            assignment.setRightHandSide(sl);
            b.statements().add(ast.newExpressionStatement(assignment));
        }
        // add String[] pFuncs = new String[size] statement
        VariableDeclarationFragment vdf2 = ast.newVariableDeclarationFragment();
        VariableDeclarationStatement vds2 = ast.newVariableDeclarationStatement(vdf2);
        vds2.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("String")), 1));
        vdf2.setName(ast.newSimpleName("pFuncs"));
        ArrayCreation ac2 = ast.newArrayCreation();
        vdf2.setInitializer(ac2);
        ac2.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("String")), 1));
        ac2.dimensions().add(ast.newNumberLiteral(new Integer(abstractMethods.size()).toString()));
        b.statements().add(vds2);
        // add pFuncs[i] = "function_name" statements
        for (int i = 0; i < abstractMethods.size(); i++) {
            ArrayAccess aa = ast.newArrayAccess();
            aa.setArray(ast.newSimpleName("pFuncs"));
            aa.setIndex(ast.newNumberLiteral(new Integer(i).toString()));
            StringLiteral sl = ast.newStringLiteral();
            String methodName = abstractMethods.get(i).getName().getFullyQualifiedName();
            sl.setLiteralValue(NameConvertor.javaMethToPythonFunc(methodName));
            Assignment assignment = ast.newAssignment();
            assignment.setLeftHandSide(aa);
            assignment.setOperator(Assignment.Operator.ASSIGN);
            assignment.setRightHandSide(sl);
            b.statements().add(ast.newExpressionStatement(assignment));
        }
        // add Class<?>[][] argTypes = new Class<?>[size][] statement
        VariableDeclarationFragment vdf3 = ast.newVariableDeclarationFragment();
        VariableDeclarationStatement vds3 = ast.newVariableDeclarationStatement(vdf3);
        vds3.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("Class")), 2));
        vdf3.setName(ast.newSimpleName("argTypes"));
        ArrayCreation ac3 = ast.newArrayCreation();
        vdf3.setInitializer(ac3);
        ac3.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("Class")), 2));
        ac3.dimensions().add(ast.newNumberLiteral(new Integer(abstractMethods.size()).toString()));
        b.statements().add(vds3);
        // add argTypes[i] = new Class[size] statements
        for (int i = 0; i < abstractMethods.size(); i++) {
            ArrayAccess aa = ast.newArrayAccess();
            aa.setArray(ast.newSimpleName("argTypes"));
            aa.setIndex(ast.newNumberLiteral(new Integer(i).toString()));
            Assignment assignment = ast.newAssignment();
            assignment.setLeftHandSide(aa);
            assignment.setOperator(Assignment.Operator.ASSIGN);
            ArrayCreation ac = ast.newArrayCreation();
            ac.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("Class")), 1));
            int argc = abstractMethods.get(i).parameters().size();
            ac.dimensions().add(ast.newNumberLiteral(new Integer(argc).toString()));
            assignment.setRightHandSide(ac);
            b.statements().add(ast.newExpressionStatement(assignment));
            // add argTypes[i][j] = ArgumentType.class statements
            for (int j = 0; j < abstractMethods.get(i).parameters().size(); j++) {
                SingleVariableDeclaration svd;
                svd = (SingleVariableDeclaration)abstractMethods.get(i).parameters().get(j);
                Type type = svd.getType();
                if (type.isParameterizedType()) {
                    type = ((ParameterizedType)type).getType();
                }
                ArrayAccess aa2 = ast.newArrayAccess();
                aa2.setArray(ast.newSimpleName("argTypes"));
                aa2.setIndex(ast.newNumberLiteral(new Integer(i).toString()));
                ArrayAccess aa3 = ast.newArrayAccess();
                aa3.setArray(aa2);
                aa3.setIndex(ast.newNumberLiteral(new Integer(j).toString()));
                Assignment assignment2 = ast.newAssignment();
                assignment2.setLeftHandSide(aa3);
                assignment2.setOperator(Assignment.Operator.ASSIGN);
                TypeLiteral tl = ast.newTypeLiteral();
                tl.setType((Type)ASTNode.copySubtree(ast, type));
                assignment2.setRightHandSide(tl);
                b.statements().add(ast.newExpressionStatement(assignment2));
            }
        }
        MethodInvocation mi = ast.newMethodInvocation();
        // pexec.
        Name pexec = ast.newSimpleName("pexec");
        mi.setExpression(pexec);
        // pexec.checkAbstrMethods
        SimpleName methodName = ast.newSimpleName("checkAbstrMethods");
        mi.setName(methodName);
        // pexec.checkAbstrMethods(jMethods, pFuncs, argTypes);
        Name name1 = ast.newSimpleName("jMethods");
        mi.arguments().add(name1);
        Name name2 = ast.newSimpleName("pFuncs");
        mi.arguments().add(name2);
        Name name3 = ast.newSimpleName("argTypes");
        mi.arguments().add(name3);
        b.statements().add(ast.newExpressionStatement(mi));
    }
    
    /**
     * Creates statements for the registration python functions.
     */
    private void createRegisterFunc(Block b) {
        // add String[] functions = new String[size] statement
        VariableDeclarationFragment vdf1 = ast.newVariableDeclarationFragment();
        VariableDeclarationStatement vds1 = ast.newVariableDeclarationStatement(vdf1);
        vds1.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("String")), 1));
        vdf1.setName(ast.newSimpleName("functions"));
        ArrayCreation ac1 = ast.newArrayCreation();
        vdf1.setInitializer(ac1);
        ac1.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("String")), 1));
        ac1.dimensions().add(ast.newNumberLiteral(new Integer(nonabstractMethods.size()).toString()));
        b.statements().add(vds1);
        // add functions[i] = "function_name" statements
        for (int i = 0; i < nonabstractMethods.size(); i++) {
            ArrayAccess aa = ast.newArrayAccess();
            aa.setArray(ast.newSimpleName("functions"));
            aa.setIndex(ast.newNumberLiteral(new Integer(i).toString()));
            StringLiteral sl = ast.newStringLiteral();
            String methodName = nonabstractMethods.get(i).getName().getFullyQualifiedName();
            sl.setLiteralValue(NameConvertor.javaMethToPythonFunc(methodName));
            Assignment assignment = ast.newAssignment();
            assignment.setLeftHandSide(aa);
            assignment.setOperator(Assignment.Operator.ASSIGN);
            assignment.setRightHandSide(sl);
            b.statements().add(ast.newExpressionStatement(assignment));
        }
        // add int[] argsCount = new int[size] statement
        VariableDeclarationFragment vdf2 = ast.newVariableDeclarationFragment();
        VariableDeclarationStatement vds2 = ast.newVariableDeclarationStatement(vdf2);
        vds2.setType(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.INT), 1));
        vdf2.setName(ast.newSimpleName("argsCount"));
        ArrayCreation ac2 = ast.newArrayCreation();
        vdf2.setInitializer(ac2);
        ac2.setType(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.INT), 1));
        ac2.dimensions().add(ast.newNumberLiteral(new Integer(nonabstractMethods.size()).toString()));
        b.statements().add(vds2);
        // add argsCount[i] = argc statements
        for (int i = 0; i < nonabstractMethods.size(); i++) {
            ArrayAccess aa = ast.newArrayAccess();
            aa.setArray(ast.newSimpleName("argsCount"));
            aa.setIndex(ast.newNumberLiteral(new Integer(i).toString()));
            Assignment assignment = ast.newAssignment();
            assignment.setLeftHandSide(aa);
            assignment.setOperator(Assignment.Operator.ASSIGN);
            int argc = nonabstractMethods.get(i).parameters().size();
            assignment.setRightHandSide(ast.newNumberLiteral(new Integer(argc).toString()));
            b.statements().add(ast.newExpressionStatement(assignment));
        }
        MethodInvocation mi = ast.newMethodInvocation();
        // pexec.
        Name pexec = ast.newSimpleName("pexec");
        mi.setExpression(pexec);
        // pexec.registerFunctions
        SimpleName methodName = ast.newSimpleName("registerFunctions");
        mi.setName(methodName);
        // pexec.registerFunctions(functions, argsCount);
        Name name1 = ast.newSimpleName("functions");
        mi.arguments().add(name1);
        Name name2 = ast.newSimpleName("argsCount");
        mi.arguments().add(name2);
        b.statements().add(ast.newExpressionStatement(mi));
    }
    
    /**
     * Creates initPython() method.
     */
    private void createInitPython() {
        // create a new method
        MethodDeclaration md = ast.newMethodDeclaration();
        md.setName(ast.newSimpleName("initPython"));
        // add to the TD body declarations
        td.bodyDeclarations().add(1, md);
        // set private modifier
        setPrivate(md);
        // create block with IF statement
        md.setBody(ast.newBlock());
        IfStatement ifSt = ast.newIfStatement();
        Block ifBlock = ast.newBlock();
        ifSt.setThenStatement(ifBlock);
        md.getBody().statements().add(ifSt);
        // create pexec == null expression
        InfixExpression infix = ast.newInfixExpression();
        infix.setLeftOperand(ast.newSimpleName("pexec"));
        infix.setOperator(InfixExpression.Operator.EQUALS);
        infix.setRightOperand(ast.newNullLiteral());
        ifSt.setExpression(infix);
        // create pexec = new PythonExecutor(this) statement
        Assignment assignment = ast.newAssignment();
        assignment.setLeftHandSide(ast.newSimpleName("pexec"));
        assignment.setOperator(Assignment.Operator.ASSIGN);
        ClassInstanceCreation cic = ast.newClassInstanceCreation();
        cic.setType(ast.newSimpleType(ast.newSimpleName("PythonExecutor")));
        cic.arguments().add(ast.newThisExpression());
        assignment.setRightHandSide(cic);
        ifBlock.statements().add(ast.newExpressionStatement(assignment));
        createCheckAbstract(ifBlock);
        createRegisterFunc(ifBlock);
    }
    
    /**
     * Creates execPython*() methods, which call pexec.execPython*() methods.
     */
    private void createExecPythonMeths() {
        String content;
        content = "public Object execPython(String function, Object ... params)" +
                  "{initPython(); return pexec.execPython(function, params);}" +
                  "public byte execPythonByte(String function, Object ... params)" +
                  "{initPython(); return pexec.execPythonByte(function, params);}" +
                  "public short execPythonShort(String function, Object ... params)" +
                  "{initPython(); return pexec.execPythonShort(function, params);}" +
                  "public char execPythonChar(String function, Object ... params)" +
                  "{initPython(); return pexec.execPythonChar(function, params);}" +
                  "public int execPythonInt(String function, Object ... params)" +
                  "{initPython(); return pexec.execPythonInt(function, params);}" +
                  "public long execPythonLong(String function, Object ... params)" +
                  "{initPython(); return pexec.execPythonLong(function, params);}" +
                  "public float execPythonFloat(String function, Object ... params)" +
                  "{initPython(); return pexec.execPythonFloat(function, params);}" +
                  "public double execPythonDouble(String function, Object ... params)" +
                  "{initPython(); return pexec.execPythonDouble(function, params);}" +
                  "public boolean execPythonBool(String function, Object ... params)" +
                  "{initPython(); return pexec.execPythonBool(function, params);}" +
                  "public void execPythonVoid(String function, Object ... params)" +
                  "{initPython(); pexec.execPythonVoid(function, params);}";
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(content.toCharArray());
        parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
        TypeDeclaration decl = (TypeDeclaration)parser.createAST(null);
        for (int i = 0; i < decl.getMethods().length; i++) {
            td.bodyDeclarations().add(ASTNode.copySubtree(ast, decl.getMethods()[i]));
        }
    }
    
    /**
     * Creates super*() methods for all non-abstract original methods.
     */
    private void createSuperMethods() {
        for (int i = 0; i < nonabstractMethods.size(); i++) {
            // get original method
            MethodDeclaration md = (MethodDeclaration)ASTNode.copySubtree(ast, nonabstractMethods.get(i));
            // set public modifier
            setPublic(md);
            // add to the TD body declarations
            td.bodyDeclarations().add(md);
            // delete annotations
            deleteAnnotations(md);
            // add "return super.method(args)" statement
            Statement superSt = getSuperMethCall(md);
            md.getBody().statements().add(superSt);
            // change name [name -> superName]
            String name = md.getName().getFullyQualifiedName();
            name = "super" + name.substring(0, 1).toUpperCase() + name.substring(1);
            md.setName(ast.newSimpleName(name));
        }
    }
    
     /**
     * Creates wrappers for non-abstract methods.
     */
    private void createMethWrappers() {
        for (int i = 0; i < nonabstractMethods.size(); i++) {
            MethodDeclaration md = (MethodDeclaration)ASTNode.copySubtree(ast, nonabstractMethods.get(i));
            // set public modifier
            setPublic(md);
            // add to the TD body declarations
            td.bodyDeclarations().add(md);
            // add @Override annotation
            addOverride(md);
            // add initPython() call
            MethodInvocation mi = ast.newMethodInvocation();
            mi.setName(ast.newSimpleName("initPython"));
            md.getBody().statements().add(ast.newExpressionStatement(mi));
            // get pexec.execPython() statement
            Statement pexec = getExecStatement(md);
            // get pexec.isImplemented(id) expression
            Expression impl = getIsImplementedExpr(i);
            // get "return super.method(args)" statement
            Statement superSt = getSuperMethCall(md);
            // create if-else statement
            IfStatement ifSt = ast.newIfStatement();
            ifSt.setExpression(impl);
            Block ifBlock = ast.newBlock();
            ifBlock.statements().add(pexec);
            ifSt.setThenStatement(ifBlock);
            Block elseBlock = ast.newBlock();
            elseBlock.statements().add(superSt);
            ifSt.setElseStatement(elseBlock);
            md.getBody().statements().add(ifSt);
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
            md.getBody().statements().add(getExecStatement(md));
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
                // want public or protected method, not a constructor, not static and not final
                if (!md.isConstructor() && !isStatic(md) && (isPublic(md) || isProtected(md)) &&
                    !isFinal(md) && !isDeprecated(md)) {
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
        Logger.verbose("methods found: " + new Integer(methods.size()));
        Logger.verbose("abstract: " + new Integer(abstractMethods.size()));
        Logger.verbose("constructors: " + new Integer(constructors.size()));
    }
    
    private void setPrivate(MethodDeclaration md) {
        Modifier modifier = ast.newModifier(ModifierKeyword.fromFlagValue(Modifier.PRIVATE));
        md.modifiers().add(modifier);
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
        List<Annotation> list = new ArrayList<Annotation>();
        for (int i = 0; i < md.modifiers().size(); i++) {
            if (((IExtendedModifier)md.modifiers().get(i)).isAnnotation()) {
                Annotation a = (Annotation)md.modifiers().get(i);
                list.add(a);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).delete();
        }
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
                if (type.isParameterizedType()) {
                    type = ((ParameterizedType)type).getType();
                }
                Type type2 = varDeclars2.get(j).getType();
                if (type2.isParameterizedType()) {
                    type2 = ((ParameterizedType)type2).getType();
                }
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
    
    /**
     * Determines if the method declaration is final.
     */
    private boolean isFinal(MethodDeclaration md) {
        if (Modifier.isFinal(md.getModifiers())) {
            return true;
        }
        else {
            return false;
        }
    }
}
