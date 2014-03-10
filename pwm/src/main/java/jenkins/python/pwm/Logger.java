package jenkins.python.pwm;

public class Logger {
    
    private static boolean verbose = false;
    
    public static void info(String text) {
        System.out.println("[INFO] " + text);
    }
    
    public static void verbose(String text) {
        if (verbose) {
            System.out.println("[INFO] " + text);
        }
    }
    
    public static void warning(String text) {
        System.err.println("[WARNING] " + text);
    }
    
    public static void error(String text) {
        System.err.println("[ERROR] " + text);
    }
    
    public static void setVerbose(boolean flag) {
        verbose = flag;
    }
}
