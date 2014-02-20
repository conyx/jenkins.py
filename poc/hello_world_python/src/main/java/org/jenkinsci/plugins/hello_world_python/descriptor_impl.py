#!/usr/bin/env jython

def configure(wrapper, request, formData):
    wrapper.setUseFrench(formData.getBoolean("useFrench"))
    wrapper.save();
    return wrapper.configureSuper(request, formData)

def is_applicable(wrapper, _class):
    return True
    
def get_display_name(wrapper):
    return "Say hello world by Jython"
