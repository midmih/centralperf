package org.centralperf.sampler.driver.gatling;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;

import org.centralperf.model.dao.Run;
import org.centralperf.sampler.api.SamplerRunJob;
import org.centralperf.service.RunResultService;
import org.centralperf.service.ScriptLauncherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread to handle external Gatling job launcher and watcher
 * @author Charles Le Gallic
 *
 */
public class GatlingRunJob implements SamplerRunJob {

	private String[] command;
	private Run run;
	private long startTime;
	private long endTime;
	private boolean running;
	private int exitStatus;
	private StringWriter processOutputWriter = new StringWriter();
	private File simulationFile;
	private File resultFile;

	private GatlingLogReader gatlingLogReader;
	
	private ScriptLauncherService scriptLauncherService;
	private RunResultService runResultService;

	private String gatlingLauncherPath;	
	
	private static final Logger log = LoggerFactory.getLogger(GatlingRunJob.class);

	public GatlingRunJob(String[] command, Run run) {
		this.command = command;
		this.run=run;
	}

	@Override
	/**
	 * Launch the jMeter external program
	 */
	public void run() {
		log.debug("Running a new Gatling job with command "+ Arrays.toString(command));
		startTime = System.currentTimeMillis();
		ProcessBuilder pb = new ProcessBuilder(command);
		pb = pb.redirectErrorStream(true);	
		Map<String, String> env = pb.environment();
		env.put("GATLING_HOME", gatlingLauncherPath);
		Process p;
		try {
			p = pb.start();
			// Gatling is launched
			running = true;
			// Listening to Gatling standard output
		    StreamWriter ouputListener = new StreamWriter(p.getInputStream(), new PrintWriter(processOutputWriter, true));		
		    
		    // Watching for result file change
		    gatlingLogReader = GatlingLogReader.newReader(this.getResultFile(), runResultService, run);
		    
		    ouputListener.start();		    
			while (running) {
				try {
					p.waitFor();
					//Stop File reader task after end of job process
					running = false;
					gatlingLogReader.cancel();
					ouputListener.interrupt();
				}catch (InterruptedException iE) {
					log.warn("Gatling run was interrupted before normal end:"+iE.getMessage(),iE);
					p.destroy();
				}
			}
			exitStatus = p.exitValue();
		} catch (IOException iOE) {log.error("Gatling job can't read output file:"+iOE.getMessage(),iOE);}
		endTime = System.currentTimeMillis();
		scriptLauncherService.endJob(this);
		gatlingLogReader = null;
	}
	
	public String getProcessOutput() {return processOutputWriter.toString();}	
	
	/**
	 * Internal stream writer to redirect Gatling standard output to a PrintWriter
	 * TODO : export to an utility class...
	 * @author charles
	 */
	class StreamWriter extends Thread {
		private InputStream in;
		private PrintWriter pw;

		StreamWriter(InputStream in, PrintWriter pw) {
			this.in = in;
			this.pw = pw;
		}

		@Override
		public void run() {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(in));
				String line = null;
				while ((line = br.readLine()) != null) {
					pw.println(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String[] getCommand() {
		return command;
	}

	public void setCommand(String[] command) {
		this.command = command;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public int getExitStatus() {
		return exitStatus;
	}

	public void setExitStatus(int exitStatus) {
		this.exitStatus = exitStatus;
	}

	public ScriptLauncherService getScriptLauncherService() {
		return scriptLauncherService;
	}

	public void setScriptLauncherService(
			ScriptLauncherService scriptLauncherService) {
		this.scriptLauncherService = scriptLauncherService;
	}

	public File getSimulationFile() {
		return simulationFile;
	}

	public void setSimulationFile(File simulationFile) {
		this.simulationFile = simulationFile;
	}

	public File getResultFile() {
		return resultFile;
	}

	public void setResultFile(File jtlFile) {
		this.resultFile = jtlFile;
	}

	public RunResultService getRunResultService() {
		return runResultService;
	}

	public void setRunResultService(RunResultService runResultService) {
		this.runResultService = runResultService;
	}
	
	public void setGatlingLauncherPath(String gatlingLauncherPath) {
		this.gatlingLauncherPath = gatlingLauncherPath;
	}	
}