package com.example.plagcheck.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

	private String location = "upload-dir";

	/**
	 * Folder location for storing files
	 */

	public StorageProperties(String location) {

		this.location = location;
	}

	public StorageProperties() {
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
