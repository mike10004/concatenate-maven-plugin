package com.github.mike10004.catmvnplugin;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.github.mike10004.catmvnplugin.OrderableFileSet.OrderingStrategy;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.plexus.logging.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Provides operations for use with FileSet instances, such as retrieving the included/excluded files, deleting all
 * matching entries, etc.
 *
 * @author jdcasey
 * @version $Id: FileSetManager.java 1721672 2015-12-25 13:18:36Z khmarbaise $
 */
public class OrderableFileSetManager extends FileSetManager
{
    @SuppressWarnings({"unused", "BooleanParameter"})
    public OrderableFileSetManager(Log log, boolean verbose) {
        super(log, verbose);
    }

    @SuppressWarnings("unused")
    public OrderableFileSetManager(Log log) {
        super(log);
    }

    @SuppressWarnings({"unused", "BooleanParameter"})
    public OrderableFileSetManager(Logger log, boolean verbose) {
        super(log, verbose);
    }

    @SuppressWarnings("unused")
    public OrderableFileSetManager(Logger log) {
        super(log);
    }

    @SuppressWarnings("unused")
    public OrderableFileSetManager() {
        super();
    }

    protected OrderingStrategy getOrderingStrategy(FileSet fileSet) {
        if (fileSet instanceof OrderableFileSet) {
            OrderingStrategy strategy = ((OrderableFileSet)fileSet).getOrderingStrategy();
            if (strategy == null) {
                throw new IllegalArgumentException("ordered file set must have strategy set");
            }
            return strategy;
        } else {
            return OrderingStrategy.traditional;
        }
    }

    /**
     * Get all the filenames which have been included by the rules in this fileset.
     *
     * @param fileSet The fileset defining rules for inclusion/exclusion, and base directory.
     * @return the array of matching filenames, relative to the basedir of the file-set.
     */
    @Override
    public String[] getIncludedFiles( FileSet fileSet ) {
        return getIncludedFilesOrDirectories(fileSet, super::getIncludedFiles);
    }

    protected String[] getIncludedFilesOrDirectories( FileSet fileSet, Function<FileSet, String[]> superGetter ) {
        OrderingStrategy strategy = getOrderingStrategy(fileSet);
        switch (strategy) {
            case traditional:
                return superGetter.apply(fileSet);
            case strict:
                if (fileSet.getIncludes().isEmpty()) {
                    return superGetter.apply(fileSet);
                } else {
                    return getIncludedFilesOrDirectoriesInIncludesOrder(fileSet, superGetter);
                }
            default:
                throw new IllegalArgumentException("bug: unhandled ordering strategy: " + strategy);
        }
    }

    private static FileSet cloneExceptIncludes(FileSet source) {
        FileSet target = new FileSet();
        target.setDirectory(source.getDirectory());
        target.setLineEnding(source.getLineEnding());
        target.setModelEncoding(source.getModelEncoding());
        target.setDirectoryMode(source.getDirectoryMode());
        target.setExcludes(source.getExcludes());
        target.setFileMode(source.getFileMode());
        target.setFollowSymlinks(source.isFollowSymlinks());
        target.setMapper(source.getMapper());
        target.setOutputDirectory(source.getOutputDirectory());
        target.setUseDefaultExcludes(source.isUseDefaultExcludes());
        return target;
    }

    protected Stream<FileSet> expandIncludes(FileSet orderedFileSet) {
        List<String> includes = orderedFileSet.getIncludes();
        return includes.stream().map(include -> {
            FileSet singleIncludeFileset = cloneExceptIncludes(orderedFileSet);
            singleIncludeFileset.setIncludes(Collections.singletonList(include));
            return singleIncludeFileset;
        });
    }

    protected String[] getIncludedFilesOrDirectoriesInIncludesOrder(FileSet orderedFileSet, Function<FileSet, String[]> getter) {
        Stream<FileSet> virtualFilesets = expandIncludes(orderedFileSet);
        Collection<String> expandedList = new LinkedHashSet<>();
        virtualFilesets.forEach(fileset -> {
            String[] includedThings = getter.apply(fileset);
            expandedList.addAll(Arrays.asList(includedThings));
        });
        return expandedList.toArray(new String[0]);
    }

    /**
     * Get all the directory names which have been included by the rules in this fileset.
     *
     * @param fileSet The fileset defining rules for inclusion/exclusion, and base directory.
     * @return the array of matching dirnames, relative to the basedir of the file-set.
     */
    @Override
    public String[] getIncludedDirectories( FileSet fileSet )
    {
        return getIncludedFilesOrDirectories(fileSet, super::getIncludedDirectories);
    }

}
