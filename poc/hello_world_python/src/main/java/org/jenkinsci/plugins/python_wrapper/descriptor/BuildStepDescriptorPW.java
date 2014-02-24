package org.jenkinsci.plugins.python_wrapper.descriptor;

import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import hudson.tasks.BuildStep;
import hudson.model.Describable;
import hudson.model.Descriptor;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.File;

import org.jenkinsci.plugins.python_wrapper.lib.*;

public class BuildStepDescriptorPW<T extends BuildStep & Describable<T>> extends BuildStepDescriptor<T> {
    
    private transient PythonExecutor pexec;
    
    // init method
    private void initPython() {
        if (pexec == null) {
            pexec = new PythonExecutor(this);
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
        return pexec.execPythonBool("configure", req, formData);
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
