package org.jenkinsci.plugins.python_wrapper.expoint;

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

import org.jenkinsci.plugins.python_wrapper.lib.*;

public class BuilderPW extends Builder {
    
    private transient PythonExecutor pexec;
    
    // init method
    private void initPython() {
        if (pexec == null) {
            pexec = new PythonExecutor(this);
        }
    }
    
    // wrapper methods
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        initPython();
        return pexec.execPythonBool("perform", build, launcher, listener);
    }
    // ...
}
