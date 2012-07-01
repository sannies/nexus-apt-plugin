package com.github.sannies.nexusaptplugin;

import org.apache.maven.index.Field;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 6/30/12
 * Time: 12:26 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DEBIAN {

    String DEBIAN_NAMESPACE = "urn:nexus:debian#";

    Field PACKAGE = new Field(null, DEBIAN_NAMESPACE, "Package", "Debian Package Name");
    Field VERSION = new Field(null, DEBIAN_NAMESPACE, "Version", "Debian Package Version");
    Field ARCHITECTURE = new Field(null, DEBIAN_NAMESPACE, "Architecture", "Debian Package Architecture");
    Field MAINTAINER = new Field(null, DEBIAN_NAMESPACE, "Maintainer", "Debian Package Maintainer");
    Field INSTALLED_SIZE = new Field(null, DEBIAN_NAMESPACE, "Installed-Size", "Debian Package Installed Size");
    Field DEPENDS = new Field(null, DEBIAN_NAMESPACE, "Depends", "Debian Package Depends");
    Field SECTION = new Field(null, DEBIAN_NAMESPACE, "Section", "Debian Package Section");
    Field PRIORITY = new Field(null, DEBIAN_NAMESPACE, "Priority", "Debian Package Priority");
    Field DESCRIPTION = new Field(null, DEBIAN_NAMESPACE, "Description", "Debian Package Description");


}
