package com.github.mike10004.catmvnplugin.tests;

import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AlphabeticalFilesTest extends CatTestBase {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void sortIncludedFilesAlphabetically() throws Exception {
        List<String> expected = Arrays.asList("one", "two", "three");
        List<String> actual = Files.asCharSource(getOutputFile(), StandardCharsets.UTF_8).readLines();
        assertEquals("output", expected, actual);
    }
}
