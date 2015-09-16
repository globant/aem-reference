FROM posti/oracle-java7

# Upload docker context files
RUN mkdir -p /opt/aem
RUN mkdir -p /opt/aem/crx-quickstart/install

WORKDIR /opt/aem
ADD files/6.1/AEM_6.1_Quickstart.jar AEM_6.1_Quickstart.jar
ADD files/6.1/license.properties license.properties
ADD files/6.1/hotfix/* /opt/aem/crx-quickstart/install/
ADD files/crx-quickstart/bin/docker-start crx-quickstart/bin/docker-start
##ADD aemInstaller.py

# Install AEM
RUN java -jar AEM_6.1_Quickstart.jar -unpack

ENTRYPOINT crx-quickstart/bin/docker-start
