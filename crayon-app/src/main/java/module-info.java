module se.l4.crayon.app {
	requires transitive com.google.guice;
	requires transitive se.l4.crayon.module;

	requires se.l4.crayon.config;
	requires se.l4.crayon.services;
	requires se.l4.crayon.vibe;

	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires org.slf4j;
	requires java.logging;
	requires java.xml;

	requires java.validation;
	requires org.hibernate.validator;

	exports se.l4.crayon.app;

	uses se.l4.crayon.module.CrayonModule;
}
