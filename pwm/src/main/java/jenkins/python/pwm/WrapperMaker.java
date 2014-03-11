package jenkins.python.pwm;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
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
        /// TODO declarations.size()
        for (int i = 0; i < 1; i++) {
            makeWrapper(declarations.get(i));
        }
    }
    
    /**
     * Makes a wrapper for the given type declaration (and all of its super classes).
     */
    private void makeWrapper(List<TypeDeclaration> declars) throws WrapperMakerException {
        try {
            // init new blank Document, AST and CompilationUnit objecs
            Document document = new Document("");
            ASTParser parser = ASTParser.newParser(AST.JLS4);
            parser.setSource(document.get().toCharArray());
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            CompilationUnit cu = (CompilationUnit)parser.createAST(null);
            cu.recordModifications();
            AST ast = cu.getAST();
            // make a wrapper inside this new AST
            /// TODO wrapper
            PackageDeclaration pd = ast.newPackageDeclaration();
            Name name = ast.newName("prdel.prdelka");
            pd.setName(name);
            cu.setPackage(pd);
            ///...
            // rewrite changes to the document
            cu.rewrite(document, null);
            TextEdit edits = cu.rewrite(document, null);
            edits.apply(document);
            // save the document as a new wrapper
            /// TODO save file
            ///...
        }
        catch (BadLocationException e) {
            Logger.error(e.getMessage());
            String declName = NameResolver.getFullName(declars.get(0));
            throw new WrapperMakerException("internal error while generating wrapper " +
                                            "of the " + declName + " class");
        }
    }
}
