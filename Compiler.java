import java.io.*;
import java.util.concurrent.*;
import java.util.*;
public class Compiler{ 
	
	public static String testName = "DEFAULT";
	private static int k = 0;
	private static void printLines(String name, InputStream ins) throws Exception{
		String line = null;
		BufferedReader in = new BufferedReader(
			new InputStreamReader(ins));
		while((line=in.readLine()) != null){
			System.out.println(name + " " + line);
		}
	}

	private static int runProcess(String command) throws Exception {
		Process pro = Runtime.getRuntime().exec(command);
		printLines(command + " stdout:", pro.getInputStream());
		printLines(command + " stderr:", pro.getErrorStream());
		pro.waitFor();
		return pro.exitValue();
	}

	public static void specifiedCompiler(String name) throws Exception{
		System.out.println("Compiling " + name + "...");
		k = runProcess("javac "+ name+".java");
		if(k==0)
			System.out.println(name + " compiled successfully.");
		if((name.equals(testName))&&(k==0)){
			System.out.println("Running main...");
			runProcess("java "+name);
		}
	}

	public static String [] getFileNames(){
		ArrayList<String> list = new ArrayList<String>();
		File f = new File(".");
		File [] files = f.listFiles();
		for(File file : files){
			if (file.getName().endsWith(".java"))
				if(!file.getName().equals("Compiler.java"))
					list.add(file.getName().substring(0,file.getName().length()-5));
		}
		String [] names = new String[list.size()];
		names = list.toArray(names);
		return names;
	}

	private static class SingleCompile implements Runnable{

		private String className;
		private Thread t;
		private CountDownLatch cdl;

		private SingleCompile(String className,CountDownLatch cdl){
			t = new Thread(this);
			this.className=className;
			this.cdl = cdl;
		}

		private void start(){
			t.start();
		}

		@Override
		public void run(){
			try{
				if(!className.equals(testName)){
					specifiedCompiler(className);
					cdl.countDown();
				}
				else{
					cdl.await();
					specifiedCompiler(className);
				}
			}catch(Exception e){
				e.printStackTrace();
			} 
		}
	}

	public static void main(String [] args){
		if(testName.equals("DEFAULT")){
			System.out.println("CHANGE TEST NAME & RECOMPILE COMPILER: TestName still reads \"DEFAULT\".");
		}
		try{
			if(args.length==0){
				args = getFileNames();
			}
			CountDownLatch cdl = new CountDownLatch(args.length-1);
			for (String s: args){
				new SingleCompile(s,cdl).start();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}