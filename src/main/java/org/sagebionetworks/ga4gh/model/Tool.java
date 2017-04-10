package org.sagebionetworks.ga4gh.model;

import java.util.List;

public class Tool {

	private String globalId;
	private String registryId;
	private String registry;
	private String organization;
	private String name;
	private String toolname;
	private ToolType tooltype;
	private String description;
	private String author;
	private String metaVersion;
	private String contains;
	private List<String> versions;

	public Tool() {
	}

	public String getGlobalId() {
		return globalId;
	}

	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}

	public String getRegistryId() {
		return registryId;
	}

	public void setRegistryId(String registryId) {
		this.registryId = registryId;
	}

	public String getRegistry() {
		return registry;
	}

	public void setRegistry(String registry) {
		this.registry = registry;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getToolname() {
		return toolname;
	}

	public void setToolname(String toolname) {
		this.toolname = toolname;
	}

	public ToolType getTooltype() {
		return tooltype;
	}

	public void setTooltype(ToolType tooltype) {
		this.tooltype = tooltype;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getMetaVersion() {
		return metaVersion;
	}

	public void setMetaVersion(String metaVersion) {
		this.metaVersion = metaVersion;
	}

	public String getContains() {
		return contains;
	}

	public void setContains(String contains) {
		this.contains = contains;
	}

	public List<String> getVersions() {
		return versions;
	}

	public void setVersions(List<String> versions) {
		this.versions = versions;
	}
	

}
