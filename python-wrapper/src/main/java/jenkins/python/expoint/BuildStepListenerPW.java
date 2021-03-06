package jenkins.python.expoint;


import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.tasks.BuildStep;
import jenkins.model.Jenkins;
import java.util.List;
import hudson.model.*;
import hudson.model.BuildStepListener.*;
import jenkins.python.DataConvertor;
import jenkins.python.PythonExecutor;

/**
 * This class was automatically generated by the PWM tool on 2014/03/21.
 * @see hudson.model.BuildStepListener
 */
public abstract class BuildStepListenerPW extends BuildStepListener {
	private transient PythonExecutor pexec;

	private void initPython() {
		if (pexec == null) {
			pexec = new PythonExecutor(this);
			String[] jMethods = new String[2];
			jMethods[0] = "started";
			jMethods[1] = "finished";
			String[] pFuncs = new String[2];
			pFuncs[0] = "started";
			pFuncs[1] = "finished";
			Class[][] argTypes = new Class[2][];
			argTypes[0] = new Class[3];
			argTypes[0][0] = AbstractBuild.class;
			argTypes[0][1] = BuildStep.class;
			argTypes[0][2] = BuildListener.class;
			argTypes[1] = new Class[4];
			argTypes[1][0] = AbstractBuild.class;
			argTypes[1][1] = BuildStep.class;
			argTypes[1][2] = BuildListener.class;
			argTypes[1][3] = boolean.class;
			pexec.checkAbstrMethods(jMethods, pFuncs, argTypes);
			String[] functions = new String[0];
			int[] argsCount = new int[0];
			pexec.registerFunctions(functions, argsCount);
		}
	}

	@Override
	public void started(AbstractBuild build, BuildStep bs, BuildListener listener) {
		initPython();
		pexec.execPythonVoid("started", build, bs, listener);
	}

	@Override
	public void finished(AbstractBuild build, BuildStep bs, BuildListener listener, boolean canContinue) {
		initPython();
		pexec.execPythonVoid("finished", build, bs, listener, DataConvertor.fromBool(canContinue));
	}

	public Object execPython(String function, Object... params) {
		initPython();
		return pexec.execPython(function, params);
	}

	public byte execPythonByte(String function, Object... params) {
		initPython();
		return pexec.execPythonByte(function, params);
	}

	public short execPythonShort(String function, Object... params) {
		initPython();
		return pexec.execPythonShort(function, params);
	}

	public char execPythonChar(String function, Object... params) {
		initPython();
		return pexec.execPythonChar(function, params);
	}

	public int execPythonInt(String function, Object... params) {
		initPython();
		return pexec.execPythonInt(function, params);
	}

	public long execPythonLong(String function, Object... params) {
		initPython();
		return pexec.execPythonLong(function, params);
	}

	public float execPythonFloat(String function, Object... params) {
		initPython();
		return pexec.execPythonFloat(function, params);
	}

	public double execPythonDouble(String function, Object... params) {
		initPython();
		return pexec.execPythonDouble(function, params);
	}

	public boolean execPythonBool(String function, Object... params) {
		initPython();
		return pexec.execPythonBool(function, params);
	}

	public void execPythonVoid(String function, Object... params) {
		initPython();
		pexec.execPythonVoid(function, params);
	}
}
