package org.fao.sws.automation.dsl;

import static java.lang.System.*;
import static java.nio.file.Files.*;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Properties;

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
		
		Path destination = locator.location().resolve(name+".xml");
		
		createDirectories(destination.getParent());
		
		storeConfig(fragment, destination);
		
		storeLabels(fragment, destination.getParent().resolve("Labels.properties"));
		
		
	}

	
	@SneakyThrows
	private void storeConfig(Configuration fragment, Path path) {
		
		log.info("persisting {} @ {}", path,locator.location());

		binder.bind(fragment,err);
		
		if (!validator.validFragment(fragment))
			throw new IllegalArgumentException("configuration is invalid, will not persist it.");
		
		@Cleanup FileOutputStream fs = new FileOutputStream(path.toFile()); 
		
		binder.bind(fragment,fs);
	}
	
	@SneakyThrows
	private void storeLabels(Configuration fragment, Path path) {
		
		Properties props = new Properties();
		
		fragment.dimensions().forEach(d->{
			
			props.put(d.labelKey(),d.label());	
			
		});
		
		fragment.flags().forEach(f->{
			
			props.put(f.labelKey(),f.label());	
			
		});
		
		fragment.domains().forEach(d->{
			
			props.put(d.labelKey(),d.label());
			
			d.datasets().forEach(ds->{
				
				props.put(ds.labelKey(),ds.label());	
				
			});
			
		});
		
		log.info("persisting labels @ {}", locator.location());
		
		props.store(err,null);
				
		@Cleanup FileOutputStream fs = new FileOutputStream(path.toFile()); 
		
		props.store(fs,null);
	}
}
