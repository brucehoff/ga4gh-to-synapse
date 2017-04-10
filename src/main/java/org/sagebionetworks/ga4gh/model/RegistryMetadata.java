package org.sagebionetworks.ga4gh.model;

public class RegistryMetadata {
	private String version;
	private String country;
	private String friendlyName;
	
	public RegistryMetadata(String version, String country, String friendlyName) {
		super();
		this.version = version;
		this.country = country;
		this.friendlyName = friendlyName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getFriendlyName() {
		return friendlyName;
	}
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	
	



}
