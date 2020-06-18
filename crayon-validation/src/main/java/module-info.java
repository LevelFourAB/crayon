module se.l4.crayon.validation {
	requires org.hibernate.validator;
	requires transitive java.validation;

	requires se.l4.crayon.contributions;

	exports se.l4.crayon.validation;
}
