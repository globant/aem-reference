# Pre-requisites
1. Configure the location of your AEM license file in *build.local.gradle*
2. Install the AEM quickstart JAR in a maven repository with the following coordinates:
```
    groupId:    com.adobe.aem
    artifactId: cq-quickstart
    version:    6.0.0
    classifier: standalone
```
3. Modify your gradle init script (~/.gradle/init.gradle) with the maven repository details you used in step 2.
```groovy
allprojects {
  repositories {
    maven {
      url = 'http://mydomain/nexus/content/groups/public'
    }
  }
}
```
NOTE: Alternative, if you have maven already installed you can install AEM quickstart JAR in your local repository:
```
$ mvn install:install-file -DgroupId=com.adobe.aem -DartifactId=cq-quickstart -Dversion=6.0.0 -Dclassifier=standalone -Dfile=<your_quickstart_jar>
```

# Useful gradle tasks
CQ tasks have been borrowed from the [cq plugin](https://github.com/TWCable/cq-gradle-plugin).
Most commonly used tasks are listed below:

|Command|Description|
|-------|-----------|
|eclipse|Creates an eclipse project ready for install.|
|createPackage|Creates the CQ Package zip file.|
|upload|Uploads the CQ Package.|
|install|Uploads and installs the CQ Package, uninstalling the previous package if it was already installed.|
|verifyBundles|Checks all the JARs that are included in the package to make sure they are OSGi compliant, and gives a report of any that are not. Never causes the build to fail.|
|test|Runs the unit tests.|
|integrationTest|Runs the integration tests. Property **aem.servers.integration.url** can be modified in configuration files to control to which instance the tests will be run against. If the property is not set, a new AEM instance will be set up on-the-fly.|
|check|Runs all checks.|

# Build configuration
Build properties can be configured in two files:
* **build.config.gradle**: This file contains global configuration and defaults. 
* **build.local.gradle**: This file contains configuration overrides applicable only to the developer's environment. Therefore, and as opposed to *build.config.gradle*, this file is not versioned.

Syntax of these file should be compliant with [Groovy's ConfigSlurper](http://mrhaki.blogspot.com/2009/10/groovy-goodness-using-configslurper.html).

## Environment specific configuration
When running gradle tasks, the -Penv parameter can be used to the selected *environment* must match an environment configuration defined in *build.config.gradle* or *build.local.gradle*. 

Example configuration file:
```
// Default configuration
...
aem.servers.integration.with {
  protocol = 'http'
  host = 'localhost'
  port =  4502
  url = "${->protocol}://${->host}:${->port}"
}

// Environment specific overrides
environments {
  dev {
    aem.servers.integration.machineName = 'aem-reference'
  }
}
```

Example gradle execution of integration tests against the *dev* environment:
```sh
$ gradlew -Penv=dev integrationTest
```
