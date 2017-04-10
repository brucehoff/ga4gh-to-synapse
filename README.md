# ga4gh-to-synapse
Web services to implement the GA4GH APIs using Synapse (www.synapse.org) as the back end.

The API implemented is described here:
http://editor.swagger.io/#/?import=https://raw.githubusercontent.com/ga4gh/tool-registry-schemas/develop/src/main/resources/swagger/ga4gh-tool-discovery.yaml

and is based on this source:
https://github.com/ga4gh/tool-registry-schemas

The implementation is a set of Synapse tables:
tool:
- resistryId
- organization
- name
- toolname
- tooltypeId - reference to toolType table
- description
- author
- meta-version
- contains

toolVersion:
- resistryId
- version
- image - Docker repository name, including version/commit
- descriptorDescription
- descriptorFile (Synapse File Handle)
- dockerfileDescription 
- dockerfileFile (Synapse File Handle)

toolType:
- name
- description

This code began with Spring's getting started demo, found here: https://github.com/spring-guides/gs-actuator-service