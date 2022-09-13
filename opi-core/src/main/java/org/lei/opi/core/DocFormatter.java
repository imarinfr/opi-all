package org.lei.opi.core;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
 
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
 
/**
 * A minimal doclet that just prints out the names of the
 * selected elements.
 */
public class DocFormatter implements Doclet {

    @Override
    public void init(Locale locale, Reporter reporter) {  }
 
    @Override
    public String getName() {
        // For this doclet, the name of the doclet is just the
        // simple name of the class. The name may be used in
        // messages related to this doclet, such as in command-line
        // help when doclet-specific options are provided.
        return getClass().getSimpleName();
    }
 
    @Override
    public Set<? extends Option> getSupportedOptions() {
        // This doclet does not support any options.
        return Collections.emptySet();
    }
 
    @Override
    public SourceVersion getSupportedSourceVersion() {
        // This doclet supports all source versions.
        // More sophisticated doclets may use a more
        // specific version, to ensure that they do not
        // encounter more recent language features that
        // they may not be able to handle.
        return SourceVersion.latest();
    }
 
    private static final boolean OK = true;
 
    @Override
    public boolean run(DocletEnvironment environment) {
        // This method is called to perform the work of the doclet.
        // In this case, it just prints out the names of the
        // elements specified on the command line.
        environment.getSpecifiedElements()
                .forEach(System.out::println);
        System.out.println("hello");
        return OK;
    }
}