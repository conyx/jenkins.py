#!/usr/bin/env python3
"""
Python Plugin Skeleton Maker 1.0

This tool creates a source code skeleton of a simple plugin for Jenkins.
"""

import shutil
import os
import os.path

name_variants = dict()

def fold(f, l, a):
    """
    f: the function to apply
    l: the list to fold
    a: the accumulator, who is also the 'zero' on the first call
    """ 
    return a if(len(l) == 0) else fold(f, l[1:], f(a, l[0]))

def fold1(f, l):
    """
    f: the function to apply
    l: the list to fold
    """ 
    return fold(f, l[1:], l[0])

def modify_names():
    for dirpath, dirnames, filenames in os.walk(name_variants["__git__"]):
        for fn in filenames:
            filename = os.path.join(dirpath, fn)
            f = open(filename, mode='r+b')
            content = f.read()
            f.close()
            for key in name_variants.keys():
                content = content.replace(bytes(key, "u8"),
                                          bytes(name_variants[key], "u8"))
            f = open(filename, mode='w+b')
            f.write(content)
            f.close()

def modify_filenames():
    for dirpath, dirnames, filenames in os.walk(name_variants["__git__"],
                                                topdown=False):
        for fn in filenames + dirnames:
            for key in name_variants.keys():
                if key in fn:
                    new_fn = fn.replace(key, name_variants[key])
                    old_filename = os.path.join(dirpath, fn)
                    new_filename = os.path.join(dirpath, new_fn)
                    os.rename(old_filename, new_filename)

def generate_name_variants(name_parts):
    # My+New+Plugin
    name_variants["__wiki__"] = fold(
        lambda x, y : x + "+" + y.capitalize(),
        name_parts[1:] + ["plugin"],
        name_parts[0].capitalize())
    # my-new
    name_variants["__artifactid__"] = fold1(
        lambda x, y : x + "-" + y,
        name_parts)
    # My New Plugin
    name_variants["__name__"] = fold(
        lambda x, y : x + " " + y.capitalize(),
        name_parts[1:] + ["plugin"],
        name_parts[0].capitalize())
    # My New
    name_variants["__nameshort__"] = fold(
        lambda x, y : x + " " + y.capitalize(),
        name_parts[1:],
        name_parts[0].capitalize())
    # my-new-plugin
    name_variants["__git__"] = fold1(
        lambda x, y : x + "-" + y,
        name_parts  + ["plugin"])
    # MyNew
    name_variants["__class__"] = fold(
        lambda x, y : x + y.capitalize(),
        name_parts[1:],
        name_parts[0].capitalize())
    # my_new
    name_variants["__pythonfile__"] = fold1(
        lambda x, y : x + "_" + y,
        name_parts)

def main():
    inserted_name = input("Insert the whitespace-separated name of " +
                          "a new plugin (without Plugin at the end): ")
    name_parts = inserted_name.split()
    name_parts = list(map(lambda x: x.lower(), name_parts))
    if len(name_parts) == 0:
        print("Incorrect name!")
        return 1
    generate_name_variants(name_parts)
    # copy template to the current working directory
    src_dir = os.path.join(os.path.dirname(os.path.realpath(__file__)),
                           "template")
    dst_dir = name_variants["__git__"]
    shutil.rmtree(dst_dir, ignore_errors=True)
    shutil.copytree(src_dir, dst_dir)
    # modify names in template files
    modify_names()
    # modify file names
    modify_filenames()
    print("Directory " + dst_dir + " created.")
    return 0

if __name__ == "__main__":
    main()
