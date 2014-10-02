package de.take_weiland.mods.commons.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author diesieben07
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface InvokeDynamic {

	String name();
	String bootstrapClass();
	String bootstrapMethod();
	BootstrapArg[] bootstrapArgs() default { };

	@interface BootstrapArg {

		ArgType type();
		String stringValue() default "";
		boolean booleanValue() default false;
		int intValue() default 0;
		long longValue() default 0;
		float floatValue() default 0;
		double doubleValue() default 0;

	}

	enum ArgType {
		STRING,
		BOOLEAN,
		INT,
		LONG,
		FLOAT,
		DOUBLE
	}

	String CLASS_NAME = "de/take_weiland/mods/commons/internal/InvokeDynamic";
	String NAME = "name";
	String BS_OWNER = "bootstrapClass";
	String BS_NAME = "bootstrapMethod";
	String BS_ARGS = "bootstrapArgs";
	String TYPE_STRING = "STRING";
	String TYPE_BOOLEAN = "BOOLEAN";
	String TYPE_INT = "INT";
	String TYPE_LONG = "LONG";
	String TYPE_FLOAT = "FLOAT";
	String TYPE_DOUBLE = "DOUBLE";
	String STRING_VALUE = "stringValue";
	String BOOLEAN_VALUE = "booleanValue";
	String INT_VALUE = "intValue";
	String LONG_VALUE = "longValue";
	String FLOAT_VALUE = "floatValue";
	String DOUBLE_VALUE = "doubleValue";

}
