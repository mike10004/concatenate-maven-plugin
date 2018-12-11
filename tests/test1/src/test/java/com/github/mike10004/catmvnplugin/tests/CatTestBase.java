package com.github.mike10004.catmvnplugin.tests;

import com.google.common.base.Preconditions;

import java.io.File;

public class CatTestBase {

    protected String getOutputFileSystemPropertyName() {
        return getClass().getSimpleName() + ".outputFile";
    }

    protected File getOutputFile() {
        return new File(System.getProperty(getOutputFileSystemPropertyName()));
    }

    protected File getTestInputFile(String relativePath) {
        Preconditions.checkArgument(relativePath != null && !relativePath.isEmpty(), "relative path must be non-null and non-empty");
        return new File(getTestInputDirectory(), relativePath);
    }

    protected File getTestInputDirectory() {
        String propValue = System.getProperty("testInputFilesDirectory");
        Preconditions.checkState(propValue != null, "test input files directory is not defined as system property testInputFilesDirectory");
        File dir = new File(propValue);
        Preconditions.checkState(dir.isDirectory(), "not a directory: " + dir);
        return dir;
    }
}
