package org.fao.sws.automation.dsl;

import static com.github.jknack.handlebars.Context.*;

import java.util.HashMap;
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
		
		Context ctx = newBuilder(modelOf(params))
						.resolver(MapValueResolver.INSTANCE,
								  FieldValueResolver.INSTANCE)
						.build();
		
		return bars.compile(name).apply(ctx);
	}
	
	
	//////////////////////////////////////////// helper(s)
	
	private Map<String,Object> modelOf(Object[] ... params) {
		
		Map<String,Object> model = new HashMap<>();
		
		for (Object[] param : params)
			model.put(param[0].toString(),param[1]);
		
		return model;
		
	}
}
