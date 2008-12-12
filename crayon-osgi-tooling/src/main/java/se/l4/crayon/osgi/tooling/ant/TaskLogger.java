package se.l4.crayon.osgi.tooling.ant;

import org.apache.tools.ant.Task;

import se.l4.crayon.osgi.tooling.core.Logger;

public class TaskLogger
	implements Logger
{
	private Task task;
	
	public TaskLogger(Task task)
	{
		this.task = task;
	}
	
	public void debug(String msg)
	{
//		task.log("DEBUG: " + msg);
	}
	
	public void info(String msg)
	{
		task.log("INFO: " + msg);
	}
	
	public void warn(String msg)
	{
		task.log("WARN: " + msg);
	}
	
	public void error(String msg)
	{
		task.log("ERROR: " + msg);
	}
}
