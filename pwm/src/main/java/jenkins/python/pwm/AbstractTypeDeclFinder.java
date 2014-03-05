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

public abstract class AbstractTypeDeclFinder {
    
    private final String CHARSET = "UTF-8";
    private final File sourceCodeDir;
    private List<List<TypeDeclaration>> wantedTypes;
    
    public AbstractTypeDeclFinder(File sourceCodeDir_) {
        sourceCodeDir = sourceCodeDir_;
    }
    
    /**
     * Returns a list of lists of wanted type declarations and also all its parent
     * declarations.
     */
    public List<List<TypeDeclaration>> getAllDeclarations() {
        wantedTypes = new LinkedList<List<TypeDeclaration>>();
        searchDir(sourceCodeDir);
        return wantedTypes;
    }
    
    /**
     * Recursively search in the given directory for wanted type declarations.
     */
    private void searchDir(File dir) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                searchDir(f);
            }
            else if (f.getName().endsWith(".java")) {
                parseFile(f);
            }
            else {
                Logger.warning("unknown file " + f.getName());
            }
        }
    }
    
    char[] readFile(File f) throws IOException
    {
        Charset charset = Charset.forName(CHARSET);
        byte[] encoded = Files.readAllBytes(Paths.get(f.getPath()));
        return charset.decode(ByteBuffer.wrap(encoded)).toString().toCharArray();
    }
    
    /**
     * Search in the given file for wanted type declarations.
     */
    private void parseFile(File f) {
        // TODO
    }
    
    protected abstract boolean isWanted(TypeDeclaration typeDecl);
}
