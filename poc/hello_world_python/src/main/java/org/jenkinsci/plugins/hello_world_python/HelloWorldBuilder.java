package org.jenkinsci.plugins.hello_world_python;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.tasks.Builder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import org.jenkinsci.plugins.python_wrapper.expoint.*;
import org.jenkinsci.plugins.python_wrapper.descriptor.*;

public class HelloWorldBuilder extends BuilderPW {

    private final String name;

    @DataBoundConstructor
    public HelloWorldBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptorPW<Builder> {

        private boolean useFrench;

        public FormValidation doCheckName(@QueryParameter String value) {
            return (FormValidation)execPython(FormValidation.class, "do_check_name", value);
        }

        public boolean getUseFrench() {
            return useFrench;
        }
        
        public void setUseFrench(boolean french) {
            useFrench = french;
        }
    }
}

