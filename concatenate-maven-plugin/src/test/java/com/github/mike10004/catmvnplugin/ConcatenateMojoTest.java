package com.github.mike10004.catmvnplugin;

import com.github.mike10004.catmvnplugin.ConcatenateMojo.RepeatedFileStrategy;
import com.github.mike10004.catmvnplugin.ConcatenateMojo.RepeatedItemException;
import com.github.mike10004.catmvnplugin.OrderableFileSet.OrderingStrategy;
import com.github.mike10004.catmvnplugin.OrderableFileSetManagerTest.TestCase;
import com.github.mike10004.catmvnplugin.OrderableFileSetManagerTest.TestCaseCreator;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

public class ConcatenateMojoTest {

    private static final Charset charset = StandardCharsets.UTF_8;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void execute_basic() throws Exception {
        File dir = temporaryFolder.newFolder();
        File someFile1 = new File(dir, "child/foo.txt"), someFile2 = new File(dir, "child/bar.txt");
        Files.createParentDirs(someFile1);
        Files.createParentDirs(someFile2);
        Files.write("foo", someFile1, charset);
        Files.write("bar", someFile2, charset);
        OrderableFileSet fileset = new OrderableFileSet();
        fileset.setDirectory(dir.getAbsolutePath());
        fileset.setIncludes(Collections.singletonList("**/*.txt"));
        File outputFile = new File(temporaryFolder.newFolder(), "output.txt");
        ConcatenateMojo mojo = new ConcatenateMojo();
        mojo.setOutputFile(outputFile);
        mojo.setSources(new OrderableFileSet[]{fileset});
        mojo.execute();
        String outputText = Files.toString(outputFile, charset);
        String expectedOutputText = ByteSource.concat(Stream.of(someFile1, someFile2).sorted().map(Files::asByteSource).collect(Collectors.toList()))
                .asCharSource(charset).read();
        assertEquals("concatenated output", expectedOutputText, outputText);
    }

    @Test
    public void execute_rewriteRepeatedFile() throws Exception {
        File file = temporaryFolder.newFile();
        String s = "hello";
        Files.write(s, file, charset);
        testRepeatedFileStrategy(file, RepeatedFileStrategy.repeat, s + s);
    }

    @Test
    public void execute_ignoreRepeatedFile() throws Exception {
        File file = temporaryFolder.newFile();
        String s = "hello";
        Files.write(s, file, charset);
        testRepeatedFileStrategy(file, RepeatedFileStrategy.ignore, s);
    }

    @Test(expected = RepeatedItemException.class)
    public void execute_failOnRepeatedFile() throws Exception {
        File file = temporaryFolder.newFile();
        testRepeatedFileStrategy(file, RepeatedFileStrategy.fail, null);
    }

    private static OrderableFileSet singletonFileset(File file) {
        OrderableFileSet fileset = new OrderableFileSet();
        fileset.setDirectory(file.getParent());
        fileset.setIncludes(Collections.singletonList(file.getName()));
        return fileset;
    }

    private void testRepeatedFileStrategy(File file, RepeatedFileStrategy strategy, String expectedOutput) throws IOException, MojoFailureException, MojoExecutionException {
        File outputFile = new File(temporaryFolder.newFolder(), "output.txt");
        OrderableFileSet fileSet1 = singletonFileset(file), fileSet2 = singletonFileset(file);
        ConcatenateMojo mojo = new ConcatenateMojo();
        mojo.setRepeatedFileStrategy(strategy);
        mojo.setSources(new OrderableFileSet[]{fileSet1, fileSet2});
        mojo.setOutputFile(outputFile);
        mojo.execute();
        String actual = Files.toString(outputFile, charset);
        assertEquals("output with strategy " + strategy, expectedOutput, actual);
        com.google.common.collect.Ordering.class.getName();
    }

    @Test
    public void strictIncludesOrdering() throws Exception {
        File root = temporaryFolder.newFolder();
        File outputFile = new File(temporaryFolder.newFolder(), "output.txt");
        int numFilesForTest = 10; // needs to be enough that random false positive is unlikely
        Random random = new Random(getClass().hashCode());
        TestCase testCase = new TestCaseCreator(random).create(root, numFilesForTest);
        Map<File, String> nameMap = new HashMap<>();
        StringBuilder expectedOutputBuilder = new StringBuilder(numFilesForTest * 50);
        for (File file : testCase.filesInCreationOrder) {
            String namePlusNewline = String.format("%s%n", file.getName());
            Files.write(namePlusNewline, file, charset);
            nameMap.put(file, namePlusNewline);
        }
        for (File file : testCase.filesInShuffledOrder) {
            String namePlusNewline = checkNotNull(nameMap.get(file));
            expectedOutputBuilder.append(namePlusNewline);
        }
        List<String> includes = testCase.getShuffledFilenames().collect(Collectors.toList());
        ConcatenateMojo mojo = new ConcatenateMojo();
        OrderableFileSet fileset = new OrderableFileSet();
        fileset.setDirectory(root.getAbsolutePath());
        fileset.setOrderingStrategy(OrderingStrategy.strict);
        fileset.setIncludes(includes);
        mojo.setSources(new OrderableFileSet[]{fileset});
        mojo.setOutputFile(outputFile);
        mojo.execute();
        String expectedOutput = expectedOutputBuilder.toString();
        String actualOutput= Files.toString(outputFile, charset);
        assertEquals("output", expectedOutput, actualOutput);
    }

}