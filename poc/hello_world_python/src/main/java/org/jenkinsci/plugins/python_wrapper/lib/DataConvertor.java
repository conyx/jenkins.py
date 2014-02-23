package org.jenkinsci.plugins.python_wrapper.lib;

import org.python.core.*;

public class DataConvertor {
    
    // methods converts PyObject to java types
    public static Object toObject(PyObject obj, Class<?> resultClass) {
        return obj.__tojava__(resultClass);
    }
    
    public static boolean toBool(PyObject obj) {
        return (obj.asInt() == 0 ? false : true);
    }
    
    public static double toDouble(PyObject obj) {
        return obj.asDouble();
    }
    
    public static float toFloat(PyObject obj) {
        return (float)obj.asDouble();
    }
    
    public static long toLong(PyObject obj) {
        return obj.asLong();
    }
    
    public static int toInt(PyObject obj) {
        return obj.asInt();
    }
    
    public static short toShort(PyObject obj) {
        return (short)obj.asInt();
    }
    
    public static byte toByte(PyObject obj) {
        return (byte)obj.asInt();
    }
    
    public static char toChar(PyObject obj) {
        return ((String)obj.__tojava__(String.class)).charAt(0);
    }
}
