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

import org.python.util.PythonInterpreter;
import org.python.core.*;

import org.jenkinsci.plugins.python_wrapper.lib.*;

public class BuildStepDescriptorPW<T extends BuildStep & Describable<T>> extends BuildStepDescriptor<T> {
    
    private transient PythonInterpreter pinterp;
    private transient PythonExecutor pexec;
    
    private void initPython() {
        if (pinterp == null) {
            /// TODO unpack JAR
            /// TODO generic class folder path lookup
            File class_folder = new File(BuildStepDescriptorPW.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            /// TODO generic python script path lookup
            File delegate_script = new File(class_folder, "org");
            delegate_script = new File(delegate_script, "jenkinsci");
            delegate_script = new File(delegate_script, "plugins");
            delegate_script = new File(delegate_script, "hello_world_python");
            delegate_script = new File(delegate_script, "descriptor_impl.py");
            ///
            pinterp = new PythonInterpreter();
            pinterp.execfile(delegate_script.getPath());
            pexec = new PythonExecutor(pinterp);
        }
    }
    
    @Override
    public String getDisplayName() {
        initPython();
        return (String)pexec.doPython(String.class, "get_display_name", this);
    }
    
    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        initPython();
        return pexec.doPythonBool("is_applicable", this, aClass);
    }
    
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        initPython();
        pinterp.set("_BuildStepDescriptorPW_configure_this", this);
        pinterp.set("_BuildStepDescriptorPW_configure_req", req);
        pinterp.set("_BuildStepDescriptorPW_configure_formData", formData);
        PyObject obj = pinterp.eval("configure(_BuildStepDescriptorPW_configure_this, " +
                                              "_BuildStepDescriptorPW_configure_req, " +
                                              "_BuildStepDescriptorPW_configure_formData)");
        return (((PyInteger)obj).asInt() == 0 ? false : true);
    }
    
    public boolean configureSuper(StaplerRequest req, JSONObject formData) throws FormException {
        return super.configure(req, formData);
    }
    
}
