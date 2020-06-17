module se.l4.crayon.config {
	requires transitive com.google.guice;
	requires transitive se.l4.commons.config;
	requires java.validation;

	requires transitive se.l4.crayon.contributions;

	exports se.l4.crayon.config;
}
