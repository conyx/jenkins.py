#!/usr/bin/env jython

def perform(build, launcher, listener):
    if wrapper.getDescriptor().getUseFrench():
        listener.getLogger().println("Bonjour, " + wrapper.getName() + "!")
    else:
        listener.getLogger().println("Hello, " + wrapper.getName() + "!")
    return True
