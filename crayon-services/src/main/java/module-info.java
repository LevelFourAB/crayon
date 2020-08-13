module se.l4.crayon.services {
	requires transitive se.l4.crayon.module;
	requires se.l4.ylem.types.matching;

	requires transitive org.reactivestreams;
	requires transitive reactor.core;

	exports se.l4.crayon.services;
}
