package com.globant.gradle.plugin.test.java.integration

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.plugins.JavaPlugin

class IntegrationTestPlugin implements Plugin<Project> {
  void apply(Project project) {
    if (project.plugins.hasPlugin(IntegrationTestPlugin.class)) {
      return;
    }

    if (!project.plugins.hasPlugin(JavaPlugin.class)) {
      project.plugins.apply(JavaPlugin.class)
    }

    project.sourceSets {
      integrationTest {
        java {
          compileClasspath += main.output + test.output
          runtimeClasspath += main.output + test.output
          srcDirs = [project.file('src/integration-test/java')]
        }
        resources.srcDirs = [project.file('src/integration-test/resources')]
      }
    }

    project.configurations {
      integrationTestCompile.extendsFrom testCompile
      integrationTestRuntime.extendsFrom testRuntime
    }

    project.with {
      task('integrationTest',
        type: Test,
        group: 'Verification',
        description: 'Runs the integration tests.') {
    
        testClassesDir = project.sourceSets.integrationTest.output.classesDir
        classpath = project.sourceSets.integrationTest.runtimeClasspath
      }
      integrationTest.mustRunAfter test
      check.dependsOn integrationTest

      tasks.withType(Test) {
        reports.html.destination = file("${reporting.baseDir}/${name}")
      }
    }
  }
}
