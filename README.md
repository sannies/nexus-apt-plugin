Nexus APT Plugin
================

this plugin generates a Packages.gz for each nexus repository and allows the repository to be 
listed in a debian /etc/apt/sources.list file so that it can be used by aptitude/apt-get/ubuntu 
software center.

Installation
============

The 'Downloads' section of this project contains the latest builds. Please download the latest 
nexus-apt-plugin-N.N-bundle.zip and unzip it into the sonatype-work/nexus/plugin-repository/
and restart nexus. 

> to be sure that the index is regenerated (the plugin adds attributes to index) it could be 
neccessary to delete the index files under sonatype-work/nexus/indexer

All repositories now contain a Packages.gz that lists all debian packages the indexer was able 
to find.

pitfall
-------

The indexer cannot find packages when there is a main artifact with the same name:
If the artifacts are named like:

-  nexus-apt-plugin-0.5.jar 
-  nexus-apt-plugin-0.5.deb 

The indexer won't index the debian package. In order to make the indexer index the debian 
package it needs a classifier:

-  nexus-apt-plugin-0.5.jar 
-  nexus-apt-plugin-0.5-all.deb 
  
This is fine.

Adding a repository to sources.list
===================================

just add the line `deb http://repository.yourcompany.com/content/repositories/releases/Packages.gz ./` 
to your `/etc/apt/sources.list`. Type `apt-get update` and all debian packages in the repository
can now be installed via `apt-get install`.