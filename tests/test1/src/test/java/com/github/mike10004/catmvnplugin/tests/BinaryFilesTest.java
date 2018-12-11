package com.github.mike10004.catmvnplugin.tests;

import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertArrayEquals;

public class BinaryFilesTest extends CatTestBase {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void concatenateBinaryFiles() throws Exception {
        byte[] expected = Files.asByteSource(getTestInputFile("cat.jpg")).read();
        byte[] actual = Files.asByteSource(getOutputFile()).read();
        assertArrayEquals("output", expected, actual);
    }
}
