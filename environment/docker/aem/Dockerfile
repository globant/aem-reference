FROM posti/oracle-java7

# Upload docker context files
RUN mkdir -p /opt/aem
RUN mkdir -p /opt/aem/crx-quickstart/install

WORKDIR /opt/aem
ADD AEM_6.1_Quickstart.jar AEM_6.1_Quickstart.jar
ADD license.properties license.properties
ADD crx-quickstart/bin/docker-start crx-quickstart/bin/docker-start
##ADD aemInstaller.py
#ADD hotfix/* /opt/aem/crx-quickstart/install/

# Install AEM
RUN java -jar AEM_6.1_Quickstart.jar -unpack

ENTRYPOINT crx-quickstart/bin/docker-start
