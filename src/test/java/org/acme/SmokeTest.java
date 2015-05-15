package org.acme;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.jooq.DSLContext;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.googlecode.jeeunit.JeeunitRunner;

@RunWith(JeeunitRunner.class)
public class SmokeTest {
	
	@Inject
	DSLContext context;
	
	@Test
	public void deps_can_be_injected() {
		assertNotNull(context);
	}

}
