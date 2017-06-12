package org.giste.spring.server.dto;

import javax.validation.constraints.NotNull;

import org.giste.util.dto.NonRemovableDto;

public class InstanceDto extends NonRemovableDto {

	private static final long serialVersionUID = -8188484993248919702L;

	@NotNull
	private String name;

	@NotNull
	private String path;

	public InstanceDto() {

	}

	public InstanceDto(Long id, boolean enabled, String name, String path) {
		super(id, enabled);
		this.name = name;
		this.path = path;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

}
