package org.fao.sws.automation.dsl;

import static org.fao.sws.automation.dsl.Templates.*;
import static org.fao.sws.common.Constants.*;
import static org.jooq.SQLDialect.*;
import static org.jooq.impl.DSL.*;

import java.io.Closeable;
import java.sql.Connection;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.fao.sws.model.Dataset;
import org.fao.sws.model.Dimension;
import org.fao.sws.model.Domain;
import org.fao.sws.model.configuration.Configuration;
import org.fao.sws.model.configuration.Validator;
import org.jooq.DSLContext;

@Slf4j
@ApplicationScoped @Alternative @Priority(high)
public class Database implements Closeable {
	 
	private final Connection conn;
	
	private final DSLContext jooq;
	
	private final Validator validator;
	
	@Setter
	private boolean clean = false;
	
	@Setter
	private boolean dryrun = true;
	
	public Database(@NonNull Connection conn, Validator validator) {
		
		this.conn=conn;
		this.validator=validator;
		this.jooq = using(conn,POSTGRES);
	}
	
	public void store(Configuration fragment) {
		
		if (!validator.validFragment(fragment))
			throw new IllegalArgumentException("configuration is invalid, will not persist it.");
		
		fragment.dimensions().forEach($->add($));
		
		fragment.domains().forEach($->add($));
	}

	public void add(Dimension dim) {
		
		log.info("creating dimension {}",dim.id());
		
		//TODO
		
	}
	
	public void add(Domain domain) {
		
		log.info("creating domain {}",domain.id());
		
		domain.datasets().forEach(this::add);
	}
	
	
	public void add(Dataset dataset) {
		
		log.info("creating schema for dataset {}",dataset.id());
		
		String ddl = instantiate("dataset", 
								 $("clean",clean) 
								,$("schema",dataset.schema())
								,$("observation",dataset.table())
								,$("metadata",dataset.metadataTable())
								,$("metadata_element",dataset.metadataElementTable())
								,$("observation_coordinate",dataset.coordinatesTable())
								,$("dimensions",dataset.dimensions().all())
								,$("session_metadata",dataset.sessionMetadataTable())
								,$("session_metadata_element",dataset.sessionMetadataElementTable())
								,$("session_observation",dataset.sessionObservationTable())
								,$("session_validation",dataset.sessionValidationTable())
								,$("validation",dataset.validationTable())
								,$("tag_observation",dataset.tagObservationTable())
								,$("flags",dataset.flags().all())
								);
		
		log.info("executing script: \n\n{}",ddl);
  
		
		if (!dryrun)
			jooq.execute(ddl);
		
	}
	
	
	
	@Override @SneakyThrows
	public void close() {
		
		log.info("closing connection");
		
		@Cleanup 
		Connection autoclose = conn;
		
	}
	
	//////////////////////////////////////////////////////////////////// helpers
	

}
