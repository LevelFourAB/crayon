package se.l4.crayon.osgi.tooling.maven;

/**
 * Representation of a dependency that should be embedded in the bundle.
 * 
 * @author Andreas Holstenson
 *
 */
public class EmbeddedDependency
{
	private String groupId;
	private String artifactId;
	private boolean transitive = true;
	
	public String getArtifactId()
	{
		return artifactId;
	}
	
	public void setArtifactId(String artifactId)
	{
		this.artifactId = artifactId;
	}
	
	public String getGroupId()
	{
		return groupId;
	}
	
	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}
	
	public boolean isTransitive()
	{
		return transitive;
	}
	
	public void setTransitive(boolean transitive)
	{
		this.transitive = transitive;
	}
}
