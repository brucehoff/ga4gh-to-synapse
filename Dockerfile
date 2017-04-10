FROM maven:3.3-jdk-8-onbuild
# ^This already calls 'RUN mvn install' so we don't have to 

ENV entryPoint=org.sagebionetworks.ga4ghtosynapse.GA4GHToSynapseConfiguration
CMD exec mvn exec:java