package jenkins.python;

/**
 * Represents some kind of python-wrapper runtime error.
 */
public class PythonWrapperError extends Error
{
    public PythonWrapperError(String message) {
        super(message);
    }
}
