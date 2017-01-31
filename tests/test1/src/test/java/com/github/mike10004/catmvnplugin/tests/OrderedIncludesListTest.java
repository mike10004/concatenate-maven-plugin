/*
 * (c) 2017 Novetta
 *
 * Created by mike
 */
package com.github.mike10004.catmvnplugin.tests;

import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class OrderedIncludesListTest extends CatTestBase {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void concatenate_orderingStrategyStrict() throws Exception {
        String expectedOutput = "1548273a1307230340020026785.tmp\n" +
                "3e7d7c6c8208515343041997728.tmp\n" +
                "194083491522014794833365089.tmp\n" +
                "1b4ee0506525418849632944565.tmp\n" +
                "7d5b4f6a5847250950834923936.tmp\n" +
                "79bd7e2c7276036019482197140.tmp\n" +
                "126e5dba8092492617660134053.tmp\n" +
                "3ceb7f756584010691443117149.tmp\n" +
                "7c8aa1a02118943624593385916.tmp\n" +
                "433399fc1147184194136794028.tmp\n";
        String actualOutput = Files.toString(getOutputFile(), StandardCharsets.UTF_8);
        assertEquals("output", expectedOutput, actualOutput);
    }
}
