package org.fao.sws.automation;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static org.fao.sws.automation.Templates.*;
import static org.fao.sws.common.Constants.*;
import static org.jooq.SQLDialect.*;
import static org.jooq.impl.DSL.*;

import java.io.Closeable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.fao.sws.model.Dataset;
import org.fao.sws.model.Dimension;
import org.fao.sws.model.Dimension.Measure;
import org.fao.sws.model.DimensionRef;
import org.fao.sws.model.Domain;
import org.fao.sws.model.Flag;
import org.fao.sws.model.configuration.Configuration;
import org.fao.sws.model.configuration.Validator;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import smallgears.api.tabular.Row;
import smallgears.api.tabular.Table;

@Slf4j
@ApplicationScoped @Alternative @Priority(high)
public class Database implements Closeable {
	 
	private final Connection conn;
	
	private final DSLContext jooq;
	
	private final Validator validator;
	
	@Setter
	private boolean dryrun = false;
	
	public void dryrun() {
		this.dryrun = true;
	}
	
	public Database(@NonNull Connection conn, Validator validator) {
		
		this.conn=conn;
		this.validator=validator;
		this.jooq = using(conn,POSTGRES);
	}
	
	
	public void createSchemaForEntire(@NonNull Configuration configuration) {
		
		if (!validator.valid(configuration))
			throw new IllegalArgumentException("configuration is invalid, will not persist it.");
		
		$createSchemas(configuration);
	}
	
	public void createSchemaFor(@NonNull Configuration configuration) {
		
		if (!validator.validFragment(configuration))
			throw new IllegalArgumentException("configuration is invalid, will not persist it.");
		
		$createSchemas(configuration);
	}
	
	public void createSchemaFor(@NonNull Dimension dim) {
		
		log.info("storing dimension '{}'",dim.id());
		
		List<Object[]> model = new ArrayList<>(asList(
				 $("table",dim.table())
				,$("length",dim.length())
				,$("selectionTable",dim.selectionTable())
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
	
	public void createSchemaFor(@NonNull Flag flag) {
		
		log.info("storing flag '{}'",flag.id());
		
		String ddl = instantiate("flag", 
									 $("table",flag.table())
									,$("length",flag.length())
									);

		log.info("\n\n{}",ddl);
		
		
		if (!dryrun)
			jooq.execute(ddl);
		
	}
	
	public void createSchemasFor(@NonNull Domain domain) {
		
		log.info("storing domain '{}'",domain.id());
		
		domain.datasets().forEach(this::createSchemaFor);
	}
	
	
	public void createSchemaFor(@NonNull Dataset dataset) {
		
		log.info("storing dataset '{}'",dataset.id());
		
		String ddl = instantiate("dataset", 
								 $("schema",dataset.schema())
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
	
	
	public Result<Record> list(String table) {
		
		String query = format("select * from %s",table);
		
		log.info("executing query over {}:\\nn{}",table,query);
		
		return jooq.fetch(query);
	}
	
	
	/**
	 * Populates a dataset from tabular data.
	 * <p>
	 * The table has the same columns as the dimensions of the dataset, and a 'value' with the observations.
	 */
	public void feed(Dataset dataset, Table table) {

		List<DimensionRef> dims = new ArrayList<>(dataset.dimensions().all());
		
		List<String> colnames = dims.stream().map($->$.joinColumn()).collect(toList());
		
		@AllArgsConstructor	@SuppressWarnings("unused")
		class Coord {
			String dim_table,dim_code;
		}
		
		@AllArgsConstructor	@SuppressWarnings("unused")
		class Obs {
			
			String value;
			List<Coord> coords;
		}
		
		
		List<Obs> data = table.stream()
								.filter(row->!row.get("value").isEmpty())
								.map(row-> new Obs(row.get("value"),
														dims.stream()
																.map(t->new Coord(t.target().table(),row.get(t.target().id())))
																.collect(toList()))
										   )
										   .collect(toList());

		String ddl = instantiate("feed-dataset", 
				$("observation_table",dataset.table()),
				$("coordinate_table",dataset.coordinatesTable()),
				$("cols",colnames), 
				$("data",data));

		log.info("feeding {} and {}:\\nn{}",dataset.table(),dataset.coordinatesTable(),ddl);
		
		if(!dryrun)
			jooq.execute(ddl);
	}
	
	/**
	 * Populates a dimension and an associated hierarchy from tabular data.
	 * <p>
	 * The table has the same column as the target table, except for an additional 'parent' column that defines the hierarchy.
	 * @see #insert(Dimension, List, List, List)
	 */
	public void insert(Dimension dim, Table table) {

		//prepare for multiple iterations
		table = table.materialise();
		
		List<List<String>> hierarchy = table
				.stream()
				.filter(root.negate())
				.map(parent_child_pair)
				.collect(toList());
		
		//remove parent col and materialise in view of multiple iterations
		Table no_parent = table.with(row->row.remove("parent")).materialise();
		
		List<String> colnames = no_parent
									.columns()
									.stream()
									.map($->$.name())
									.filter($->!$.equals("parent"))
									.collect(toList());
		
		List<List<String>> data = table
									.stream()
									.map(row->colnames.stream().map(row::get).collect(toList()))
									.collect(toList());
		
		String ddl = instantiate("feed-dimension", 
				$("table",dim.table()),
				$("hierarchy_table",dim.hierarchyTable()),
				$("cols",colnames), 
				$("data",data),
				$("hierarchy",hierarchy),
				$("parent",dim.parent()),
				$("child",dim.child())
				);

		log.info("inserting over {} and {}:\\nn{}",dim.table(),dim.hierarchyTable(),ddl);
		
		if(!dryrun)
			jooq.execute(ddl);
	}
	
	
	@Override @SneakyThrows
	public void close() {
		
		log.info("closing connection");
		
		@Cleanup 
		Connection autoclose = conn;
		
	}
	
	//////////////////////////////////////////////////////////////////// helpers

	Predicate<Row> root = r -> r.getOr("parent","").isEmpty();
	Function<Row,List<String>> parent_child_pair = r->asList(r.get("parent"),r.get("code"));
	
	public void createGroup(@NonNull Group group, @NonNull Dataset dataset) {
		
		log.info("creating group for dataset '{}'",dataset.id());
		
		String ddl = instantiate("group",
				  $("group",group)
				 ,$("schema",dataset.schema())
				 ,$("dimensions",dataset.dimensions().all())
		);
		
		log.info("\n\n{}",ddl);
  
		
		if (!dryrun)
			jooq.execute(ddl);
	}

	void $createSchemas(@NonNull Configuration configuration) {
		
		configuration.dimensions().forEach(this::createSchemaFor);
		
		configuration.flags().forEach(this::createSchemaFor);
		
		configuration.domains().forEach(this::createSchemasFor);
	
	}
	

}
