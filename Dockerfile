FROM jboss/wildfly
ADD rest-services/target/sample.war /opt/jboss/wildfly/standalone/deployments/ROOT.war
ADD src/test/h2-data/account.h2.db src/test/h2-data/customer.h2.db src/test/h2-data/location.h2.db /opt/jboss/wildfly/standalone/
ADD scripts/standalone.xml /opt/jboss/wildfly/standalone/configuration
EXPOSE 8080
WORKDIR /opt/jboss/wildfly/standalone
