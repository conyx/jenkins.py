package jenkins.python.pwm;

public class NameConvertor {
    
    /**
     * Converts a java method name to a python function name.
     * E.g.: someMethod -> some_method
     */
    public static String javaMethToPythonFunc(String name) {
        String pythonFuncName = "";
        int bigLettersCount = 0;
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (bigLettersCount == 0 && i != 0) {
                    pythonFuncName += "_";
                }
                pythonFuncName += Character.toLowerCase(ch);
                bigLettersCount += 1;
            }
            else {
                if (bigLettersCount > 1) {
                    pythonFuncName = pythonFuncName.substring(0, pythonFuncName.length()-1) + "_" +
                    pythonFuncName.charAt(pythonFuncName.length()-1);
                }
                pythonFuncName += ch;
                bigLettersCount = 0;
            }
        }
        return pythonFuncName;
    }
}
