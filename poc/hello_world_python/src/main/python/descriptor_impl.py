#!/usr/bin/env jython

import hudson.util.FormValidation as FormValidation

def configure(request, formData):
    wrapper.setUseFrench(formData.getBoolean("useFrench"))
    wrapper.save();
    return wrapper.configureSuper(request, formData)

def is_applicable(_class):
    return True
    
def get_display_name():
    return "Say hello world by Jython"

def do_check_name(value):
    if len(value) == 0:
        return FormValidation.error("Please set a name")
    if len(value) < 4:
        return FormValidation.warning("Isn't the name too short?")
    return FormValidation.ok()
