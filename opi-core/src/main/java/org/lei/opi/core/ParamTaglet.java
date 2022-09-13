package org.lei.opi.core;

import jdk.javadoc.doclet.Taglet;
import jdk.javadoc.doclet.Taglet.Location;
import com.sun.source.doctree.DocTree;
import javax.lang.model.element.Element;

import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ParamTaglet implements Taglet {
    
public Set<Taglet.Location> getAllowedLocations() { 
    Set<Taglet.Location> s = new HashSet<>();
    s.add(Taglet.Location.METHOD);
    return s;
}

public String getName() { return "ParamTag";}

public boolean isInlineTag() { return false;}

public String toString(List<? extends DocTree> tags, Element element) {
    return "AJHSDLKAJLKJSFLKJASLKFJASLKFJASLKFJASLKFJASLKFJASLFKJASLFJALFJAFhahah";
}

}
