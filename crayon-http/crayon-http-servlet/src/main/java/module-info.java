module se.l4.crayon.http.servlet {
	requires transitive com.google.guice;
	requires transitive java.servlet;

	requires se.l4.crayon.module;
	requires se.l4.crayon.types;

	exports se.l4.crayon.http.servlet;
}
