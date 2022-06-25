package jco.ql.db.ds.client.shell;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Profile;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;

@Profile("shell")
@ShellComponent
@ShellCommandGroup("JCo")
@Retention(RetentionPolicy.RUNTIME)
public @interface JCoShellCommand {
	String value() default "";
}
