package se.l4.crayon.services;

import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

/**
 * Abstraction of a running service.
 */
public interface RunningService
{
	/**
	 * Stop this service.
	 *
	 * @return
	 */
	Mono<Boolean> stop();

	/**
	 * {@link Mono} that completes if the service stops for any reason, or
	 * errors if an error occurs during running.
	 *
	 * @return
	 */
	Mono<Void> onStop();

	/**
	 * Get an instance representing a service that can be stopped with the
	 * given action.
	 *
	 * <pre>
	 * class ServiceImpl implements StartableService {
	 *   {@literal @}Override
	 *   public Mono<RunningService> start() {
	 *     return Mono.fromSupplier(r -> {
	 *       // Do stuff to start the service here
	 *
	 *       return RunningService.stoppable(runnableToStopService);
	 * 	   }).
	 *   }
	 * }
	 * </pre>
	 *
	 * @return
	 */
	static RunningService stoppable(Runnable stopAction)
	{
		MonoProcessor<Void> processor = MonoProcessor.create();

		return new RunningService()
		{
			@Override
			public Mono<Boolean> stop()
			{
				return Mono.fromSupplier(() -> {
					try
					{
						stopAction.run();
						processor.onNext(null);
					}
					catch(Throwable t)
					{
						processor.onError(t);
					}

					return true;
				});
			}

			@Override
			public Mono<Void> onStop()
			{
				return processor;
			}
		};
	}

	/**
	 * Get an instance representing a service that can not be stopped.
	 *
	 * <pre>
	 * class ServiceImpl implements StartableService {
	 *   {@literal @}Override
	 *   public Mono<RunningService> start() {
	 *     return Mono.fromSupplier(r -> {
	 *       // Do stuff to start the service here
	 *
	 *       return RunningService.unstoppable();
	 * 	   });
	 *   }
	 * }
	 * </pre>
	 *
	 * @return
	 */
	static RunningService unstoppable()
	{
		return new RunningService()
		{
			@Override
			public Mono<Boolean> stop()
			{
				return Mono.empty();
			}

			@Override
			public Mono<Void> onStop()
			{
				return Mono.never();
			}
		};
	}
}
