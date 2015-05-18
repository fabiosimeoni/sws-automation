package org.fao.sws.automation;

import static java.util.Arrays.*;
import static org.fao.sws.automation.Templates.*;
import static org.fao.sws.common.Constants.*;
import static org.jooq.SQLDialect.*;
import static org.jooq.impl.DSL.*;

import java.io.Closeable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

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
import org.fao.sws.model.Dimension.Measure;
import org.fao.sws.model.Domain;
import org.fao.sws.model.Flag;
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
	private boolean dryrun = false;
	
	public Database(@NonNull Connection conn, Validator validator) {
		
		this.conn=conn;
		this.validator=validator;
		this.jooq = using(conn,POSTGRES);
	}

	public void createSchemaForEntire(Configuration configuration) {
		
		if (!validator.valid(configuration))
			throw new IllegalArgumentException("configuration is invalid, will not persist it.");
		
		create(configuration);
	}
	
	public void createSchemaFor(Configuration configuration) {
		
		if (!validator.validFragment(configuration))
			throw new IllegalArgumentException("configuration is invalid, will not persist it.");
		
		create(configuration);
	}
	
	void create(Configuration configuration) {
		
		configuration.dimensions().forEach(this::createSchemaFor);
		
		configuration.flags().forEach(this::createSchemaFor);
		
		configuration.domains().forEach(this::createSchemasFor);
	}

	public void createSchemaFor(Dimension dim) {
		
		log.info("storing dimension '{}'",dim.id());
		
		List<Object[]> model = new ArrayList<>(asList(
				 $("clean",clean)
				,$("table",dim.table())
				,$("length",dim.length())
				,$("hierarchyTable",dim.hierarchyTable())
				,$("parent",dim.parent())
				,$("child",dim.child())
		));
		
		if (dim instanceof Measure)
			model.add($("measure",true));
		
		String ddl = instantiate("dimension",model);

		log.info("\n\n{}",ddl);
		
		
		if (!dryrun)
			jooq.execute(ddl);
		
	}
	
	public void createSchemaFor(Flag flag) {
		
		log.info("storing flag '{}'",flag.id());
		
		String ddl = instantiate("flag", 
									 $("clean",clean)
									,$("table",flag.table())
									,$("length",flag.length())
									);

		log.info("\n\n{}",ddl);
		
		
		if (!dryrun)
			jooq.execute(ddl);
		
	}
	
	public void createSchemasFor(Domain domain) {
		
		log.info("storing domain '{}'",domain.id());
		
		domain.datasets().forEach(this::createSchemaFor);
	}
	
	
	public void createSchemaFor(Dataset dataset) {
		
		log.info("storing dataset '{}'",dataset.id());
		
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
		
		log.info("\n\n{}",ddl);
  
		
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
