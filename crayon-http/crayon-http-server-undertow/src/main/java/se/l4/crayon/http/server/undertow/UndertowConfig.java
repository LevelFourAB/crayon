package se.l4.crayon.http.server.undertow;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import se.l4.exobytes.AnnotationSerialization;
import se.l4.exobytes.Expose;

/**
 * Configuration used with {@link UndertowService}.
 */
@AnnotationSerialization
public class UndertowConfig
{
	@Expose
	@Min(1) @Max(65535)
	private int port = 8080;

	public int getPort()
	{
		return port;
	}
}
