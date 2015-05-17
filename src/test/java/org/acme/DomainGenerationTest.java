package org.acme;

import static org.fao.sws.model.configuration.Dsl.*;

import javax.inject.Inject;

import org.fao.sws.automation.Database;
import org.fao.sws.automation.Recipe;
import org.fao.sws.model.Dataset;
import org.fao.sws.model.Dimension;
import org.fao.sws.model.Flag;
import org.fao.sws.model.configuration.Configuration;
import org.junit.After;
import org.junit.Test;

public class DomainGenerationTest extends Recipe {

	@Inject
	Database db;
	

	@Test
	public void create_dataset() {
		
		Dimension dim = dimension("test-dimension");
		Dimension tdim = timeDimension("time-dimension");
		Dimension mdim = measureDimension("measure-dimension");
		Flag flag = flag("test-flag");
	
		Dataset dataset = dataset("test")
								.with(dim.ref(),tdim.ref(),mdim.ref())
								.with(flag.ref());
		
		Configuration config = sws()
								.contact("john.doe@acme.org")
								.with(dim)
								.with(domain().with(dataset));
		
		
		db.store(config);
	}
	
	@After
	public void cleanup() {
		
		db.close();
	}
}
