package com.github.mike10004.catmvnplugin.tests;/*
 * (c) 2017 Mike Chaberski
 *
 * Created by mike
 */

import java.io.File;

public class CatTestBase {

    protected String getOutputFileSystemPropertyName() {
        return getClass().getSimpleName() + ".outputFile";
    }

    protected File getOutputFile() {
        return new File(System.getProperty(getOutputFileSystemPropertyName()));
    }
}
