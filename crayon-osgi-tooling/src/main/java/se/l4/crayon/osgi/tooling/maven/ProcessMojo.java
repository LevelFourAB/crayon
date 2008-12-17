package se.l4.crayon.osgi.tooling.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import se.l4.crayon.osgi.tooling.core.BundleProcessor;
import se.l4.crayon.osgi.tooling.core.FailException;

/**
 * Mojo for processing a Bundle XML-file by generating a valid manifest, will
 * also copy the processed XML-file into the build-directory along with any
 * embedded dependencies.
 * 
 * @author Andreas Holstenson
 * @goal process
 * @phase compile
 * @requiresProject true
 * @requiresDependencyResolution compile
 */
public class ProcessMojo
	extends AbstractMojo
{
	/**
	 * XML-file to generate from.
	 * 
	 * @parameter
	 * 		default-value="bundle.xml"
	 */
	private File xml;
	
	/**
	 * The location of the manifest to be generated.
	 * 
	 * @parameter
	 * 		default-value="${project.build.outputDirectory}/META-INF/MANIFEST.MF"
	 */
	private File manifest;
	
	/**
	 * If the bundle.xml should be processed and embedded into the JAR (by
	 * placing it into {@code project.build.outputDirectory}).
	 * 
	 * @parameter
	 * 		default-value="true"
	 */
	private boolean embedXml;
	
	/**
	 * The location of the embedded Bundle XML.
	 * 
	 * @parameter
	 * 		default-value="${project.build.outputDirectory}/OSGI-OPT/bundle.xml"
	 */
	private File embedXmlLocation;
	
	/**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
	private MavenProject project;
	
	/**
     * The directory for the generated bundles.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File outputDirectory;
    
    /**
     * Dependencies that should be embedded within the bundle.
     * 
     * @parameter
     */
    private EmbeddedDependency[] embedded;
    
    /** @component */
    private ArtifactResolver resolver;

    /**@parameter expression="${localRepository}" */
    private ArtifactRepository localRepository;

    /** @parameter expression="${project.remoteArtifactRepositories}" */
    private List<ArtifactRepository> remoteRepositories;

    /** @component */
    private ArtifactMetadataSource metadataSource;
    
	public void execute()
		throws MojoExecutionException, MojoFailureException
	{
		BundleProcessor processor = new BundleProcessor(new MojoLogger(getLog()));
		
		// Set the values that are needed
		processor.setXmlLocation(xml);
		processor.setManifestLocation(manifest);
		processor.setEmbedXml(embedXml);
		processor.setEmbedXmlLocation(embedXmlLocation);
		processor.setOutputDirectory(outputDirectory);

		// Generate a set with all dependencies that should be embedded
		Map<String, EmbeddedDependency> embeddedDeps = new HashMap<String, EmbeddedDependency>();
		if(embedded != null)
		{
			for(EmbeddedDependency d : embedded)
			{
				if(d.getGroupId() == null || d.getArtifactId() == null)
				{
					throw new MojoFailureException("Embedded dependency requires both groupId and artifactId");
				}
				
				embeddedDeps.put(d.getGroupId() + ":" + d.getArtifactId(), d);
			}
		}
		
		List<File> embeddedFiles = new LinkedList<File>();
		Set<Artifact> embeddedArtifacts = new HashSet<Artifact>();
		
		// Get the classpath from the POM and set it
		Set<Artifact> artifacts = project.getArtifacts();
		
		List<File> classpath = new ArrayList<File>(artifacts.size());
		classpath.add(outputDirectory);
		
		int i=0;
		for(Artifact artifact : artifacts)
		{
			i++;
			File file = artifact.getFile();
			classpath.add(file);
			
			// Check if the artifact needs to be embedded
			String artifactString = artifact.getGroupId() + ":" + artifact.getArtifactId();
			EmbeddedDependency dep = embeddedDeps.get(artifactString);
			
			if(dep != null)
			{
				if(dep.isTransitive())
				{
					embeddedArtifacts.add(artifact);
				}
				else
				{
					embeddedFiles.add(artifact.getFile());
				}
				
				embeddedDeps.remove(artifactString);
			}
		}
		
		processor.setClasspath(classpath);
		
		// Check if all the embedded dependencies could be found
		if(false == embeddedDeps.isEmpty())
		{
			StringBuilder builder = new StringBuilder();
			builder.append("Unresolved embedded dependencies:");
			
			for(String s : embeddedDeps.keySet())
			{
				builder.append(" ").append(s);
			}
			
			throw new MojoFailureException(builder.toString());
		}
		
		// Transitively resolve the embedded dependencies and add them
		try
		{
			ArtifactResolutionResult result = resolver.resolveTransitively(
					embeddedArtifacts, 
					project.getArtifact(), 
					localRepository, 
					remoteRepositories, 
					metadataSource, 
					null);
			
			Set<Artifact> results = result.getArtifacts();
			for(Artifact a : results)
			{
				embeddedFiles.add(a.getFile());
			}
		}
		catch(ArtifactResolutionException e1)
		{
			throw new MojoFailureException(e1.getMessage());
		}
		catch(ArtifactNotFoundException e1)
		{
			throw new MojoFailureException(e1.getMessage());
		}
		
		processor.setEmbeddedFiles(embeddedFiles);
		
		// Start the processing
		try
		{
			processor.generate();
		}
		catch(FailException e)
		{
			throw new MojoFailureException(e.getMessage());
		}
	}
	
	public MavenProject getProject()
	{
		return project;
	}
	
	public void setProject(MavenProject project)
	{
		this.project = project;
	}

}
