package org.jenkinsci.plugins.python_wrapper.expoint;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import java.lang.InterruptedException;
import java.io.IOException;

import org.jenkinsci.plugins.python_wrapper.lib.*;

public class BuilderPW extends Builder {
    
    private transient PythonExecutor pexec;
    
    // init method
    public void initPython() {
        if (pexec == null) {
            pexec = new PythonExecutor(this);
            // check abstract methods implementation
            String[] jMethods = {};
            String[] pFuncs = {};
            Class<?>[][] argTypes = new Class<?>[0][];
            pexec.checkAbstrMethods(jMethods, pFuncs, argTypes);
            // find and register functions in python script
            String[] functions = {"perform"};
            int[] argsCount = {3};
            pexec.registerFunctions(functions, argsCount);
        }
    }
    
    // wrapper methods
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        initPython();
        if (pexec.isImplemented(0)) {
            return pexec.execPythonBool("perform", build, launcher, listener);
        }
        else {
            return super.perform(build, launcher, listener);
        }
    }
    // ...
    
    // super methods
    public boolean performSuper(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return super.perform(build, launcher, listener);
    }
    //...
    
    // exec python methods
    //...
}
