package jenkins.python;

public class NameConvertor {
    
    /**
     * Converts a java class name to a python file name.
     * E.g.: some.package.SomeClass -> some_class.py
     *       some.package.SomeClass$SomeInnerClass -> some_inner_class.py
     */
    public static String javaClassToPythonFile(String name) {
        String pythonFileName = "";
        String[] splittedName = name.split("(\\$|\\.)");
        String className = splittedName[splittedName.length-1];
        int bigLettersCount = 0;
        for (int i = 0; i < className.length(); i++) {
            char ch = className.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (bigLettersCount == 0 && i != 0) {
                    pythonFileName += "_";
                }
                pythonFileName += Character.toLowerCase(ch);
                bigLettersCount += 1;
            }
            else {
                if (bigLettersCount > 1) {
                    pythonFileName = pythonFileName.substring(0, pythonFileName.length()-1) + "_" +
                    pythonFileName.charAt(pythonFileName.length()-1);
                }
                pythonFileName += ch;
                bigLettersCount = 0;
            }
        }
        pythonFileName += ".py";
        return pythonFileName;
    }
}
