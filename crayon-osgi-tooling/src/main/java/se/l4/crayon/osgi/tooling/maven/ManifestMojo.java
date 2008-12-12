package se.l4.crayon.osgi.tooling.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import se.l4.crayon.osgi.tooling.core.FailException;
import se.l4.crayon.osgi.tooling.core.ManifestGenerator;

/**
 * Mojo for generating a manifest.
 * 
 * @author Andreas Holstenson
 * @goal manifest
 * @requiresProject true
 */
public class ManifestMojo
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
	 * 		default-value="${project.build.outputDirectory}/bundle.xml"
	 */
	private File embedXmlLocation;
	
	public void execute()
		throws MojoExecutionException, MojoFailureException
	{
		ManifestGenerator generator = new ManifestGenerator();
		MojoLogger logger = new MojoLogger(getLog());
		
		try
		{
			generator.generate(logger, xml, manifest, embedXml, embedXmlLocation);
		}
		catch(FailException e)
		{
			throw new MojoFailureException(e.getMessage());
		}
	}
}
