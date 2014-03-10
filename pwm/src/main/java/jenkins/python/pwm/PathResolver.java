package jenkins.python.pwm;

import java.io.File;

/**
 * Evaluates file paths of the fully qualified names.
 */
public class PathResolver {
    
    public static File getPath(String name) {
        String[] parts = name.split("\\.");
        File path = Application.getSrcDir();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("")) {
                break;
            }
            else if (Character.isUpperCase(parts[i].charAt(0))) {
                path = new File(path, parts[i] + ".java");
                break;
            }
            else {
                path = new File(path, parts[i]);
            }
        }
        return path;
    }
}
