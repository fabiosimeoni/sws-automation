package org.acme;

import static org.fao.sws.model.configuration.Dsl.*;

import javax.inject.Inject;

import org.fao.sws.automation.dsl.Deployment;
import org.fao.sws.automation.dsl.FileSystem;
import org.fao.sws.model.Dataset;
import org.fao.sws.model.Dimension;
import org.fao.sws.model.Domain;
import org.fao.sws.model.Flag;
import org.fao.sws.model.configuration.Configuration;
import org.junit.Test;

public class ConfigGenerationTest extends AutomationTest {

	@Inject @Deployment(config="src/main/resources")
	FileSystem fs;
	

	@Test
	public void persist_fragment() {
		
		Configuration fragment = sws()
				.contact("john.doe@acme.org")
				.contact("joe.plumber@acme.org")
				.with(aDomain());
		
		
		fs.store(fragment,"test");
	}
	
	Domain aDomain() {
		
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
		
		
		return domain("d").with(ds1);
		
		
	}
}
