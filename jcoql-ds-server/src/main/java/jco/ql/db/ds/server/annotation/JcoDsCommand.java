package jco.ql.db.ds.server.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("server")
@Retention(RetentionPolicy.RUNTIME)
public @interface JcoDsCommand {

}
