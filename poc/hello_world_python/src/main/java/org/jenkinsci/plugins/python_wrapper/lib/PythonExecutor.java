package org.jenkinsci.plugins.python_wrapper.lib;

import java.io.File;

import org.python.util.PythonInterpreter;
import org.python.core.*;

/**
 * Executes functions inside Jython interpreter
 */
public class PythonExecutor {
    
    private PythonInterpreter pinterp;
    // callId guarantees unique attributes' names for every function call inside Jython interpreter
    private int callId;
    private boolean[] funcFlags;
    
    public PythonExecutor(Object javaWrapper) {
        String scriptPath = getScriptPath(javaWrapper);
        pinterp = new PythonInterpreter();
        pinterp.execfile(scriptPath);
        pinterp.set("wrapper", javaWrapper);
        callId = 0;
    }
    
    /**
     * Determines if a function with the specified id is implemented in the Python script.
     */
    public boolean isImplemented(int id) {
        return funcFlags[id];
    }
    
    /**
     * Search for all functions from the given array in the Python script and register
     * availability flags for them.
     */
    public void registerFunctions(String[] functions, int[] argsCount) {
        // init funcFlags with false values
        funcFlags = new boolean[functions.length];
        PyStringMap locals = (PyStringMap)pinterp.getLocals();
        for (int i = 0; i < functions.length; i++) {
            if (locals.has_key(functions[i])) {
                // great, there is some variable with this name
                PyObject obj = locals.get(new PyString(functions[i]));
                if (obj.getClass() == PyFunction.class) {
                    // great, it's a function
                    PyFunction fnc = (PyFunction)obj;
                    int aCount = ((PyTableCode)fnc.func_code).co_argcount;
                    if (aCount == argsCount[i]) {
                        // great, it accepts correct number of arguments
                        funcFlags[i] = true;
                    }
                }
            }
        }
    }
    
    /**
     * Finds a correct file path of a python implementation for this wrapper.
     */
    private String getScriptPath(Object javaWrapper) {
        String className = javaWrapper.getClass().getName();
        File scriptFile;
        File classFolder = new File(javaWrapper.getClass().getProtectionDomain()
                                               .getCodeSource().getLocation().getPath());
        if (classFolder.getPath().endsWith(".jar")) {
            // normal mode (plugin was properly installed)
            JARUnpacker.unpackPythonFiles(classFolder);
            scriptFile = new File(classFolder.getParentFile(), "python");
        }
        else {
            // "mvn hpi:run" mode (plugin debug)
            scriptFile = new File(classFolder, "python");
        }
        scriptFile = new File(scriptFile, NameConvertor.javaClassToPythonFile(className));
        return scriptFile.getPath();
    }
    
    /**
     * Call the function inside Jython interpreter and return PyObject
     */
    private PyObject execPythonGeneric(String function, Object ... params) {
        // prepare function call string
        String paramName;
        String execStr = function + "(";
        for (int i = 0; i < params.length; i++) {
            paramName = "_" + function + "_" + (new Integer(callId)).toString() + "_" + (new Integer(i)).toString();
            pinterp.set(paramName, params[i]);
            execStr += paramName;
            if (i < params.length-1) {
                execStr += ", ";
            }
        }
        execStr += ")";
        // call function inside Jython interpreter
        PyObject obj = pinterp.eval(execStr);
        // delete params from Jython interpreter namespace
        for (int i = 0; i < params.length; i++) {
            paramName = "_" + function + "_" + (new Integer(callId)).toString() + "_" + (new Integer(i)).toString();
            pinterp.exec("del " + paramName);
        }
        // increase call id for the next use
        callId++;
        if (callId < 0) {
            callId = 0;
        }
        return obj;
    }
    
    /**
     * Call the function inside Jython interpreter and return Java Object
     */
    public Object execPython(Class<?> resultClass, String function, Object ... params) {
        PyObject obj = execPythonGeneric(function, params);
        return DataConvertor.toObject(obj, resultClass);
    }
    
    /**
     * Call the function inside Jython interpreter
     */
    public void execPythonVoid(String function, Object ... params) {
        execPythonGeneric(function, params);
    }
    
    /**
     * Call the function inside Jython interpreter and return bool value
     */
    public boolean execPythonBool(String function, Object ... params) {
        PyObject obj = execPythonGeneric(function, params);
        return DataConvertor.toBool(obj);
    }
    
    /**
     * Call the function inside Jython interpreter and return double value
     */
    public double execPythonDouble(String function, Object ... params) {
        PyObject obj = execPythonGeneric(function, params);
        return DataConvertor.toDouble(obj);
    }
    
    /**
     * Call the function inside Jython interpreter and return float value
     */
    public float execPythonFloat(String function, Object ... params) {
        PyObject obj = execPythonGeneric(function, params);
        return DataConvertor.toFloat(obj);
    }
    
    /**
     * Call the function inside Jython interpreter and return long value
     */
    public long execPythonLong(String function, Object ... params) {
        PyObject obj = execPythonGeneric(function, params);
        return DataConvertor.toLong(obj);
    }
    
    /**
     * Call the function inside Jython interpreter and return integer value
     */
    public int execPythonInt(String function, Object ... params) {
        PyObject obj = execPythonGeneric(function, params);
        return DataConvertor.toInt(obj);
    }
    
    /**
     * Call the function inside Jython interpreter and return short value
     */
    public short execPythonShort(String function, Object ... params) {
        PyObject obj = execPythonGeneric(function, params);
        return DataConvertor.toShort(obj);
    }
    
    /**
     * Call the function inside Jython interpreter and return byte value
     */
    public byte execPythonByte(String function, Object ... params) {
        PyObject obj = execPythonGeneric(function, params);
        return DataConvertor.toByte(obj);
    }
    
    /**
     * Call the function inside Jython interpreter and return char value
     */
    public char execPythonChar(String function, Object ... params) {
        PyObject obj = execPythonGeneric(function, params);
        return DataConvertor.toChar(obj);
    }
}
