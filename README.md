[![Travis build status](https://travis-ci.org/mike10004/concatenate-maven-plugin.svg?branch=master)](https://travis-ci.org/mike10004/concatenate-maven-plugin)
[![AppVeyor build status](https://ci.appveyor.com/api/projects/status/ivrio01k770ctkxb?svg=true)](https://ci.appveyor.com/project/mike10004/concatenate-maven-plugin)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.mike10004/concatenate-maven-plugin.svg)](https://repo1.maven.org/maven2/com/github/mike10004/concatenate-maven-plugin/)

concatentate-maven-plugin
=========================

Maven plugin that concatenates files. The current implementation is extremely
simple and does not allow for much configuration.

Basic Usage
-----------

Basic usage is like this:

    <plugin>
        <groupId>com.github.mike10004</groupId>
        <artifactId>concatenate-maven-plugin</artifactId>
        <version>0.2</version>
        <executions>
            <execution>
                <id>concatenate-css-files</id>
                <phase>prepare-package</phase> <!-- default phase -->
                <goals>
                    <goal>cat</goal>
                </goals>
                <configuration>
                    <sources>
                        <fileset>
                            <directory>${project.basedir}/src/main/parts</directory>
                            <orderingStrategy>strict</orderingStrategy>
                            <includes>
                                <include>head.txt</include>
                                <include>body*.txt</include>
                                <include>tail.txt</include>
                            </includes>
                        </fileset>
                    </sources>
                    <outputFile>${project.build.outputDirectory}/concatenated-parts.txt</outputFile>
                </configuration>
            </execution>
        </executions>
    </plugin>

This produces a file named `target/classes/concatentated-parts.txt` that 
consists of the concatenated content of the files identified in the `<includes>`
section of the sources fileset.

Note that without `<orderingStrategy>strict</orderingStrategy>`, you wind up 
with the source files in (what I think is) whatever order the filesystem lists 
them on a call to `java.io.File.list()`. Depending on your use case, this may 
be okay, and you can get a performance benefit out of omitting the ordering 
strategy parameter.

The Why
-------

There are a few plugins for concatenating and optimizing resource files, but
none are dead simple. Most want to tool around in your classpath or obfuscate
and compress the output. If you're always on Linux with Bash and you know the
names of your files ahead of time, you can use the exec plugin and `cat` 
everything to wherever you want it. But if you need build-time file selection
and cross-platform compatibility, this might help you.

Deployment
----------

Run 

    $ mvn deploy

from the `concatenate-maven-plugin` child module directory instead of this
parent directory.
