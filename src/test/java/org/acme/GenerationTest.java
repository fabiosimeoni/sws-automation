package org.acme;

import static org.fao.sws.model.configuration.Dsl.*;

import javax.inject.Inject;

import org.fao.sws.automation.Database;
import org.fao.sws.automation.Deployment;
import org.fao.sws.automation.FileSystem;
import org.fao.sws.automation.Recipe;
import org.fao.sws.model.Dataset;
import org.fao.sws.model.Dimension;
import org.fao.sws.model.Flag;
import org.fao.sws.model.configuration.Configuration;
import org.junit.After;
import org.junit.Test;

public class GenerationTest extends Recipe {

	@Inject @Deployment(config="src/test/resources")
	FileSystem fs;
	
	@Inject @Deployment(user="fabio",pwd="fabio")
	Database db;
	
	@Test
	public void store_config_fragment() {
		
		fs.store(aFragment(),"test");
	}
	

	@Test
	public void store_db_dimension() {
		
		Dimension dim = dimension("test").length(5);
		
		//db.clean(true);
		//db.dryrun(true);
		db.store(dim);
		
	}
	
	@Test
	public void store_db_fragment() {
		
		db.storeFragment(aFragment());

		db.close();
	}
	
	@Test
	public void store_dimension() {
		
		db.storeFragment(aFragment());

	}
	
	@After
	public void shutdown() {
	
		db.close();
	}
	
	//////////////////////////////////////////////////////////////////
	

	Configuration aFragment() {
			
			Dimension dim = dimension("a");
			Dimension time = timeDimension("b");
			Dimension measure = measureDimension("c");
			Flag flag = flag("f");
			
			dim.labelKey("key"); //defaulting to id will do 99/100 times. just to test here.
					
			Dataset ds1 = dataset("ds1").with(
					
					dim.ref().roots(110,120,130),
					time.ref().sdmxCode("somecode").descending(),
					measure.ref()
					
				)
				.with(
					flag.ref()
				);
			
			
			return sws()
					.with(dim)
					.contact("john.doe@acme.org")
					.contact("joe.plumber@acme.org")
					.with(domain("d").with(ds1));
			
			
		}
	
}
