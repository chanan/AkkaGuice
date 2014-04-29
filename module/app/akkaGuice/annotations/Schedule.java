package akkaGuice.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Schedule {
	public int initialDelay() default 500;
	
	public TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
	
	public int interval() default 1000;
}
