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

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.File;

import org.python.util.PythonInterpreter;
import org.python.core.*;

import org.jenkinsci.plugins.python_wrapper.lib.*;

public class BuilderPW extends Builder {
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        File class_folder = new File(BuilderPW.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File delegate_script = new File(class_folder, "org");
        delegate_script = new File(delegate_script, "jenkinsci");
        delegate_script = new File(delegate_script, "plugins");
        delegate_script = new File(delegate_script, "hello_world_python");
        delegate_script = new File(delegate_script, "hello_world_builder.py");
        PythonInterpreter interp = new PythonInterpreter();
        interp.execfile(delegate_script.getPath());
        interp.set("_BuilderPW_perform_this", this);
        interp.set("_BuilderPW_perform_build", build);
        interp.set("_BuilderPW_perform_launcher", launcher);
        interp.set("_BuilderPW_perform_listener", listener);
        PyObject obj = interp.eval("perform(_BuilderPW_perform_this, " +
                                           "_BuilderPW_perform_build, " +
                                           "_BuilderPW_perform_launcher, " +
                                           "_BuilderPW_perform_listener)");
        return (((PyInteger)obj).asInt() == 0 ? false : true);
    }
    
}
