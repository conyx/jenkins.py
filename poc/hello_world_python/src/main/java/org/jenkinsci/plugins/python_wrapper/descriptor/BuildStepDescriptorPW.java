package org.jenkinsci.plugins.python_wrapper.descriptor;

import hudson.model.AbstractProject;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import hudson.model.Describable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStep;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.File;

import org.jenkinsci.plugins.python_wrapper.lib.*;

public class BuildStepDescriptorPW<T extends BuildStep & Describable<T>> extends BuildStepDescriptor<T> {
    
    private transient PythonExecutor pexec;
    
    // init method
    public void initPython() {
        if (pexec == null) {
            pexec = new PythonExecutor(this);
            String[] functions = {"get_display_name",
                                  "is_applicable",
                                  "configure"};
            int[] argsCount = {0,
                               1,
                               2};
            pexec.registerFunctions(functions, argsCount);
        }
    }
    
    // wrapper methods
    @Override
    public String getDisplayName() {
        initPython();
        return (String)pexec.execPython(String.class, "get_display_name");
    }
    
    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        initPython();
        return pexec.execPythonBool("is_applicable", aClass);
    }
    
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        initPython();
        if (pexec.isImplemented(2)) {
            return pexec.execPythonBool("configure", req, formData);
        }
        else {
            return super.configure(req, formData);
        }
    }
    //...
    
    // super methods
    public boolean configureSuper(StaplerRequest req, JSONObject formData) throws FormException {
        return super.configure(req, formData);
    }
    //...
    
    // exec python methods
    public Object execPython(Class<?> resultClass, String function, Object ... params) {
        return pexec.execPython(resultClass, function, params);
    }
    //...
}
