package com.github.mike10004.catmvnplugin.tests;/*
 * (c) 2017 Mike Chaberski
 *
 * Created by mike
 */

import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SingleFileTest extends CatTestBase {

    @Test
    public void outputExists() throws Exception {
        assertTrue("isFile", getOutputFile().isFile());
    }

    @Test
    public void outputHasCorrectContent() throws Exception {
        String actual = com.google.common.io.Files.toString(getOutputFile(), UTF_8);
        assertEquals("content", "a", actual);
    }

}
