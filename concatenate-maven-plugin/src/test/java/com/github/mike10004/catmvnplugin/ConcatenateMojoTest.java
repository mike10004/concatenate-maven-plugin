package com.github.mike10004.catmvnplugin;

import com.github.mike10004.catmvnplugin.ConcatenateMojo.NoYieldFromAnyFilesetsException;
import com.github.mike10004.catmvnplugin.ConcatenateMojo.NoYieldFromFileSetException;
import com.github.mike10004.catmvnplugin.ConcatenateMojo.RepeatedFileStrategy;
import com.github.mike10004.catmvnplugin.ConcatenateMojo.RepeatedItemException;
import com.github.mike10004.catmvnplugin.OrderableFileSet.OrderingStrategy;
import com.github.mike10004.catmvnplugin.OrderableFileSetManagerTest.TestCase;
import com.github.mike10004.catmvnplugin.OrderableFileSetManagerTest.TestCaseCreator;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.CharSource;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.*;

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
        Collection<List<File>> permutationsOfInputFiles = Collections2.permutations(ImmutableList.of(someFile1, someFile2));
        Set<String> possibleOutputs = permutationsOfInputFiles.stream()
                .map(files -> files.stream().map(file -> Files.asCharSource(file, charset)))
                .map(charSources -> CharSource.concat(charSources.collect(Collectors.toList())))
                .map(ConcatenateMojoTest::readUnchecked)
                .collect(Collectors.toSet());
        assertTrue("concatenated output not equal to any of expected outputs: " + outputText, possibleOutputs.contains(outputText));
    }

    private static String readUnchecked(CharSource charSource) {
        try {
            return charSource.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    @Test(expected = NoYieldFromFileSetException.class)
    public void emptySourcesList_default() throws Exception {
        testEmptySourcesList(false, false);
    }

    @Test(expected = NoYieldFromFileSetException.class)
    public void emptySourcesList_allowEmptySourcesList() throws Exception {
        testEmptySourcesList(false, true);
    }

    @Test(expected = NoYieldFromAnyFilesetsException.class)
    public void emptySourcesList_yieldlessFilesetAllowedButEmptySourcesListNot() throws Exception {
        testEmptySourcesList(true, false);
    }

    @Test
    public void emptySourcesList_ignoreYieldlessFilesetAndEmptySourcesList() throws Exception {
        File outputFile = testEmptySourcesList(true, true);
        assertEquals("output file length", 0, outputFile.length());
    }

    private File testEmptySourcesList(boolean ignoreEmptyIncludedFilesList, boolean ignoreEmptySourcesList) throws IOException, MojoFailureException, MojoExecutionException {
        File root = temporaryFolder.newFolder();
        File outputFile = new File(root, "output.txt");
        File inputDir = new File(root, "input");
        inputDir.mkdirs();
        for (String filename : new String[]{"a.txt", "b.jpg"}) {
            Files.write(filename, new File(inputDir, filename), charset);
        }
        OrderableFileSet fileset = new OrderableFileSet();
        fileset.setIgnoreEmptyIncludedFilesList(ignoreEmptyIncludedFilesList);
        fileset.setDirectory(inputDir.getAbsolutePath());
        fileset.setIncludes(Collections.singletonList("**/*.png"));
        ConcatenateMojo mojo = new ConcatenateMojo();
        mojo.setOutputFile(outputFile);
        mojo.setSources(new OrderableFileSet[]{fileset});
        mojo.setIgnoreEmptySourcesList(ignoreEmptySourcesList);
        mojo.execute();
        return outputFile;
    }

    @Test
    public void implicitIncludes() throws Exception {
        File outputFile = new File(temporaryFolder.getRoot(), "output.txt");
        File root = temporaryFolder.newFolder();
        File implicitlyIncludedFile = new File(root, "input.txt");
        File explicitlyExcludedFile = new File(root, "excluded.txt");
        Files.write(implicitlyIncludedFile.getName(), implicitlyIncludedFile, charset);
        Files.write(explicitlyExcludedFile.getName(), explicitlyExcludedFile, charset);
        OrderableFileSet fileset = new OrderableFileSet();
        fileset.setDirectory(root.getAbsolutePath());
        fileset.setExcludes(Collections.singletonList(explicitlyExcludedFile.getName()));
        newMojo(outputFile, fileset).execute();
        String expectedOutput = Files.toString(implicitlyIncludedFile, charset);
        String actualOutput = Files.toString(outputFile, charset);
        assertEquals("implicit includes", expectedOutput, actualOutput);
    }

    private static OrderableFileSet newFileSet(File directory, String...includes) {
        return newFileSet(directory, Arrays.asList(includes));
    }

    private static OrderableFileSet newFileSet(File directory, Iterable<String> includes) {
        OrderableFileSet fileset = new OrderableFileSet();
        fileset.setDirectory(directory.getAbsolutePath());
        fileset.setIncludes(Lists.newArrayList(includes));
        return fileset;
    }

    private static ConcatenateMojo newMojo(File outputFile, OrderableFileSet...filesets) {
        ConcatenateMojo mojo = new ConcatenateMojo();
        mojo.setOutputFile(outputFile);
        mojo.setSources(filesets);
        return mojo;
    }

    @Test
    public void divider() throws Exception {
        File outputFile = new File(temporaryFolder.getRoot(), "output.txt");
        File root = temporaryFolder.newFolder();
        List<String> contents = Arrays.asList("a", "b");
        for (String content : contents) {
            File file = new File(root, content);
            Files.write(content, file, charset);
        }
        String divider = "X";
        String expected = Joiner.on(divider).join(contents);
        ConcatenateMojo mojo = newMojo(outputFile, newFileSet(root, contents));
        mojo.setDivider(divider);
        mojo.setDividerCharset(charset.name());
        mojo.execute();
        String actual = Files.toString(outputFile, charset);
        assertEquals("divided output", expected, actual);
    }
}