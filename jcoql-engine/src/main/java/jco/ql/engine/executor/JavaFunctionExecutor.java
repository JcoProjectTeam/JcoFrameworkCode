package jco.ql.engine.executor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import jco.ql.engine.EngineConfiguration;
import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.command.JavaFunctionCommand;
import jco.ql.model.engine.JMH;


@Executor(JavaFunctionCommand.class)
public class JavaFunctionExecutor implements IExecutor<JavaFunctionCommand> {
    @Override
    public void execute(Pipeline pipeline, JavaFunctionCommand command) throws ExecuteProcessException {
    	// PF. 2022.03.24 - New Policy... in case of already existing USER Defined Function, a message is emitted and the newer version replace the old one
        if (compileCode (command)) {
	        if(pipeline.getUserFunctions().containsKey(command.getFunctionName())) 
	        	JMH.addJSMessage("[" + command.getInstruction().getInstructionName() + "]:\tdefinition of " + command.getFunctionName() + " function has been replaced.");        	
	
	        pipeline.addUserFunction(command);
        }
        else
        	JMH.addJSMessage("[" + command.getInstruction().getInstructionName() + "]:\tJava function " + command.getFunctionName() + " cannot be compiled.");        	
    }
    
    
	private boolean compileCode(JavaFunctionCommand jfc) {
		try {
	        String tmpProperty = EngineConfiguration.getTempDirectory();
	        Path javaFile = Paths.get(tmpProperty, jfc.getClassName() + ".java");
	        Files.write(javaFile, jfc.getCode().getBytes());

	        
	        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	        OutputStream compileErrors = new ByteArrayOutputStream();
	        int errors = compiler.run(null, null, compileErrors, javaFile.toFile().getAbsolutePath());
	        // nok
	        if (errors != 0) 
				JMH.addJSMessage("[" + jfc.getInstruction().getInstructionName() + "]:\tJava function " + jfc.getFunctionName() + 
	        		" in class " + jfc.getClassName() + " cannot be compiled\n" + compileErrors.toString());
	        // code not compliant to specification
	        else if (!validateFunction (jfc))
				JMH.addJSMessage("[" + jfc.getInstruction().getInstructionName() + "]:\tJava function " + jfc.getFunctionName() + 
									" in class " + jfc.getClassName() + " does not comply with definition");
	        // ok
	        else {
	            jfc.setJavaClass (javaFile.getParent().resolve(jfc.getClassName() + ".class"));
				JMH.addJSMessage("[" + jfc.getInstruction().getInstructionName() + "]:\tfunction " + jfc.getFunctionName() + " compiled and registered");
		        compileErrors.close();
		        return true;
	        } 
	        compileErrors.close();
	        
		} catch (IOException e) {
        	JMH.addJSMessage("[" + jfc.getInstruction().getInstructionName() + "]: definition of " + jfc.getClassName() + " function impossible to compile.\n" + e.toString());        	
		}
		return false;
	}
 
    
    @SuppressWarnings({ "resource", "rawtypes", "unchecked" })
	private boolean validateFunction (JavaFunctionCommand jfc ) {
    	// Create a File object on the root of the directory containing the class file
    	File file = new File(EngineConfiguration.getTempDirectory());

    	try {
    	    // Convert File to a URL
    	    URL url = file.toURI().toURL();          // file:/c:/myclasses/
    	    URL[] urls = new URL[]{url};

    	    // Create a new class loader with the directory
    	    ClassLoader cl = new URLClassLoader(urls);
    	    Class cls = cl.loadClass(jfc.getClassName());
	    	
    		Class cArg[]=new Class [jfc.getParameters().size()];
    		for (int i=0; i<jfc.getParameters().size(); i++) {
    			 jco.ql.parser.model.util.Parameter p = jfc.getParameters().get(i);
    			if ("int".equals(p.type))
    				cArg[i] = int.class;
    			else if ("double".equals(p.type))
    				cArg[i] = double.class;
    			else if ("String".equals(p.type))
    				cArg[i] = String.class;
    			else
    				cArg[i] = Object.class;
    		}

			Method myMethod = null;
			try {
				myMethod = cls.getDeclaredMethod(jfc.getFunctionName(), cArg);
			} catch (NoSuchMethodException | SecurityException e1) {
				String msg = "[" + jfc.getInstruction().getInstructionName() + "]:\tno " + jfc.getFunctionName() + " (";
				StringJoiner sj = new StringJoiner(", ");
	    		for (int i=0; i<jfc.getParameters().size(); i++) 
	    			 sj.add(jfc.getParameters().get(i).type);
	    		msg += sj.toString() + ") function in Java class " + jfc.getClassName();
				JMH.addJSMessage(msg);
				return false; 
			}
			jfc.setMethodToInvoke(myMethod);
	    	return true;
    	} catch (MalformedURLException | ClassNotFoundException e) {
			JMH.addJSMessage("[" + jfc.getInstruction().getInstructionName() + "]:\tJava function " + jfc.getFunctionName() + 
	        		" in class " + jfc.getClassName() + " not found\n" + e.toString());
    		return false;
    	}
    }
}
