package org.sagebionetworks.ga4gh.model;

public class ToolVersion {
	private String name;
	private String gobalId;
	private String registryId;
	private String image;
	private ToolDescriptor toolDescriptor;
	private ToolDockerfile toolDockerfile;
	private String metaVersion;

	public ToolVersion() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGobalId() {
		return gobalId;
	}

	public void setGobalId(String gobalId) {
		this.gobalId = gobalId;
	}

	public String getRegistryId() {
		return registryId;
	}

	public void setRegistryId(String registryId) {
		this.registryId = registryId;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public ToolDescriptor getToolDescriptor() {
		return toolDescriptor;
	}

	public void setToolDescriptor(ToolDescriptor toolDescriptor) {
		this.toolDescriptor = toolDescriptor;
	}

	public ToolDockerfile getToolDockerfile() {
		return toolDockerfile;
	}

	public void setToolDockerfile(ToolDockerfile toolDockerfile) {
		this.toolDockerfile = toolDockerfile;
	}

	public String getMetaVersion() {
		return metaVersion;
	}

	public void setMetaVersion(String metaVersion) {
		this.metaVersion = metaVersion;
	}
	
	

}
