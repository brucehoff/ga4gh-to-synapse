package org.sagebionetworks.ga4gh.util;

import static org.sagebionetworks.client.SynapseClient.COUNT_PARTMASK;
import static org.sagebionetworks.client.SynapseClient.QUERY_PARTMASK;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseResultNotReadyException;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;

public class Util {
	public static final String REGISTRY = "https://www.synapse.org";
	
    private static final long QUERY_PAGE_SIZE = 100;
    public static final long UPLOAD_ROWS_TIMEOUT_MILLIS = 1000L*300; // five minutes
    
    public static final String TABLE_NAME_TOOL = "tool";
    public static final String TABLE_NAME_TOOL_TYPE = "toolType";
    public static final String TABLE_NAME_TOOL_VERSION = "toolVersion";
    
    
    // columns for 'tool' table
    public static final String TOOL_ID="toolId";
    public static final String AUTHOR="author";
    public static final String CONTAINS="contains";
    public static final String DESCRIPTION="description";
    public static final String ORGANIZATION="organization";
    public static final String NAME="name";
    public static final String TOOL_NAME="toolname";
    public static final String TOOL_TYPE_ID="toolTypeId";
    public static final String META_VERSION="metaVersion";
    
    // columns for 'toolType' table
    public static final String TYPE_ID = "id";
    public static final String TYPE_NAME = "name";
    public static final String TYPE_DESCRIPTION = "description";
    
    // columns for 'toolVersion' table
    public static final String VERSION = "version";
    public static final String IMAGE = "image";
    public static final String DESCRIPTOR_DESCRIPTION = "descriptorDescription";
    public static final String DESCRIPTOR_FILE = "descriptorFile";
    public static final String DOCKERFILE_DESCRIPTION = "dockerfileDescription";
    public static final String DOCKERFILE = "dockerfileFile";

	private SynapseClient synapse;

	public static Map<String,Integer> columnNameToIndexMap(List<SelectColumn> columns)  {
		Map<String,Integer> result = new HashMap<String,Integer>();
		for (int i=0; i<columns.size(); i++) {
			result.put(columns.get(i).getName(), i);
		}
		return result;
	}

	public Util(SynapseClient synapse) {
		this.synapse=synapse;
	}
	
    public Map<String,String> getTables(String projectId) throws SynapseException {
    	if (StringUtils.isEmpty(projectId)) throw new IllegalArgumentException("projectId is required.");
		try {
			String queryString = "select name, id from table where parentId==\""+projectId+"\"";
			JSONObject results = synapse.query(queryString);
			JSONArray tables = results.getJSONArray("results");
			Map<String,String> result = new HashMap<String,String>();
			for (int i=0; i<tables.length(); i++) {
				result.put(
						tables.getJSONObject(i).getString("table.name"),
						tables.getJSONObject(i).getString("table.id")
				);
			}
			System.out.println("getTables("+projectId+") result: "+result);
			return result;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
    }
    
    public RowSet executeQuery(String tableId, String query) throws SynapseException {
    	int mask = 	QUERY_PARTMASK |  COUNT_PARTMASK ;
    	RowSet result = new RowSet();
     	long queryCount = Long.MAX_VALUE;
       	for (long offset=0; offset<queryCount; offset+=QUERY_PAGE_SIZE) {
       		String jobToken = synapse.queryTableEntityBundleAsyncStart(
	    			query, offset, QUERY_PAGE_SIZE, true, mask, tableId);
			QueryResultBundle qrb=null;
			long backoff = 100L; // initial back off, millisec
			for (int i=0; i<20; i++) {
				try {
					qrb = synapse.queryTableEntityBundleAsyncGet(jobToken, tableId);
					break;
				} catch (SynapseResultNotReadyException e) {
					// keep waiting
					try {
						Thread.sleep(backoff);
					} catch (InterruptedException ie) {
						break;
					}
					backoff *=2L; // use exponential backoff
				}
			}
			if (qrb==null) throw new RuntimeException("Query failed to return: "+query);
			queryCount = qrb.getQueryCount();
			RowSet pageResult = qrb.getQueryResult().getQueryResults();
			pageResult.getHeaders();
			pageResult.getRows();
			pageResult.getTableId();
			if (result.getHeaders()==null) {
				result.setHeaders(pageResult.getHeaders());
			} else {
				if (!result.getHeaders().equals(pageResult.getHeaders())) 
					throw new IllegalStateException("Inconsistent headers between pages.");
			}
			if (result.getTableId()==null) {
				result.setTableId(pageResult.getTableId());
			} else {
				if (!result.getTableId().equals(pageResult.getTableId())) 
					throw new IllegalStateException("Inconsistent Table ID between pages.");
			}
			if (result.getRows()==null) result.setRows(new ArrayList<Row>());
			result.getRows().addAll(pageResult.getRows());
       	}
       	return result;
    }
    
    /*
     * get the download url for a given entityId
     */
    public String getUrlForEntityId(String entityId) throws ClientProtocolException, MalformedURLException, IOException, SynapseException {
    	return synapse.getFileEntityTemporaryUrlForCurrentVersion(entityId).toString();
    }
}
