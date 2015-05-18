package org.fao.sws.automation;

import static com.github.jknack.handlebars.Context.*;
import static java.util.Arrays.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;

@UtilityClass
public class Templates {

	private String prefix = "/ddl";

	public String dataset = "";

	private final Handlebars bars = new Handlebars(new ClassPathTemplateLoader(prefix, ".sws"));

	public Object[] $(String key,Object value) {
		return new Object[]{key,value};
	}
		
	@SneakyThrows
	public String instantiate(String name, Object[] ... params) {
		
		return instantiate(name, asList(params));
	}
	
	@SneakyThrows
	public String instantiate(String name, List<Object[]> params) {
		
		Context model = newBuilder(modelOf(params))
						.resolver(MapValueResolver.INSTANCE,
								  FieldValueResolver.INSTANCE)
						.build();
		
		return bars.compile(name).apply(model);
	}
	
	
	//////////////////////////////////////////// helper(s)
	
	private Map<String,Object> modelOf(List<Object[]> params) {
		
		Map<String,Object> model = new HashMap<>();
		
		for (Object[] param : params)
			model.put(param[0].toString(),param[1]);
		
		return model;
		
	}
}
