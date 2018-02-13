package com.github.mike10004.catmvnplugin.tests;

import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OrderedIncludesListTest extends CatTestBase {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void concatenate_orderingStrategyStrict() throws Exception {
        List<String> expected = Arrays.asList(
                "1548273a1307230340020026785.tmp",
                "3e7d7c6c8208515343041997728.tmp", 
                "194083491522014794833365089.tmp", 
                "1b4ee0506525418849632944565.tmp", 
                "7d5b4f6a5847250950834923936.tmp", 
                "79bd7e2c7276036019482197140.tmp", 
                "126e5dba8092492617660134053.tmp", 
                "3ceb7f756584010691443117149.tmp", 
                "7c8aa1a02118943624593385916.tmp", 
                "433399fc1147184194136794028.tmp"
        );
        List<String> actual = Files.asCharSource(getOutputFile(), StandardCharsets.UTF_8).readLines();
        assertEquals("output", expected, actual);
    }
}
