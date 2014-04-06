#!/usr/bin/env jython

import hudson.util.FormValidation as FormValidation

def descriptor_impl():
    extension.load();

def configure(request, form_data):
    extension.french = form_data.getBoolean("french")
    extension.save();
    return extension.superConfigure(request, form_data)

def is_applicable(_class):
    return True
    
def get_display_name():
    return "Execute __nameshort__ builder"

def do_check_name(value):
    if len(value) == 0:
        return FormValidation.error("Please set a name")
    if len(value) < 4:
        return FormValidation.warning("Isn't the name too short?")
    return FormValidation.ok()
