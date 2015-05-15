package org.acme;

import static org.fao.sws.model.configuration.Dsl.*;

import javax.inject.Inject;

import org.fao.sws.automation.Recipe;
import org.fao.sws.automation.dsl.Database;
import org.fao.sws.automation.dsl.Deployment;
import org.fao.sws.model.Dataset;
import org.fao.sws.model.Dimension;
import org.fao.sws.model.Flag;
import org.fao.sws.model.configuration.Configuration;
import org.junit.After;
import org.junit.Test;

public class DomainGenerationTest extends Recipe {

	@Inject @Deployment()
	Database db;
	

	@Test
	public void create_dataset() {
		
		Dimension dim = dimension("test-dimension");
		Flag flag = flag("test-flag");
	
		Dataset dataset = dataset("test")
								.with(dim.ref())
								.with(flag.ref());
		
		Configuration config = sws()
								.with(dim)
								.with(domain().with(dataset));
		
		
		db.store(config);
	}
	
	@After
	public void cleanup() {
		
		db.close();
	}
}
