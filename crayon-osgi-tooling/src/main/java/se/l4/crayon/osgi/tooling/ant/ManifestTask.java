package se.l4.crayon.osgi.tooling.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class ManifestTask
	extends Task
{
	private File xml;
	private File manifest;
	
	private boolean embedXml;
	private File embedXmlLocation;
	
	public ManifestTask()
	{
		embedXml = true;
	}
	
	@Override
	public void execute()
		throws BuildException 
	{
//		BundleProcessor generator = new BundleProcessor();
//		TaskLogger logger = new TaskLogger(this);
//		
//		try
//		{
//			generator.generate(logger, xml, manifest, embedXml, embedXmlLocation, new File[0]);
//		}
//		catch(FailException e)
//		{
//			throw new BuildException(e.getMessage());
//		}
	}
	
	public File getXml()
	{
		return xml;
	}
	
	public void setXml(File xml)
	{
		this.xml = xml;
	}
	
	public File getManifest()
	{
		return manifest;
	}
	
	public void setManifest(File manifest)
	{
		this.manifest = manifest;
	}
	
	public boolean isEmbedXml()
	{
		return embedXml;
	}
	
	public void setEmbedXml(boolean embedXml)
	{
		this.embedXml = embedXml;
	}
	
	public File getEmbedXmlLocation()
	{
		return embedXmlLocation;
	}
	
	public void setEmbedXmlLocation(File embedXmlLocation)
	{
		this.embedXmlLocation = embedXmlLocation;
	}
}
