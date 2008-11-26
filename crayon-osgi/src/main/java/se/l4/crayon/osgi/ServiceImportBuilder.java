package se.l4.crayon.osgi;

import com.google.inject.Provider;

public interface ServiceImportBuilder<T>
{
	Provider<T> single();
}
