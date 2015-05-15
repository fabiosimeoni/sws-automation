package org.fao.sws.automation.dsl;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import javax.enterprise.context.ApplicationScoped;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.fao.sws.model.configuration.Binder;
import org.fao.sws.model.configuration.Configuration;
import org.fao.sws.model.configuration.Locator;

@Slf4j @RequiredArgsConstructor
@ApplicationScoped
public class FileSystem {
	 
	@NonNull
	Locator locator;
	
	@NonNull
	Binder binder;
	
	@SneakyThrows
	public void store(Configuration config, String name) {
		
		@Cleanup ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		binder.bind(config,stream);
		
		String xml = new String(stream.toByteArray());
		
		name = name +".xml";
		
		log.info("persisting {} @ {}:\n\n{}", name,locator.location(),xml);
		
		@Cleanup FileOutputStream fs = new FileOutputStream(locator.location().resolve(name).toFile()); 
		
		binder.bind(config,fs);
	}

}
