package com.github.mike10004.catmvnplugin;

import com.github.mike10004.catmvnplugin.OrderableFileSet.OrderingStrategy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.assertEquals;

public class OrderableFileSetManagerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    static class TestCase {
        public final ImmutableList<File> filesInCreationOrder;
        public final ImmutableList<File> filesInShuffledOrder;

        public TestCase(Iterable<File> filesInCreationOrder, Iterable<File> filesInShuffledOrder) {
            this.filesInCreationOrder = ImmutableList.copyOf(filesInCreationOrder);
            this.filesInShuffledOrder = ImmutableList.copyOf(filesInShuffledOrder);
        }

        public Stream<String> getShuffledFilenames() {
            return filesInShuffledOrder.stream().map(File::getName);
        }
    }

    static class TestCaseCreator {

        private final Random random;

        TestCaseCreator(Random random) {
            this.random = random;
        }

        public TestCase create(File root, int numFilesForTest) throws IOException {
            List<File> files = new ArrayList<>(numFilesForTest);
            for (int i = 0; i < numFilesForTest; i++) {
                String filenameStem = String.format("%08x", random.nextInt(Integer.MAX_VALUE));
                File f = File.createTempFile(filenameStem, null, root);
                files.add(f);
            }
            checkState(!Ordering.natural().isOrdered(files), "really unlikely: random filenames came out in alphabetical order");
            List<File> shuffledFiles = new ArrayList<>(files);
            Collections.shuffle(shuffledFiles, random);
            checkState(!files.equals(shuffledFiles), "really unlikely: shuffling made no change in list order");
            return new TestCase(files, shuffledFiles);
        }
    }

    @Test
    public void getIncludedFiles_orderingStrategyStrict() throws Exception {
        File root = temporaryFolder.newFolder();
        int numFilesForTest = 10; // needs to be enough that random false positive is unlikely
        Random random = new Random(getClass().hashCode());
        TestCase testCase = new TestCaseCreator(random).create(root, numFilesForTest);
        List<String> includes = testCase.getShuffledFilenames().collect(Collectors.toList());
        OrderableFileSet fs = new OrderableFileSet();
        fs.setOrderingStrategy(OrderingStrategy.strict);
        fs.setDirectory(root.getAbsolutePath());
        fs.setIncludes(includes);
        OrderableFileSetManager fsm = new OrderableFileSetManager();
        List<String> actual = Arrays.asList(fsm.getIncludedFiles(fs));
        System.out.println();
        actual.forEach(System.out::println);
        assertEquals("expect file list in order of includes", includes, actual);
    }

    @Test
    public void implicitIncludes_demonstateTraditionalBehavior() throws Exception {
        testImplicitIncludes(new FileSet(), new FileSetManager());
    }

    @Test
    public void implicitIncludes_orderableManager() throws Exception {
        testImplicitIncludes(new FileSet(), new OrderableFileSetManager());
    }

    @Test
    public void implicitIncludes_orderableSetAndOrderableManager() throws Exception {
        testImplicitIncludes(new OrderableFileSet(), new OrderableFileSetManager());
    }

    @Test
    public void implicitIncludes_orderableSet() throws Exception {
        testImplicitIncludes(new OrderableFileSet(), new FileSetManager());
    }

    private void testImplicitIncludes(FileSet fileset, FileSetManager fsm) throws Exception {
        File file = temporaryFolder.newFile();
        Files.touch(file);
        fileset.setDirectory(temporaryFolder.getRoot().getAbsolutePath());
        String[] includedFiles = fsm.getIncludedFiles(fileset);
        System.out.format("included files: %s%n", Arrays.toString(includedFiles));
        assertEquals("included files count", 1, includedFiles.length);
        String[] includedDirs = fsm.getIncludedDirectories(fileset);
        System.out.format("included dirs: %s%n", Arrays.toString(Stream.of(includedDirs).map(dir -> "\"" + StringEscapeUtils.escapeJava(dir) + "\"").toArray()));
        assertEquals("included dirs count", 1, includedDirs.length);
        assertEquals("included dir", "", includedDirs[0]);
    }
}
