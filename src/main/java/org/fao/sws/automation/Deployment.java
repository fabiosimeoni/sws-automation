package org.fao.sws.automation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
@Target({FIELD, PARAMETER, METHOD})
public @interface Deployment {
	
	@Nonbinding
	String host() default "localhost";
	
	@Nonbinding
	String port() default "5432";
	
	@Nonbinding
	String db() default "sws_data";
	
	@Nonbinding
	String user() default "sws";
	
	@Nonbinding
	String pwd() default "sws";
	
	@Nonbinding
	String config() default "src/test/resources";
}
