package com.globant.aem.archetype

import org.gradle.api.Project
import org.gradle.api.Plugin

/**
 * @author andres.postiglioni
 * Created: Tue Sep 08 21:52:58 CDT 2015
 */
class AemArchetypePlugin implements Plugin<Project> {
   void apply (Project project) {
      project.extensions.create("aemarchetype", AemArchetypePluginExtension)

      project.task('createAemModule')  {
        group='Build Setup'
        description='Creates a new AEM module project.'
        doLast {
          def console = System.console()
          def module = console.readLine("> Please enter the module name: ")
          def group = console.readLine("> Please enter the module group: ")
          def moduleVersion = console.readLine("> Please enter the module version: ")
          def packageRoot = console.readLine("> Please enter the root of the package structure: ")
          def packageTree = packageRoot.replace('.', '/')

          def context = [
            group: group,
            module: module,
            version: moduleVersion,
            packageRoot: packageRoot,
            packageTree: packageTree
          ]

          // Copy all files except for templates
          project.copy {
            from project.aemarchetype.src
            into "$project.aemarchetype.dest/$module"
            exclude '**/*.template'
            eachFile { details ->
              context.each { k, v -> details.path = details.path.replaceAll(/\{$k\}/, v) }

              //This is required otherwise the original dir is still created at destination
              //See: https://discuss.gradle.org/t/copyspec-support-for-moving-files-directories/7412/8
              includeEmptyDirs = false
            }
          }

          // Process templates
          project.copy {
            from project.aemarchetype.src
            into "$project.aemarchetype.dest/$module"
            include '**/*.template'
            expand context
            eachFile { details -> details.name = details.name - '.template' }
            eachFile { details ->
              context.each { k, v -> details.path = details.path.replaceAll(/\{$k\}/, v) }

              //This is required otherwise the original dir is still created at destination
              //See: https://discuss.gradle.org/t/copyspec-support-for-moving-files-directories/7412/8
              includeEmptyDirs = false
            }
          }
        }
      }
   }
}

class AemArchetypePluginExtension {
  def src = 'templates/aem-module'
  def dest = 'projects'
}
