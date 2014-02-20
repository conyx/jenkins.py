package org.jenkinsci.plugins.python_wrapper.lib;

import org.python.core.*;

public class DataConvertor {
    
    public static Object toObject(PyObject obj, Class<?> resultClass) {
        return obj.__tojava__(resultClass);
    }
    
    public static boolean toBool(PyObject obj) {
        return (obj.asInt() == 0 ? false : true);
    }
}
