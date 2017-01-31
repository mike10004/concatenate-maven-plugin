package com.github.mike10004.catmvnplugin.tests;

import com.google.common.io.Files;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

public class IgnoreRepeatedFileTest extends CatTestBase {

    @Test
    public void confirmCorrectOutput() throws Exception {
        String actual = Files.toString(getOutputFile(), UTF_8);
        assertEquals("output", "ab", actual);
    }
}
