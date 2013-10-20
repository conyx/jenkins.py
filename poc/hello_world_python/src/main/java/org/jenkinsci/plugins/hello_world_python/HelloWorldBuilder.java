package org.jenkinsci.plugins.hello_world_python;
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

public class HelloWorldBuilder extends Builder {

    private final String name;

    @DataBoundConstructor
    public HelloWorldBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        File class_folder = new File(HelloWorldBuilder.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        // TODO exctract package name to path
        File delegate_script = new File(class_folder, "org");
        delegate_script = new File(delegate_script, "jenkinsci");
        delegate_script = new File(delegate_script, "plugins");
        delegate_script = new File(delegate_script, "hello_world_python");
        delegate_script = new File(delegate_script, "hello_world_builder.py");
        PythonInterpreter interp = new PythonInterpreter();
        interp.execfile(delegate_script.getPath());
        interp.set("_HelloWorldBuilder_perform_this", this);
        interp.set("_HelloWorldBuilder_perform_build", build);
        interp.set("_HelloWorldBuilder_perform_launcher", launcher);
        interp.set("_HelloWorldBuilder_perform_listener", listener);
        PyObject obj = interp.eval("perform(_HelloWorldBuilder_perform_this, " +
                                           "_HelloWorldBuilder_perform_build, " +
                                           "_HelloWorldBuilder_perform_launcher, " +
                                           "_HelloWorldBuilder_perform_listener)");
        boolean result = ((PyInteger)obj).asInt() == 0 ? false : true;
        return result;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }


    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private boolean useFrench;

        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Say hello world";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            useFrench = formData.getBoolean("useFrench");
            save();
            return super.configure(req,formData);
        }

        public boolean getUseFrench() {
            return useFrench;
        }
    }
}

