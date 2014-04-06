#!/usr/bin/env jython

def perform(build, launcher, listener):
    if extension.getDescriptor().french:
        listener.getLogger().println("Bonjour, " + extension.name + "!")
    else:
        listener.getLogger().println("Hello, " + extension.name + "!")
    return True
