module se.l4.crayon.config {
	requires transitive com.google.guice;
	requires transitive se.l4.exoconf;

	requires se.l4.crayon.contributions;
	requires se.l4.crayon.validation;

	exports se.l4.crayon.config;
}
