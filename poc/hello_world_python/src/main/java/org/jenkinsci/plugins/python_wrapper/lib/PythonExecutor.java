package org.jenkinsci.plugins.python_wrapper.lib;

import org.python.util.PythonInterpreter;
import org.python.core.*;

/*
 * Executes functions inside Jython interpreter
 */
public class PythonExecutor {
    
    private PythonInterpreter pinterp;
    private int callId;
    
    public PythonExecutor(PythonInterpreter _pinterp) {
        pinterp = _pinterp;
        // callId guarantee unique attributes names for every function call inside Jython interpreter
        callId = 0;
    }
    
    /*
     * Call the function inside Jython interpreter and return PyObject
     */
    private PyObject doPythonGeneral(String function, Object ... params) {
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
    public Object doPython(Class<?> resultClass, String function, Object ... params) {
        PyObject obj = doPythonGeneral(function, params);
        return DataConvertor.toObject(obj, resultClass);
    }
    
    /*
     * Call the function inside Jython interpreter
     */
    public void doPythonVoid(String function, Object ... params) {
        doPythonGeneral(function, params);
    }
    
    /*
     * Call the function inside Jython interpreter and return bool value
     */
    public boolean doPythonBool(String function, Object ... params) {
        PyObject obj = doPythonGeneral(function, params);
        return DataConvertor.toBool(obj);
    }
}
