module se.l4.crayon {
	requires org.slf4j;

	requires transitive com.google.guice;
	requires javax.inject;
	requires transitive aopalliance;

	exports se.l4.crayon;
}
