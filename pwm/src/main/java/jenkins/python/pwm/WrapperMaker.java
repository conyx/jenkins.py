package jenkins.python.pwm;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.TextEdit;

/**
 * Makes Python wrappers of given classes for the python-wrapper plugin.
 */
public class WrapperMaker {
    
    private List<List<TypeDeclaration>> declarations;
    private File outputDir;
    
    public WrapperMaker(List<List<TypeDeclaration>> declars, File dir) {
        declarations = declars;
        outputDir = dir;
    }
    
    public void makeWrappers() throws WrapperMakerException {
        outputDir.mkdirs();
        if (!outputDir.isDirectory()) {
            throw new WrapperMakerException("cannot create output directory " +
                                            outputDir.getPath());
        }
        /// TODO declarations.size()
        for (int i = 0; i < 1; i++) {
            makeWrapper(declarations.get(i));
        }
    }
    
    /**
     * Saves a document to the output directory.
     */
    private void saveFile(Document doc, TypeDeclaration decl) throws WrapperMakerException {
        String[] nameParts = NameResolver.getFullName(decl).split("\\.");
        String name = nameParts[nameParts.length-1] + "PW.java";
        File outputFile = new File(outputDir, name);
        try {
            FileWriter fw = new FileWriter(outputFile);
            fw.write(doc.get());
            fw.close();
        } catch (IOException e) {
            throw new WrapperMakerException("cannot save a file " + outputFile.getPath() +
                                            " caused by " + e.getMessage());
        }
        Logger.info("wrapper " + outputFile.getPath() + " created");
    }
    
    /**
     * Makes a wrapper for the given type declaration (and all of its super classes).
     */
    private void makeWrapper(List<TypeDeclaration> declars) throws WrapperMakerException {
        try {
            // init new blank Document, AST and CompilationUnit objecs
            Document document = new Document("\n");
            ASTParser parser = ASTParser.newParser(AST.JLS4);
            parser.setSource(document.get().toCharArray());
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            CompilationUnit cu = (CompilationUnit)parser.createAST(null);
            cu.recordModifications();
            AST ast = cu.getAST();
            // add package declaration
            addPackage(cu);
            // add import declarations
            addImports(cu, declars);
            // add type declaration
            /// TODO type
            // add method declarations
            /// TODO methods
            // rewrite changes to the document
            cu.rewrite(document, null);
            TextEdit edits = cu.rewrite(document, null);
            edits.apply(document);
            // save the document as a new wrapper
            saveFile(document, declars.get(0));
        }
        catch (BadLocationException e) {
            Logger.error(e.getMessage());
            String declName = NameResolver.getFullName(declars.get(0));
            throw new WrapperMakerException("internal error while generating wrapper " +
                                            "of the " + declName + " class");
        }
    }
    
    /**
     * Adds a package declaration to the compilation unit cu.
     */
    private void addPackage(CompilationUnit cu) {
        AST ast = cu.getAST();
        PackageDeclaration pd = ast.newPackageDeclaration();
        Name name = ast.newName("jenkins.python." + outputDir.getPath());
        pd.setName(name);
        cu.setPackage(pd);
    }
    
    /**
     * Determines if the node is in the list by subtreeMatch() method.
     */
    private boolean isIn(List<? extends ASTNode> list, ASTNode node) {
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).subtreeMatch(new ASTMatcher(), node)) {
                found = true;
                break;
            }
        }
        return found;
    }
    
    /**
     * Adds import declarations to the compilation unit cu.
     */
    private void addImports(CompilationUnit cu, List<TypeDeclaration> declars) {
        AST ast = cu.getAST();
        List<ImportDeclaration> imports = cu.imports();
        // for all original declarations
        for (int i = 0; i < declars.size(); i++) {
            TypeDeclaration decl = declars.get(i);
            String declName = NameResolver.getFullName(decl);
            CompilationUnit oldCU = (CompilationUnit)decl.getRoot();
            List<ImportDeclaration> oldImports = oldCU.imports();
            // for all imports in the CU of the declaration
            for (int j = 0; j < oldImports.size(); j++) {
                ImportDeclaration oldImportD = oldImports.get(j);
                // if not in the list, copy and add an import declaration
                if (!isIn(imports, oldImportD)) {
                    imports.add((ImportDeclaration)ASTNode.copySubtree(ast, oldImportD));
                }
            }
            // add also the original package name as an on-demand import declaration
            Name pckgImportName = oldCU.getPackage().getName();
            ImportDeclaration pckgImport = ast.newImportDeclaration();
            pckgImport.setName((Name)ASTNode.copySubtree(ast, pckgImportName));
            pckgImport.setOnDemand(true);
            if (!isIn(imports, pckgImport)) {
                imports.add(pckgImport);
            }
            // import also all subtypes of the original type declarations
            ImportDeclaration subtypesImport = ast.newImportDeclaration();
            subtypesImport.setName(ast.newName(declName));
            subtypesImport.setOnDemand(true);
            if (!isIn(imports, subtypesImport)) {
                imports.add(subtypesImport);
            }
            // import also all subtypes of the parent type of the original declaration
            String[] declNameParts = declName.split("\\.");
            String parentTypeName = "";
            for (int j = 0; j < declNameParts.length-1; j++) {
                parentTypeName += declNameParts[j];
                if (j < declNameParts.length-2) {
                    parentTypeName += ".";
                }
            }
            ImportDeclaration parentSubtypesImport = ast.newImportDeclaration();
            parentSubtypesImport.setName(ast.newName(parentTypeName));
            parentSubtypesImport.setOnDemand(true);
            if (!isIn(imports, parentSubtypesImport)) {
                imports.add(parentSubtypesImport);
            }
        }
        // import also jenkins.python.* names
        Name iName;
        ImportDeclaration pPckgImport;
        iName = ast.newName("jenkins.python.DataConvertor");
        pPckgImport = ast.newImportDeclaration();
        pPckgImport.setName(iName);
        imports.add(pPckgImport);
        iName = ast.newName("jenkins.python.PythonExecutor");
        pPckgImport = ast.newImportDeclaration();
        pPckgImport.setName(iName);
        imports.add(pPckgImport);
    }
}
