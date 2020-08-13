module se.l4.crayon.module {
	requires transitive com.google.guice;
	requires transitive se.l4.crayon.config;
	requires transitive se.l4.crayon.contributions;
	requires transitive org.slf4j;

	exports se.l4.crayon.module;
}
