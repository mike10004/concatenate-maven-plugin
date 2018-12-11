package com.github.mike10004.catmvnplugin;

import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;

public class ConcatenateBinaryFilesTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Random random;

    @Before
    public void setUp() {
        random = new Random(getClass().getName().hashCode());
    }

    @Test
    public void execute_basic() throws Exception {
        File dir = temporaryFolder.newFolder();
        File someFile1 = new File(dir, "child/part1.bin"), someFile2 = new File(dir, "child/part2.bin");
        Files.createParentDirs(someFile1);
        Files.createParentDirs(someFile2);
        byte[] head = new byte[64];
        random.nextBytes(head);
        byte[] tail = new byte[64];
        random.nextBytes(tail);
        byte[] expected = ArrayUtils.addAll(head, tail);
        Files.asByteSink(someFile1).write(head);
        Files.asByteSink(someFile2).write(tail);
        OrderableFileSet fileset = new OrderableFileSet();
        fileset.setDirectory(dir.getAbsolutePath());
        fileset.setIncludes(Collections.singletonList("**/*.bin"));
        fileset.setSort(OrderableFileSet.SortingStrategy.alphabetical);
        File outputFile = new File(temporaryFolder.newFolder(), "output.bin");
        ConcatenateMojo mojo = new ConcatenateMojo();
        mojo.setOutputFile(outputFile);
        mojo.setSources(new OrderableFileSet[]{fileset});
        mojo.execute();
        byte[] actual = Files.toByteArray(outputFile);
        if (!Arrays.equals(expected, actual)) {
            System.out.format("expected %s%n", BaseEncoding.base16().encode(expected));
            System.out.format("  actual %s%n", BaseEncoding.base16().encode(actual));
        }
        assertArrayEquals("output file bytes should equal concatenation of input files bytes", expected, actual);
    }

}