package org.sagebionetworks.ga4gh.controller;

import static org.sagebionetworks.ga4gh.util.Util.AUTHOR;
import static org.sagebionetworks.ga4gh.util.Util.CONTAINS;
import static org.sagebionetworks.ga4gh.util.Util.DESCRIPTION;
import static org.sagebionetworks.ga4gh.util.Util.NAME;
import static org.sagebionetworks.ga4gh.util.Util.ORGANIZATION;
import static org.sagebionetworks.ga4gh.util.Util.TABLE_NAME_TOOL;
import static org.sagebionetworks.ga4gh.util.Util.TOOL_ID;
import static org.sagebionetworks.ga4gh.util.Util.TOOL_NAME;
import static org.sagebionetworks.ga4gh.util.Util.VERSION;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.ga4gh.model.Credentials;
import org.sagebionetworks.ga4gh.model.ErrorResponse;
import org.sagebionetworks.ga4gh.model.RegistryMetadata;
import org.sagebionetworks.ga4gh.model.SessionToken;
import org.sagebionetworks.ga4gh.model.Tool;
import org.sagebionetworks.ga4gh.model.ToolDescriptor;
import org.sagebionetworks.ga4gh.model.ToolDockerfile;
import org.sagebionetworks.ga4gh.model.ToolType;
import org.sagebionetworks.ga4gh.model.ToolVersion;
import org.sagebionetworks.ga4gh.util.SynapseClientFactory;
import org.sagebionetworks.ga4gh.util.Util;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/{projectId}/api/v1")
public class ToolDiscoveryController {
	
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody
	ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
		return error;
	}

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public @ResponseBody
	ErrorResponse handleNotFoundException(NotFoundException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
		return error;
	}

	@RequestMapping(value = "/session", method = RequestMethod.POST)
	public @ResponseBody SessionToken
	authenticate(
    		@RequestBody Credentials credentials
			) throws Exception {
		SynapseClient synapse = SynapseClientFactory.createSynapseClient();
    	LoginRequest loginRequest = new LoginRequest();
    	loginRequest.setUsername(credentials.getUsername());
    	loginRequest.setPassword(credentials.getPassword());
    	LoginResponse loginResponse = synapse.login(loginRequest);
    	SessionToken response = new SessionToken();
    	response.setSessionToken(loginResponse.getSessionToken());
    	return response;
	}
	
	private static SynapseClient createSynapseClient(String sessionToken) {
		SynapseClient synapse = SynapseClientFactory.createSynapseClient();
		synapse.setSessionToken(sessionToken);
		return synapse;
	}
	
	private static final String REGISTRY = "https://www.synapse.org";
	
	private static Tool createTool(String tableId, Row row, List<SelectColumn> rowHeaders) {
		List<String> values = row.getValues();
		Map<String,Integer> columnNameToIndexMap = Util.columnNameToIndexMap(rowHeaders);
		String toolId = values.get(columnNameToIndexMap.get(TOOL_ID));
		Tool result = new Tool();
		result.setRegistryId(toolId);
		result.setGlobalId(REGISTRY+"/"+tableId+"/"+toolId); // TODO revisit this
		result.setAuthor(values.get(columnNameToIndexMap.get(AUTHOR)));
		result.setContains(values.get(columnNameToIndexMap.get(CONTAINS)));
		result.setDescription(values.get(columnNameToIndexMap.get(DESCRIPTION)));
		result.setMetaVersion(values.get(columnNameToIndexMap.get(VERSION)));
		result.setName(values.get(columnNameToIndexMap.get(NAME)));
		result.setOrganization(values.get(columnNameToIndexMap.get(ORGANIZATION)));
		result.setRegistry(REGISTRY);
		result.setToolname(values.get(columnNameToIndexMap.get(TOOL_NAME)));
		ToolType toolType = new ToolType(); // TODO 
		result.setTooltype(toolType);
		List<String> versions = new ArrayList<String>(); // TODO
		result.setVersions(versions);
		return result;
	}

	@RequestMapping(value = "/tools/{registry-id}", method = RequestMethod.GET)
    public @ResponseBody Tool 
    getTool(
    		@PathVariable(value="projectId", required=true) String projectId,
    		@PathVariable(value="registry-id", required=true) String toolId,
    		@RequestHeader(value="sessionToken", required=true) String sessionToken
    		    		) throws Exception {
		SynapseClient synapseClient = createSynapseClient(sessionToken);
		Util util = new Util(synapseClient);
		Map<String,String> tableNameToIdMap = util.getTables(projectId);
		String toolTableId = tableNameToIdMap.get(TABLE_NAME_TOOL);
		RowSet rowSet = util.executeQuery(toolTableId, "select * from "+toolTableId+" where toolId='"+toolId+"'");
		int n = rowSet.getRows().size();
		if (n==0) throw new NotFoundException("Unable to find tool: "+toolId);
		
		return createTool(projectId, rowSet.getRows().get(n-1), rowSet.getHeaders());
    }
	
	@RequestMapping(value = "/tools/{registry-id}/version/{version-id}", method = RequestMethod.GET)
	public @ResponseBody ToolVersion
	getToolVersion(
    		@PathVariable(value="registry-id", required=true) String toolId,
    		@PathVariable(value="version-id", required=true) String version
    		) {
		ToolVersion toolVersion = new ToolVersion();
		// TODO populate toolVersion
		return toolVersion;
	}

	// TODO will this work or do we need to define a ToolList POJO???
	@RequestMapping(value = "/tools", method = RequestMethod.GET)
    public @ResponseBody List<Tool> 
    listTools() {
    	List<Tool> tools = new ArrayList<Tool>();
    	// TODO populate tools
        return tools;
    }
	
	@RequestMapping(value = "/tools/{registry-id}/version/{version-id}/descriptor", method = RequestMethod.GET)
	public @ResponseBody ToolDescriptor
	getToolDescriptor(
    		@PathVariable(value="registry-id", required=true) String toolId,
    		@PathVariable(value="version-id", required=true) String version
    		) {
		ToolDescriptor toolDescriptor = new ToolDescriptor();
		// TODO populate toolDescriptor
		return toolDescriptor;
	}

	@RequestMapping(value = "/tools/{registry-id}/version/{version-id}/dockerfile", method = RequestMethod.GET)
	public @ResponseBody ToolDockerfile
	getToolDockerfile(
    		@PathVariable(value="registry-id", required=true) String toolId,
    		@PathVariable(value="version-id", required=true) String version
    		) {
		ToolDockerfile toolDockerfile = new ToolDockerfile();
		// TODO populate toolDockerfile
		return toolDockerfile;
	}
	
	@RequestMapping(value = "/tools/metadata", method = RequestMethod.GET)
    public @ResponseBody RegistryMetadata 
    getRegistryMetadata() {
		RegistryMetadata metadata = new RegistryMetadata("v1", "us", "Synapse Tool Registry");
        return metadata;
    }
	


}
