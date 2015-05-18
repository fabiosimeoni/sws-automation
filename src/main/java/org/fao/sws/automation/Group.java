package org.fao.sws.automation;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data @RequiredArgsConstructor
public class Group {

	@NonNull
	private String name;
	
	private String description;
}
