package org.fao.sws.automation.utils;

import static java.lang.String.*;
import static org.fao.sws.common.Constants.*;

import java.sql.Connection;
import java.sql.DriverManager;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.fao.sws.automation.Database;
import org.fao.sws.automation.Deployment;
import org.fao.sws.automation.FileSystem;
import org.fao.sws.model.configuration.Binder;
import org.fao.sws.model.configuration.Locator;
import org.fao.sws.model.configuration.Validator;


@ApplicationScoped @Alternative @Priority(high)
@Slf4j
public class Producers {

	@Produces @Deployment @SneakyThrows
	FileSystem filesystem(InjectionPoint point, Binder binder, Validator validator) {
		
		Deployment d = point.getAnnotated().getAnnotation(Deployment.class);
		
		Locator locator = d.config()==null || d.config().isEmpty() ? new Locator() : new Locator(d.config());
		
		return new FileSystem(locator, binder,validator);
			
	}
	
	@Produces @Deployment @SneakyThrows
	Database database(InjectionPoint point, Validator validator) {
		
		
		Deployment d = point.getAnnotated().getAnnotation(Deployment.class);
		
		String url = format("jdbc:postgresql://%s:%s/%s",d.host(),d.port(),d.db());
		
		log.info("connecting to {} as '{}'",url,d.user());
		
		Connection conn =  DriverManager.getConnection(url,d.user(), d.pwd());
		
		return new Database(conn,validator);
	
	}

	//cute trick: 
	// we want to specify @Deployment in injection points only to override its defaults, otherwise we'd rather omit it. 
	// we achieve this by getting it injected here and then re-producing it without the qualifier
	@Produces
	Database default_database(@Deployment Database db) {
		return db;
	}
	
	
}
