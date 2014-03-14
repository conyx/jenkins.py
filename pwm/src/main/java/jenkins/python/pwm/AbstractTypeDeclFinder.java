package jenkins.python.pwm;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Modifier;

public abstract class AbstractTypeDeclFinder {
    
    private final String CHARSET = "UTF-8";
    private final File sourceCodeDir;
    private List<List<TypeDeclaration>> wantedTypes;
    
    public AbstractTypeDeclFinder() {
        sourceCodeDir = Application.getSrcDir();
    }
    
    /**
     * Returns a list of lists of wanted type declarations and all parent type declarations.
     */
    public List<List<TypeDeclaration>> getAllDeclarations() throws JavaParserException {
        wantedTypes = new LinkedList<List<TypeDeclaration>>();
        searchDir(sourceCodeDir);
        return wantedTypes;
    }
    
    /**
     * Recursively search in the given directory for wanted type declarations.
     */
    private void searchDir(File dir) throws JavaParserException {
        File[] files = dir.listFiles();
        if (files == null) {
            String errStr = "error while scanning directory " + dir.getPath() + " occured";
            throw new JavaParserException(errStr);
        }
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                searchDir(f);
            }
            else if (f.getName().endsWith(".java")) {
                Logger.verbose("parsing file " + f.getPath());
                searchInFile(f);
            }
            else {
                Logger.verbose("file " + f.getName() + " ignored");
            }
        }
    }
    
    /**
     * Returns the file as a char array.
     */
    private char[] readFile(File f) throws IOException
    {
        Charset charset = Charset.forName(CHARSET);
        byte[] encoded = Files.readAllBytes(Paths.get(f.getPath()));
        return charset.decode(ByteBuffer.wrap(encoded)).toString().toCharArray();
    }
    
    /**
     * Parses a given file and returns a CompilationUnit object as a root node.
     */
    private CompilationUnit parseFile(File f) throws JavaParserException {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        try {
            parser.setSource(readFile(f));
        }
        catch (IOException e) {
            String errStr = "cannot parse file " + f.getPath() + " caused by " + e.getMessage();
            throw new JavaParserException(errStr);
        }
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit cu = (CompilationUnit)parser.createAST(null);
        return cu;
    }
    
    /**
     * Recursively search for the parent TypeDeclarations and adds them to the list.
     */
    private void searchForParents(List<TypeDeclaration> list) throws JavaParserException {
        TypeDeclaration lastDecl = list.get(list.size()-1);
        Type superClass = lastDecl.getSuperclassType();
        if (superClass == null) {
            // the last type declaration inherits from the Object class
            return;
        }
        String fullName = NameResolver.getFullName(superClass);
        if (!fullName.startsWith("jenkins") && !fullName.startsWith("hudson")) {
            // the last type declaration inherits from the java.* or 3rd party class
            Logger.verbose("skipping parent class: " + fullName);
            return;
        }
        File path = PathResolver.getPath(fullName);
        String[] nameParts = fullName.split("\\.");
        String shortName = nameParts[nameParts.length-1];
        CompilationUnit cu = parseFile(path);
        ParentTypeVisitor visitor = new ParentTypeVisitor(shortName);
        cu.accept(visitor);
        if (visitor.getFound()) {
            TypeDeclaration parent = visitor.getTypeDecl();
            Logger.verbose("parent: " + NameResolver.getFullName(parent));
            list.add(parent);
            searchForParents(list);
        }
        else {
            Logger.error("cannot find a type declaration: " + fullName);
        }
    }
    
    /**
     * Search in the given file for the wanted type declaration.
     */
    private void searchInFile(File f) throws JavaParserException {
        CompilationUnit cu = parseFile(f);
        WantedTypeVisitor visitor = new WantedTypeVisitor();
        cu.accept(visitor);
        if (visitor.getFound()) {
            String typeName = NameResolver.getFullName(visitor.getTypeDecl());
            Logger.verbose("wanted type declaration " + typeName + " found");
            List<TypeDeclaration> list = new LinkedList<TypeDeclaration>();
            // add the found type declaration to the list
            list.add(visitor.getTypeDecl());
            searchForParents(list);
            Logger.verbose("number of parent classes: " + new Integer(list.size()-1));
            // add the list (of the type declaration and its parents) to the wanted types list
            wantedTypes.add(list);
        }
    }
    
    protected boolean isAbstract(TypeDeclaration td) {
        if (Modifier.isAbstract(td.getModifiers())) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Determines if a type declaration is wanted by a concrete finder.
     */
    protected abstract boolean isWanted(TypeDeclaration typeDecl);
    
    
    /**
     * Visits all TypeDeclaration nodes and checks if they are wanted.
     * Only the last wanted node in the tree is found!!
     */
    private class WantedTypeVisitor extends ASTVisitor {
        private boolean found = false;
        private TypeDeclaration typeDecl;
        
        public boolean visit(TypeDeclaration node) {
            if (isWanted(node)) {
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
    
    /**
     * Visits all TypeDeclaration nodes and checks if the name equals.
     */
    private class ParentTypeVisitor extends ASTVisitor {
        private boolean found = false;
        private TypeDeclaration typeDecl;
        private String name;
        
        ParentTypeVisitor(String aName) {
            super();
            name = aName;
        }
        
        public boolean visit(TypeDeclaration node) {
            if (node.getName().getFullyQualifiedName().equals(name)) {
                found = true;
                typeDecl = node;
                return false;
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
