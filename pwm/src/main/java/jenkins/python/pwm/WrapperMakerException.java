package jenkins.python.pwm;

/**
 * Indicates some generic exception during application run.
 */
public abstract class WrapperMakerException extends Exception {
    
    public WrapperMakerException(String message) {
        super(message);
    }
}
