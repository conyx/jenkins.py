package jenkins.python.pwm;

/**
 * Indicates some exception during parsing java files.
 */
public class JavaParserException extends WrapperMakerException {

    public JavaParserException(String message) {
        super(message);
    }
}
