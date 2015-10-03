package com.globant.gradle.plugin.test.osgi.integration

import org.gradle.api.Plugin
import org.gradle.api.Project
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.util.slurpersupport.NodeChild
import org.apache.tools.ant.types.Path
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.osgi.OsgiManifest
import org.gradle.api.plugins.osgi.OsgiPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar

class OsgiIntegrationTestPlugin implements Plugin<Project> { 
  @SuppressWarnings("GroovyUnusedDeclaration")
  final static String SCR_ANT_VERSION = '1.3.0'

  @SuppressWarnings("GroovyUnusedDeclaration")
  final static String BND_LIB_VERSION = '1.50.0'

  @Override
  void apply(Project project) {
    addJarIntegrationTestTask(project)
    addScrTask(project)
  }

  protected void addJarIntegrationTestTask(Project project) {
    final task = project.task('jarIntegrationTest', type: Jar) {
      group = 'Build'
      description = 'Assembles an osgi bundle containing the integrationTest classes.' 
      baseName = "${project.jar.baseName}-it"
      def osgiConvention = new OsgiPluginConvention(project)
      manifest = osgiConvention.osgiManifest {
        name = baseName
        symbolicName = baseName

        classesDir = project.sourceSets.integrationTest.output.classesDir
        classpath = project.sourceSets.integrationTest.runtimeClasspath
      }
      from project.compileIntegrationTestJava.outputs.files.files.collect { project.fileTree(it) }
    }
  }

  protected void addScrTask(Project project) {
    final task = project.tasks.create('processIntegrationTestScrAnnotations')
    task.with {
      group = 'Build'
      description = 'Processes the Felix SCR service annotations for integration test classes'
      dependsOn 'integrationTestClasses'
      outputs.file new File(itSourceSet(project).output.classesDir, 'OSGI-INF/serviceComponents.xml')
      inputs.source itSourceSet(project).output.classesDir

      doLast {
        configureAction(project)
      }

      project.tasks.getByName('jarIntegrationTest').dependsOn task
    }
  }

  void configureAction(Project project) {
    SourceSet itSourceSet = itSourceSet(project)
    final antProject = project.ant.project
    final classesDir = itSourceSet.output.classesDir
    final runtimeClasspath = itSourceSet.runtimeClasspath
    final runtimePath = runtimeClasspath.asPath

    project.logger.info "Running SCR for ${classesDir}"
    if (classesDir.exists()) {
      final task = new ClassSCRDescriptorTask(
        srcdir: classesDir, 
        destdir: classesDir,
        classpath: new Path(antProject, runtimePath),
        strictMode: true,
        project: antProject
      )

      task.execute()

      addToManifest(project, classesDir)
    }
  }

  void addToManifest(Project project, File resourcesDir) throws InvalidUserDataException {
    final osgiInfDir = new File(resourcesDir, 'OSGI-INF')

    final scFile = new File(osgiInfDir, 'serviceComponents.xml')
    if (scFile.exists()) {
      project.logger.info "Created ${scFile}"
      final jar = (Jar) project.tasks.getByName('jarIntegrationTest')
      final osgiManifest = (OsgiManifest)jar.manifest
      osgiManifest.instruction('Service-Component', 'OSGI-INF/serviceComponents.xml')
      validateReferences(project, scFile)
    } else {
      project.logger.warn "${scFile} was not created"
    }
  }

  private Collection<Map> allComponents(File scFile) {
    return new XmlSlurper().parse(scFile).children().findAll { groovy.util.slurpersupport.NodeChild node ->
      node.name() == 'component'
    } as Collection<Map>
  }

  private void validateReferences(Project project, File scFile) {
    def allComponents = allComponents(scFile)
    def errorMessage = ""

    if (!allComponents) {
      project.logger.warn "No components found in ${scFile}."
      return
    }

    for (component in allComponents) {
      for (references in component.reference) {
        errorMessage = generateErrorMsg(
          errorMessage, 
          references.@interface.text(), 
          component.@name.text(),
          loadClassPaths(project)
        )
      }
    }

    if (errorMessage != "") {
      throw new InvalidUserDataException(errorMessage)
    }
  }

  private String generateErrorMsg(String errorMessage, String interfaceName, String className, ClassLoader classLoader) {
    try {
      def clas = classLoader.loadClass(interfaceName)

      if (!clas.isInterface()) {
        errorMessage += "\n${className} has an @Reference to ${interfaceName}, but it is not an interface."
      }
    } catch (ClassNotFoundException ignored) {
      errorMessage += "\n${className} has an @Reference to ${interfaceName} that could not be found by the class loader."
    }
    errorMessage
  }

  /**
   * load classPaths into class loader
   */
  private ClassLoader loadClassPaths(Project project) throws InvalidUserDataException {
    def classpathURLs = itSourceSet(project).runtimeClasspath.collect { File f -> f.toURI().toURL() }

    if (!classpathURLs) throw new InvalidUserDataException("Runtime class path empty.")

    new URLClassLoader(classpathURLs as URL[])
  }

  public SourceSet itSourceSet(Project project) {
    return project.convention.findPlugin(JavaPluginConvention)?.sourceSets?.getByName('integrationTest')
  }
}
