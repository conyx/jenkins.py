package org.jenkinsci.plugins.python_wrapper.lib;

import java.io.File;

import org.python.util.PythonInterpreter;
import org.python.core.*;

import org.jenkinsci.plugins.hello_world_python.HelloWorldBuilder;///

/*
 * Executes functions inside Jython interpreter
 */
public class PythonExecutor {
    
    private PythonInterpreter pinterp;
    // callId guarantee unique attributes names for every function call inside Jython interpreter
    private int callId;
    
    public PythonExecutor(Object javaWrapper) {
        /// TODO unpack JAR static
        /// ...
        String scriptPath = getScriptPath(javaWrapper);
        pinterp = new PythonInterpreter();
        pinterp.execfile(scriptPath);
        pinterp.set("wrapper", javaWrapper);
        callId = 0;
    }
    
    /*
     * Finds a correct file path of a python implementation for this wrapper
     */
    private String getScriptPath(Object javaWrapper) {
        File class_folder = new File(javaWrapper.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        /// TODO generic python script path lookup
        File delegate_script = new File(class_folder, "python");
        if (javaWrapper.getClass() == HelloWorldBuilder.DescriptorImpl.class) {
            delegate_script = new File(delegate_script, "descriptor_impl.py");
        }
        else if (javaWrapper.getClass() == HelloWorldBuilder.class) {
            delegate_script = new File(delegate_script, "hello_world_builder.py");
        }
        return delegate_script.getPath();
        ///
    }
    
    /*
     * Call the function inside Jython interpreter and return PyObject
     */
    private PyObject execPythonGeneral(String function, Object ... params) {
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
    
    /*
     * Call the function inside Jython interpreter and return Java Object
     */
    public Object execPython(Class<?> resultClass, String function, Object ... params) {
        PyObject obj = execPythonGeneral(function, params);
        return DataConvertor.toObject(obj, resultClass);
    }
    
    /*
     * Call the function inside Jython interpreter
     */
    public void execPythonVoid(String function, Object ... params) {
        execPythonGeneral(function, params);
    }
    
    /*
     * Call the function inside Jython interpreter and return bool value
     */
    public boolean execPythonBool(String function, Object ... params) {
        PyObject obj = execPythonGeneral(function, params);
        return DataConvertor.toBool(obj);
    }
    
    /*
     * Call the function inside Jython interpreter and return double value
     */
    public double execPythonDouble(String function, Object ... params) {
        PyObject obj = execPythonGeneral(function, params);
        return DataConvertor.toDouble(obj);
    }
    
    /*
     * Call the function inside Jython interpreter and return float value
     */
    public float execPythonFloat(String function, Object ... params) {
        PyObject obj = execPythonGeneral(function, params);
        return DataConvertor.toFloat(obj);
    }
    
    /*
     * Call the function inside Jython interpreter and return long value
     */
    public long execPythonLong(String function, Object ... params) {
        PyObject obj = execPythonGeneral(function, params);
        return DataConvertor.toLong(obj);
    }
    
    /*
     * Call the function inside Jython interpreter and return integer value
     */
    public int execPythonInt(String function, Object ... params) {
        PyObject obj = execPythonGeneral(function, params);
        return DataConvertor.toInt(obj);
    }
    
    /*
     * Call the function inside Jython interpreter and return short value
     */
    public short execPythonShort(String function, Object ... params) {
        PyObject obj = execPythonGeneral(function, params);
        return DataConvertor.toShort(obj);
    }
    
    /*
     * Call the function inside Jython interpreter and return byte value
     */
    public byte execPythonByte(String function, Object ... params) {
        PyObject obj = execPythonGeneral(function, params);
        return DataConvertor.toByte(obj);
    }
    
    /*
     * Call the function inside Jython interpreter and return char value
     */
    public char execPythonChar(String function, Object ... params) {
        PyObject obj = execPythonGeneral(function, params);
        return DataConvertor.toChar(obj);
    }
}
