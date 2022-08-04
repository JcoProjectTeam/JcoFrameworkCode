package jco.ql.engine.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.stereotype.Component;

import jco.ql.model.command.ICommand;

@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface Executor {
	Class<? extends ICommand> value();
	boolean overrideStandard() default false;
}
