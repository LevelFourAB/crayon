package se.l4.crayon.osgi.remoting.internal.def;

import java.io.Serializable;

public class ImportDef
	implements Serializable
{
	private final String packageName;
	private final String version;
	
	public ImportDef(String packageName, String version)
	{
		this.packageName = packageName;
		this.version = version;
	}
	
	public String getPackageName()
	{
		return packageName;
	}
	
	public String getVersion()
	{
		return version;
	}
	
	@Override
	public String toString()
	{
		return packageName + ";version=" + version;
	}
}
