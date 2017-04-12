package org.sagebionetworks.ga4gh.model;

public class ToolDockerfile {
	private String dockerfile;
	private String url;

	public ToolDockerfile() {
	}

	public String getDockerfile() {
		return dockerfile;
	}

	public void setDockerfile(String dockerfile) {
		this.dockerfile = dockerfile;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
