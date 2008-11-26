package se.l4.crayon.osgi.internal;

import org.osgi.framework.BundleContext;

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Description;
import se.l4.crayon.annotation.Shutdown;
import se.l4.crayon.osgi.ExportManager;
import se.l4.crayon.osgi.ServiceRefManager;

import com.google.inject.Binder;

public class InternalServices
{
	private BundleContext ctx;
	
	public InternalServices(BundleContext ctx)
	{
		this.ctx = ctx;
	}
	
	@Description
	public void configure(Binder binder)
	{
		binder.bind(BundleContext.class).toInstance(ctx);
		binder.bind(ExportManager.class).to(ExportManagerImpl.class);
		binder.bind(ServiceRefManager.class).to(ServiceRefManagerImpl.class);
	}
		
	@Contribution
	public void contribute(ExportManagerImpl manager)
	{
		manager.autoExport();
	}
	
	@Shutdown
	public void shutdown(ExportManager manager)
	{
		manager.removeAll();
	}
}