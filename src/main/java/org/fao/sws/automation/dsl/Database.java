package org.fao.sws.automation.dsl;

import static java.lang.String.*;
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
import org.jooq.DSLContext;

@Slf4j
@ApplicationScoped @Alternative @Priority(high)
public class Database implements Closeable {
	 
	private final Connection conn;
	
	private final DSLContext jooq;
	
	@Setter
	private boolean clean = true;
	
	public Database(@NonNull Connection conn) {
		
		this.conn=conn;
		this.jooq = using(conn,POSTGRES);
	}
	
	public void store(Configuration config) {
		
		config.dimensions().forEach($->add($));
		
		config.domains().forEach($->add($));
	}

	public void add(Dimension dim) {
		
		log.info("creating dimension {}",dim.id());
		
		//TODO
		
	}
	
	public void add(Domain domain) {
		
		log.info("creating domain {}",domain.id());
		
		domain.datasets().forEach($->add($));
	}
	
	public void add(Dataset dataset) {
		
		log.info("creating schema for dataset {}",dataset.id());
		
		StringBuilder ddl = new StringBuilder();
		
		if (clean)
			ddl.append(format("drop schema if exists %s cascade;",dataset.id()));
		
		ddl.append("\n").append(format("create schema if not exists %s;",dataset.id()));
		
		ddl.append("\n").append(format("set search_path = %s;",dataset.id()));
		
		
		ddl.append("\n").append("create table observation ("
				+ "id bigserial not null"
				+ ",observation_coordinates bigint not null"
				+ ",version integer not null"
				+ ",value numeric(30,6)"
				+ ",flag_obs_status character(3)"
				+ ",flag_method character(3)"
				+ ",created_on timestamp without time zone default now() not null"
				+ ",created_by integer not null"
				+ ",replaced_on timestamp without time zone"
				+ ",primary key (id))"
				+ ";"); 
		
		ddl.append("\n").append("create table metadata ("
				+ "id bigserial not null"
				+ ",observation bigint not null"
				+ ",metadata_type integer not null"
				+ ",language integer not null"
				+ ",copy_metadata bigint"
				+ ",primary key (id))"
				+ ";")
				
			.append("\n")
				.append("create index on metadata using btree (observation);")
			.append("\n")
				.append("alter table only metadata add foreign key (metadata_type) references reference_data.metadata_type(id);")
			.append("\n")
				.append("alter table only metadata add foreign key (observation) references observation(id);")
			;  
		
		
//		ddl.append("\n").append("create table metadata_element ("
//				+ "id bigserial not null"
//				+ ",metadata integer not null"
//				+ ",metadata_element_type integer"
//				+ ",value character varying(500)"
//				+ ",primary key (id))"
//				+ ";")
//				
//			.append("\n")
//				.append("create index on metadata_element using btree (metadata);")
//			.append("\n")
//				.append("alter table only metadata_element add foreign key (metadata_element_type) references reference_data.metadata_element_type(id)" )
//			.append("\n")
//				.append("alter table only metadata_element add foreign key (metadata) references metadata(id)")
//			;   
		 		
		jooq.execute(ddl.toString());
		
	}
	
	
	
	@Override @SneakyThrows
	public void close() {
		
		log.info("closing connection");
		
		@Cleanup 
		Connection autoclose = conn;
		
	}
	
	//////////////////////////////////////////////////////////////////// helpers
	

}
