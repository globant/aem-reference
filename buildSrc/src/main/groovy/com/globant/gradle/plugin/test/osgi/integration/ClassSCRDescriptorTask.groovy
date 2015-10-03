package com.globant.gradle.plugin.test.osgi.integration

import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.apache.felix.scrplugin.Source
import org.apache.felix.scrplugin.ant.SCRDescriptorTask
import org.apache.tools.ant.types.FileSet
import org.apache.tools.ant.types.Resource
import org.apache.tools.ant.types.resources.FileResource

/**
 * Extends <code>SCRDescriptorTask</code> to generate a service descriptor file based
 * on annotations found in the class files.
 */
@TypeChecked
class ClassSCRDescriptorTask extends SCRDescriptorTask {

    @TypeChecked(TypeCheckingMode.SKIP)
    protected Collection<Source> getSourceFiles(final FileSet sourceFiles) {
        final prefix = sourceFiles.dir.absolutePath
        final prefixLength = prefix.length() + 1

        sourceFiles.inject([] as List<Source>) { List<Source> result, Resource sourceFile ->
            if (sourceFile instanceof FileResource) {
                final file = sourceFile.file
                if (file.name.endsWith('.class')) {
                    result << new Source() {

                        public File getFile() {
                            file
                        }


                        public String getClassName() {
                            final relativeFilename = file.absolutePath.substring(prefixLength)
                            final normalized = relativeFilename.replace(File.separatorChar, '/' as char)
                            String name = normalized.replace('/' as char, '.' as char)
                            name[0..-7]
                        }
                    }
                }
            }
            result
        }
    }

}
