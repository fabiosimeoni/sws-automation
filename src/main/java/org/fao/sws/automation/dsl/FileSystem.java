package org.fao.sws.automation.dsl;

import static java.nio.file.Files.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.fao.sws.model.configuration.Binder;
import org.fao.sws.model.configuration.Configuration;
import org.fao.sws.model.configuration.Locator;
import org.fao.sws.model.configuration.Validator;

@Slf4j @RequiredArgsConstructor
@ApplicationScoped
public class FileSystem {
	 
	@NonNull
	Locator locator;
	
	@NonNull
	Binder binder;
	
	@NonNull
	Validator validator;
	
	
	@SneakyThrows
	public void store(Configuration fragment, String name) {
		
		@Cleanup ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		binder.bind(fragment,stream);
		
		String xml = new String(stream.toByteArray());
		
		name = name +".xml";

		log.info("persisting {} @ {}:\n\n{}", name,locator.location(),xml);

		if (!validator.validFragment(fragment))
			throw new IllegalArgumentException("configuration is invalid, will not persist it.");
		
		Path destination = locator.location().resolve(name);
		
		createDirectories(destination.getParent());
		
		@Cleanup FileOutputStream fs = new FileOutputStream(destination.toFile()); 
		
		binder.bind(fragment,fs);
	}

}
