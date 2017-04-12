package org.sagebionetworks.ga4gh.controller;

import static org.sagebionetworks.ga4gh.util.Util.AUTHOR;
import static org.sagebionetworks.ga4gh.util.Util.CONTAINS;
import static org.sagebionetworks.ga4gh.util.Util.DESCRIPTION;
import static org.sagebionetworks.ga4gh.util.Util.DESCRIPTOR_DESCRIPTION;
import static org.sagebionetworks.ga4gh.util.Util.DESCRIPTOR_FILE;
import static org.sagebionetworks.ga4gh.util.Util.DOCKERFILE;
import static org.sagebionetworks.ga4gh.util.Util.DOCKERFILE_DESCRIPTION;
import static org.sagebionetworks.ga4gh.util.Util.IMAGE;
import static org.sagebionetworks.ga4gh.util.Util.META_VERSION;
import static org.sagebionetworks.ga4gh.util.Util.NAME;
import static org.sagebionetworks.ga4gh.util.Util.ORGANIZATION;
import static org.sagebionetworks.ga4gh.util.Util.REGISTRY;
import static org.sagebionetworks.ga4gh.util.Util.TABLE_NAME_TOOL;
import static org.sagebionetworks.ga4gh.util.Util.TABLE_NAME_TOOL_TYPE;
import static org.sagebionetworks.ga4gh.util.Util.TABLE_NAME_TOOL_VERSION;
import static org.sagebionetworks.ga4gh.util.Util.TOOL_ID;
import static org.sagebionetworks.ga4gh.util.Util.TOOL_NAME;
import static org.sagebionetworks.ga4gh.util.Util.TOOL_TYPE_ID;
import static org.sagebionetworks.ga4gh.util.Util.TYPE_DESCRIPTION;
import static org.sagebionetworks.ga4gh.util.Util.TYPE_ID;
import static org.sagebionetworks.ga4gh.util.Util.TYPE_NAME;
import static org.sagebionetworks.ga4gh.util.Util.VERSION;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
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
import org.sagebionetworks.repo.model.Project;
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
		ex.printStackTrace();
		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
		return error;
	}

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public @ResponseBody
	ErrorResponse handleNotFoundException(NotFoundException ex,
			HttpServletRequest request) {
		ex.printStackTrace();
		ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
		return error;
	}

	@ExceptionHandler(SynapseNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public @ResponseBody
	ErrorResponse handleSynapseNotFoundException(NotFoundException ex,
			HttpServletRequest request) {
		ex.printStackTrace();
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
	
	private static String gobalId(String projectId, String toolId) {return REGISTRY+"/"+projectId+"/"+toolId;}
	
	private static Tool createTool(String projectId, 
			Row toolRow, List<SelectColumn> toolRowHeaders,
			List<Row> toolVersions, List<SelectColumn> toolVersionRowHeaders,
			List<Row> toolTypes, List<SelectColumn> toolTypeRowHeaders) {
		List<String> toolRowValues = toolRow.getValues();
		Map<String,Integer> toolColumnNameToIndexMap = Util.columnNameToIndexMap(toolRowHeaders);
		Map<String,Integer> toolTypeColumnNameToIndexMap = Util.columnNameToIndexMap(toolTypeRowHeaders);
		Map<String,Integer> toolVersionColumnNameToIndexMap = Util.columnNameToIndexMap(toolVersionRowHeaders);
		String toolId = toolRowValues.get(toolColumnNameToIndexMap.get(TOOL_ID));
		String metaVersion = toolRowValues.get(toolColumnNameToIndexMap.get(META_VERSION));
		Tool result = new Tool();
		result.setRegistryId(toolId);
		result.setGlobalId(gobalId(projectId, toolId));
		result.setAuthor(toolRowValues.get(toolColumnNameToIndexMap.get(AUTHOR)));
		result.setContains(toolRowValues.get(toolColumnNameToIndexMap.get(CONTAINS)));
		result.setDescription(toolRowValues.get(toolColumnNameToIndexMap.get(DESCRIPTION)));
		result.setMetaVersion(metaVersion);
		result.setName(toolRowValues.get(toolColumnNameToIndexMap.get(NAME)));
		result.setOrganization(toolRowValues.get(toolColumnNameToIndexMap.get(ORGANIZATION)));
		result.setRegistry(REGISTRY);
		result.setToolname(toolRowValues.get(toolColumnNameToIndexMap.get(TOOL_NAME)));
		ToolType toolType = new ToolType();
		boolean foundToolType = false;
		String toolTypeId = toolRowValues.get(toolColumnNameToIndexMap.get(TOOL_TYPE_ID));
		for (Row toolTypeRow : toolTypes) {
			List<String> toolTypeValues = toolTypeRow.getValues();
			String typeId = toolTypeValues.get(toolTypeColumnNameToIndexMap.get(TYPE_ID));
			if (typeId.equals(toolTypeId)) {
				foundToolType=true;
				toolType.setName(toolTypeValues.get(toolTypeColumnNameToIndexMap.get(TYPE_NAME)));
				toolType.setDescription(toolTypeValues.get(toolTypeColumnNameToIndexMap.get(TYPE_DESCRIPTION)));
			}
		}
		if (!foundToolType) throw new IllegalStateException(
				"Cannot find tool type "+toolTypeId+" for tool "+result.getRegistryId());
		result.setTooltype(toolType);
		List<String> versions = new ArrayList<String>();
		for (Row versionRow : toolVersions) {
			List<String> toolVersionValues = versionRow.getValues();
			if (toolId.equals(toolVersionValues.get(toolVersionColumnNameToIndexMap.get(TOOL_ID))) &&
				metaVersion.equals(toolVersionValues.get(toolVersionColumnNameToIndexMap.get(META_VERSION)))) {
				versions.add(toolVersionValues.get(toolVersionColumnNameToIndexMap.get(VERSION)));
			}
		}
		result.setVersions(versions);
		return result;
	}
	
	private static void validateProjectId(SynapseClient synapseClient, String projectId) throws SynapseException {
		synapseClient.getEntity(projectId, Project.class);
	}
	
	private static Util connectToSynapse(String sessionToken, String projectId) throws Exception {
		SynapseClient synapseClient = createSynapseClient(sessionToken);		
		validateProjectId(synapseClient, projectId);
		
		return new Util(synapseClient);		
	}

	@RequestMapping(value = "/tools", method = RequestMethod.GET)
    public @ResponseBody List<Tool> 
    listTools(
    		@PathVariable(value="projectId", required=true) String projectId,
    		@RequestHeader(value="sessionToken", required=true) String sessionToken
    		) throws Exception {
		Util util = connectToSynapse(sessionToken, projectId);
		Map<String,String> tableNameToIdMap = util.getTables(projectId);
		
		String toolTableId = tableNameToIdMap.get(TABLE_NAME_TOOL);
		if (toolTableId==null) throw new IllegalArgumentException("No table "+TABLE_NAME_TOOL+" in project "+projectId);
		RowSet toolRowSet = util.executeQuery(toolTableId, "select * from "+toolTableId);
		
		String toolVersionTableId = tableNameToIdMap.get(TABLE_NAME_TOOL_VERSION);
		if (toolVersionTableId==null) throw new IllegalArgumentException("No table "+TABLE_NAME_TOOL_VERSION+" in project "+projectId);
		RowSet toolVersionRowSet = util.executeQuery(toolVersionTableId, "select * from "+toolVersionTableId);
		
		String toolTypeTableId = tableNameToIdMap.get(TABLE_NAME_TOOL_TYPE);
		if (toolTypeTableId==null) throw new IllegalArgumentException("No table "+TABLE_NAME_TOOL_TYPE+" in project "+projectId);
		RowSet toolTypeRowSet = util.executeQuery(toolTableId, "select * from "+toolTypeTableId);
		
    	List<Tool> tools = new ArrayList<Tool>();
    	for (Row row : toolRowSet.getRows()) {
    		tools.add(createTool(projectId, 
    				row, toolRowSet.getHeaders(),
    				toolVersionRowSet.getRows(), toolVersionRowSet.getHeaders(),
    				toolTypeRowSet.getRows(), toolTypeRowSet.getHeaders()
    				));
    	}
        return tools;
    }
	
	@RequestMapping(value = "/tools/{registry-id}", method = RequestMethod.GET)
    public @ResponseBody Tool 
    getTool(
    		@PathVariable(value="projectId", required=true) String projectId,
    		@PathVariable(value="registry-id", required=true) String toolId,
    		@RequestHeader(value="sessionToken", required=true) String sessionToken
    		    		) throws Exception {
		Util util = connectToSynapse(sessionToken, projectId);
		Map<String,String> tableNameToIdMap = util.getTables(projectId);
		
		String toolTableId = tableNameToIdMap.get(TABLE_NAME_TOOL);
		if (toolTableId==null) throw new IllegalArgumentException("No table "+TABLE_NAME_TOOL+" in project "+projectId);
		RowSet toolTableRows = util.executeQuery(toolTableId, "select * from "+toolTableId+" where toolId='"+toolId+"'");
		int n = toolTableRows.getRows().size();
		if (n==0) throw new NotFoundException("Unable to find tool: "+toolId);
		
		String toolVersionTableId = tableNameToIdMap.get(TABLE_NAME_TOOL_VERSION);
		if (toolVersionTableId==null) throw new IllegalArgumentException("No table "+TABLE_NAME_TOOL_VERSION+" in project "+projectId);
		RowSet toolVersionRowSet = util.executeQuery(toolVersionTableId, "select * from "+toolVersionTableId);
		
		String toolTypeTableId = tableNameToIdMap.get(TABLE_NAME_TOOL_TYPE);
		if (toolTypeTableId==null) throw new IllegalArgumentException("No table "+TABLE_NAME_TOOL_TYPE+" in project "+projectId);
		RowSet toolTypeRowSet = util.executeQuery(toolTableId, "select * from "+toolTypeTableId);
		
		return createTool(projectId, 
				toolTableRows.getRows().get(n-1), toolTableRows.getHeaders(),
				toolVersionRowSet.getRows(), toolVersionRowSet.getHeaders(),
				toolTypeRowSet.getRows(), toolTypeRowSet.getHeaders()
				);
    }
	
	private static ToolVersion createToolVersion(String projectId, 
			List<Row> toolRows, List<SelectColumn> toolRowHeaders,
			Row toolVersion, List<SelectColumn> toolVersionRowHeaders, Util util) throws Exception {
		List<String> toolVersionRowValues = toolVersion.getValues();
		Map<String,Integer> toolColumnNameToIndexMap = Util.columnNameToIndexMap(toolRowHeaders);
		Map<String,Integer> toolVersionColumnNameToIndexMap = Util.columnNameToIndexMap(toolVersionRowHeaders);
		String toolId = toolVersionRowValues.get(toolVersionColumnNameToIndexMap.get(TOOL_ID));
		String metaVersion = toolVersionRowValues.get(toolVersionColumnNameToIndexMap.get(META_VERSION));
		
		// find the right 'meta version'
		List<String> toolRowValues = null;
		for (Row toolRow : toolRows) {
			List<String> values = toolRow.getValues();
			if (toolId.equals(values.get(toolColumnNameToIndexMap.get(TOOL_ID))) &&
					metaVersion.equals(values.get(toolColumnNameToIndexMap.get(META_VERSION))) ) {
				toolRowValues = values;
			}
		}
		if (toolRowValues==null) throw new IllegalStateException("Unable to find metadata for tool "+toolId+" and meta-version"+metaVersion);
		
		ToolVersion result = new ToolVersion();
		result.setGobalId(gobalId(projectId, toolId));
		result.setImage(toolVersionRowValues.get(toolVersionColumnNameToIndexMap.get(IMAGE)));
		result.setMetaVersion(metaVersion);
		result.setName(toolRowValues.get(toolColumnNameToIndexMap.get(NAME)));
		result.setRegistryId(toolId);
		ToolDescriptor toolDescriptor = new ToolDescriptor();
		result.setToolDescriptor(toolDescriptor);
		toolDescriptor.setDescriptor(toolVersionRowValues.get(toolVersionColumnNameToIndexMap.get(DESCRIPTOR_DESCRIPTION)));
		String toolDescriptorEntityId = toolVersionRowValues.get(toolVersionColumnNameToIndexMap.get(DESCRIPTOR_FILE));
		toolDescriptor.setUrl(util.getUrlForEntityId(toolDescriptorEntityId));
		ToolDockerfile toolDockerfile = new ToolDockerfile();
		result.setToolDockerfile(toolDockerfile);
		toolDockerfile.setDockerfile(toolVersionRowValues.get(toolVersionColumnNameToIndexMap.get(DOCKERFILE_DESCRIPTION)));
		String toolDockerfileEntityId = toolVersionRowValues.get(toolVersionColumnNameToIndexMap.get(DOCKERFILE));
		toolDockerfile.setUrl(util.getUrlForEntityId(toolDockerfileEntityId));
		
		return result;
	}
	
	@RequestMapping(value = "/tools/{registry-id}/version/{version-id}", method = RequestMethod.GET)
	public @ResponseBody ToolVersion
	getToolVersion(
    		@PathVariable(value="projectId", required=true) String projectId,
    		@PathVariable(value="registry-id", required=true) String toolId,
    		@PathVariable(value="version-id", required=true) String version,
    		@RequestHeader(value="sessionToken", required=true) String sessionToken
    		) throws Exception {
		Util util = connectToSynapse(sessionToken, projectId);
		Map<String,String> tableNameToIdMap = util.getTables(projectId);
		
		String toolTableId = tableNameToIdMap.get(TABLE_NAME_TOOL);
		if (toolTableId==null) throw new IllegalArgumentException("No table "+TABLE_NAME_TOOL+" in project "+projectId);
		RowSet toolTableRows = util.executeQuery(toolTableId, "select * from "+toolTableId+" where toolId='"+toolId+"'");
		if (toolTableRows.getRows().isEmpty()) throw new NotFoundException("Unable to find tool "+toolId);
		
		String toolVersionTableId = tableNameToIdMap.get(TABLE_NAME_TOOL_VERSION);
		if (toolVersionTableId==null) throw new IllegalArgumentException("No table "+TABLE_NAME_TOOL_VERSION+" in project "+projectId);
		RowSet toolVersionRowSet = util.executeQuery(toolVersionTableId, 
				"select * from "+toolVersionTableId+" where "+TOOL_ID+"='"+toolId+"' and "+
						VERSION+"='"+version+"'");
		int numVersions = toolVersionRowSet.getRows().size();
		if (numVersions==0) throw new NotFoundException("Unable to find version "+version+" of tool "+toolId);
		if (numVersions>1) throw new IllegalStateException("There are "+numVersions+" records of toolId "+toolId+" with version "+version);

		return createToolVersion(projectId, 
				toolTableRows.getRows(), toolTableRows.getHeaders(),
				toolVersionRowSet.getRows().get(0), toolVersionRowSet.getHeaders(),
				util);
	}

	private static ToolDescriptor createToolDescriptor(Row toolVersion, List<SelectColumn> toolVersionRowHeaders, Util util) throws Exception{
		List<String> toolVersionRowValues = toolVersion.getValues();
		Map<String,Integer> toolVersionColumnNameToIndexMap = Util.columnNameToIndexMap(toolVersionRowHeaders);
		
		ToolDescriptor result = new ToolDescriptor();
		result.setDescriptor(toolVersionRowValues.get(toolVersionColumnNameToIndexMap.get(DESCRIPTOR_DESCRIPTION)));
		String toolDescriptorEntityId = toolVersionRowValues.get(toolVersionColumnNameToIndexMap.get(DESCRIPTOR_FILE));
		result.setUrl(util.getUrlForEntityId(toolDescriptorEntityId));

		return result;
	}
	
	@RequestMapping(value = "/tools/{registry-id}/version/{version-id}/descriptor", method = RequestMethod.GET)
	public @ResponseBody ToolDescriptor
	getToolDescriptor(
    		@PathVariable(value="projectId", required=true) String projectId,
    		@PathVariable(value="registry-id", required=true) String toolId,
    		@PathVariable(value="version-id", required=true) String version,
    		@RequestHeader(value="sessionToken", required=true) String sessionToken
    		) throws Exception {
		Util util = connectToSynapse(sessionToken, projectId);
		Map<String,String> tableNameToIdMap = util.getTables(projectId);
		
		String toolVersionTableId = tableNameToIdMap.get(TABLE_NAME_TOOL_VERSION);
		if (toolVersionTableId==null) throw new IllegalArgumentException("No table "+TABLE_NAME_TOOL_VERSION+" in project "+projectId);
		RowSet toolVersionRowSet = util.executeQuery(toolVersionTableId, 
				"select * from "+toolVersionTableId+" where "+TOOL_ID+"='"+toolId+"' and "+
						VERSION+"='"+version+"'");
		int numVersions = toolVersionRowSet.getRows().size();
		if (numVersions==0) throw new NotFoundException("Unable to find version "+version+" of tool "+toolId);
		if (numVersions>1) throw new IllegalStateException("There are "+numVersions+" records of toolId "+toolId+" with version "+version);

		return createToolDescriptor(toolVersionRowSet.getRows().get(0), toolVersionRowSet.getHeaders(), util);
	}

	private static ToolDockerfile createToolDockerfile(Row toolVersion, List<SelectColumn> toolVersionRowHeaders, Util util) throws Exception {
		List<String> toolVersionRowValues = toolVersion.getValues();
		Map<String,Integer> toolVersionColumnNameToIndexMap = Util.columnNameToIndexMap(toolVersionRowHeaders);
		
		ToolDockerfile result = new ToolDockerfile();
		result.setDockerfile(toolVersionRowValues.get(toolVersionColumnNameToIndexMap.get(DESCRIPTOR_DESCRIPTION)));
		String toolDescriptorEntityId = toolVersionRowValues.get(toolVersionColumnNameToIndexMap.get(DESCRIPTOR_FILE));
		result.setUrl(util.getUrlForEntityId(toolDescriptorEntityId));

		return result;
	}
	
	@RequestMapping(value = "/tools/{registry-id}/version/{version-id}/dockerfile", method = RequestMethod.GET)
	public @ResponseBody ToolDockerfile
	getToolDockerfile(
    		@PathVariable(value="projectId", required=true) String projectId,
    		@PathVariable(value="registry-id", required=true) String toolId,
    		@PathVariable(value="version-id", required=true) String version,
    		@RequestHeader(value="sessionToken", required=true) String sessionToken
    		) throws Exception {
		Util util = connectToSynapse(sessionToken, projectId);
		Map<String,String> tableNameToIdMap = util.getTables(projectId);
		
		String toolVersionTableId = tableNameToIdMap.get(TABLE_NAME_TOOL_VERSION);
		if (toolVersionTableId==null) throw new IllegalArgumentException("No table "+TABLE_NAME_TOOL_VERSION+" in project "+projectId);
		RowSet toolVersionRowSet = util.executeQuery(toolVersionTableId, 
				"select * from "+toolVersionTableId+" where "+TOOL_ID+"='"+toolId+"' and "+
						VERSION+"='"+version+"'");
		int numVersions = toolVersionRowSet.getRows().size();
		if (numVersions==0) throw new NotFoundException("Unable to find version "+version+" of tool "+toolId);
		if (numVersions>1) throw new IllegalStateException("There are "+numVersions+" records of toolId "+toolId+" with version "+version);

		return createToolDockerfile(toolVersionRowSet.getRows().get(0), toolVersionRowSet.getHeaders(), util);
	}
	
	@RequestMapping(value = "/tools/metadata", method = RequestMethod.GET)
    public @ResponseBody RegistryMetadata 
    getRegistryMetadata() {
		RegistryMetadata metadata = new RegistryMetadata("v1", "us", "Synapse Tool Registry");
        return metadata;
    }
	


}
