package org.sagebionetworks.ga4gh.util;



public interface Executable<T> {
		T execute() throws Throwable;
}
