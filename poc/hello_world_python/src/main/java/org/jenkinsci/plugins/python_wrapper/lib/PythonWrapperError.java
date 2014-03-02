package org.jenkinsci.plugins.python_wrapper.lib;

/**
 * Represents some kind of python-wrapper runtime error.
 */
public class PythonWrapperError extends Error
{
    public PythonWrapperError(String message) {
        super(message);
    }
}
