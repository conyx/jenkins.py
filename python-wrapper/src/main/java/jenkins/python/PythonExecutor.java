package jenkins.python;

import java.io.File;
import java.lang.reflect.Method;

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
    private Object extension;
    
    public PythonExecutor(Object _extension) throws PythonWrapperError {
        String scriptPath = getScriptPath(_extension);
        pinterp = new PythonInterpreter();
        pinterp.execfile(scriptPath);
        pinterp.set("extension", _extension);
        callId = 0;
        extension = _extension;
        if (hasFunction("init_plugin", 0)) {
            pinterp.exec("init_plugin()");
        }
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
        for (int i = 0; i < functions.length; i++) {
            if (hasFunction(functions[i], argsCount[i])) {
                // a function exists, mark it with the true flag
                funcFlags[i] = true;
            }
        }
    }
    
    /**
     * Check if all abstract methods are implemented either in Java class or in Python script
     * @throws PythonWrapperError if any of methods has no implementation.
     */
    public void checkAbstrMethods(String[] jMethods, String[] pFuncs, Class<?>[][] argTypes) throws PythonWrapperError {
        Method[] methods = extension.getClass().getDeclaredMethods();
        for (int i = 0; i < jMethods.length; i++) {
            boolean found = false;
            if (hasFunction(pFuncs[i], argTypes[i].length)) {
                // great, python implementation is presented
                found = true;
            }
            else {
                for (int j = 0; j < methods.length; j++) {
                    Method m = methods[j];
                    if (m.getName().equals(jMethods[i])) {
                        Class<?>[] paramTypes = m.getParameterTypes();
                        if (paramTypes.length != argTypes[i].length) {
                            continue;
                        }
                        if (paramTypes.length == 0) {
                            // this java method override the abstract method
                            found = true;
                            break;
                        }
                        for (int k = 0; k < paramTypes.length; k++) {
                            if (argTypes[i][k] == null) {
                                // this is for parametric types, do nothing
                            }
                            else if (paramTypes[k] != argTypes[i][k]) {
                                break;
                            }
                            if (k == paramTypes.length - 1) {
                                // this java method override the abstract method
                                found = true;
                            }
                        }
                    }
                }
            }
            if (!found) {
                // abstract method is not implemented, let's inform a developer!
                throw new PythonWrapperError("The abstract method '" + jMethods[i] + "' of this extension " +
                                             "has to be implemented in either " +
                                             "Python (" + pFuncs[i] + "(" + new Integer(argTypes[i].length) + "))" +
                                             " or " +
                                             "Java (" + jMethods[i] + "(" + new Integer(argTypes[i].length) + ")).");
            }
        }
        // great, all methods are implemented!
    }
    
    /**
     * Determines if there is a function with the given name and the correct number of arguments
     * in the loaded script.
     */
    private boolean hasFunction(String name, int argsCount) {
        PyStringMap locals = (PyStringMap)pinterp.getLocals();
        if (locals.has_key(name)) {
            // great, there is some variable with this name
            PyObject obj = locals.get(new PyString(name));
            if (obj.getClass() == PyFunction.class) {
                // great, it's a function
                PyFunction fnc = (PyFunction)obj;
                int aCount = ((PyTableCode)fnc.func_code).co_argcount;
                if (aCount == argsCount) {
                    // great, it accepts correct number of arguments
                    return true;
                }
                boolean varargs = ((PyTableCode)fnc.func_code).varargs;
                if (aCount < argsCount && varargs) {
                    // great, it is variable arguments function
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Finds a correct file path of a python implementation for this object.
     */
    private String getScriptPath(Object obj) throws PythonWrapperError {
        String className = obj.getClass().getName();
        File scriptFile;
        File classFolder = new File(obj.getClass().getProtectionDomain()
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
    public Object execPython(String function, Object ... params) {
        PyObject obj = execPythonGeneric(function, params);
        return DataConvertor.toObject(obj, Object.class);
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
