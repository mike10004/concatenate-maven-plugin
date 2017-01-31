/*
 * (c) 2017 Novetta
 *
 * Created by mike
 */
package com.github.mike10004.catmvnplugin;

import com.google.common.io.Files;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Stream;

@Mojo(name = "cat", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
@NotThreadSafe
public class ConcatenateMojo extends org.apache.maven.plugin.AbstractMojo {

    @SuppressWarnings({"MismatchedReadAndWriteOfArray", "unused"})
    @Parameter
    private OrderableFileSet[] sources;

    @Parameter
    private File outputFile;

    /**
     * Flag that indicates whether an error should occur if the set of source filesets
     * does not result in any source files to be concatenated.
     */
    @SuppressWarnings("unused")
    @Parameter
    private boolean ignoreEmptySourcesList;

    @Parameter(defaultValue = "repeat")
    private RepeatedFileStrategy repeatedFileStrategy = RepeatedFileStrategy.repeat;

    @SuppressWarnings("unused")
    public enum RepeatedFileStrategy {
        repeat,
        ignore,
        fail;

        public Collection<File> createBucket() {
            switch (this) {
                case repeat:
                    return new ArrayList<>();
                case ignore:
                    return new LinkedHashSet<>();
                case fail:
                    return new NoRepeatsSet<>();
                default:
                    throw new IllegalStateException("bug: strategy " + this + " not handled");
            }
        }

    }
    static class RepeatedItemException extends IllegalArgumentException {
        public RepeatedItemException(String s) {
            super(s);
        }
    }

    private static class NoRepeatsSet<E> extends LinkedHashSet<E> {
        @Override
        public boolean add(E e) {
            boolean result = super.add(e);
            if (!result) {
                throw new RepeatedItemException("add() failed, indicating that element was already in set");
            }
            return true;
        }

    }

    private static String toString(FileSet fileset) {
        Map<String, Object> map = new HashMap<>();
        map.put("directory", fileset.getDirectory());
        map.put("includes", fileset.getIncludes());
        map.put("excludes", fileset.getExcludes());
        return "FileSet{" + map.toString() + "}";
    }

    protected Collection<File> createBucket() {
        return repeatedFileStrategy.createBucket();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (sources == null) {
            throw new IllegalStateException("sources filesets not yet set");
        }
        final Collection<File> sourceFiles = createBucket();
        FileSetManager fileSetManager = new OrderableFileSetManager();
//        FileSetManager fileSetManager = new FileSetManager();
        getLog().debug(sources.length + " sources specified");
        for (int i = 0; i < sources.length; i++) {
            OrderableFileSet fileset = sources[i];
            String filesetDir = fileset.getDirectory();
            if (filesetDir == null) {
                throw new MojoExecutionException("fileset directory not set on fileset at index " + i + ": " + toString(fileset));
            }
            File parent = new File(filesetDir);
            String[] includedDirs = fileSetManager.getIncludedDirectories( fileset );
            if (includedDirs != null && includedDirs.length > 0) {
                throw new MojoExecutionException("included directories are not supported");
            }
            String[] excludedDirs = fileSetManager.getExcludedDirectories( fileset );
            if (excludedDirs != null && excludedDirs.length > 0) {
                throw new MojoExecutionException("excluded directories are not yet supported");
            }
            String[] includedFiles = fileSetManager.getIncludedFiles( fileset );
            if (!fileset.isIgnoreEmptyIncludedFilesList() && includedFiles.length == 0) {
                throw new MojoExecutionException("fileset at index " + i + " did not yield any files: " + toString(fileset));
            }
            Stream.of(includedFiles).forEach(p -> {
                File file = new File(parent, p);
                boolean result = sourceFiles.add(file);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("included file " + file + " (Collection.add = " + result + ")");
                }
            });
        }
        if (!ignoreEmptySourcesList && sourceFiles.isEmpty()) {
            throw new MojoExecutionException("filesets did not yield any files; set ignoreEmptySourceList flag to true if this should be ignored");
        }
        try {
            writeConcatenated(sourceFiles);
        } catch (IOException e) {
            throw new MojoExecutionException("failed to copy source files to destination " + outputFile, e);
        }
    }

    protected void writeConcatenated(Iterable<File> sourceFiles) throws IOException {
        if (outputFile == null) {
            throw new IllegalStateException("output file not set");
        }
        Files.createParentDirs(outputFile);
        try (OutputStream output = new FileOutputStream(outputFile)) {
            for (File sourceFile : sourceFiles) {
                try (InputStream input = new FileInputStream(sourceFile)) {
                    org.apache.commons.io.IOUtils.copy(input, output);
                }
            }
        }
    }

    void setSources(OrderableFileSet[] sources) {
        this.sources = sources;
    }

    void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    @SuppressWarnings("unused")
    private static class BadSourceSpecificationException extends MojoExecutionException {

        public BadSourceSpecificationException(Object source, String shortMessage, String longMessage) {
            super(source, shortMessage, longMessage);
        }

        public BadSourceSpecificationException(String message, Exception cause) {
            super(message, cause);
        }

        public BadSourceSpecificationException(String message, Throwable cause) {
            super(message, cause);
        }

        public BadSourceSpecificationException(String message) {
            super(message);
        }
    }

    @SuppressWarnings("unused")
    private static class SourceFileNotFoundException extends MojoExecutionException {

        public SourceFileNotFoundException(Object source, String shortMessage, String longMessage) {
            super(source, shortMessage, longMessage);
        }

        public SourceFileNotFoundException(String message, Exception cause) {
            super(message, cause);
        }

        public SourceFileNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public SourceFileNotFoundException(String message) {
            super(message);
        }
    }

    void setRepeatedFileStrategy(RepeatedFileStrategy repeatedFileStrategy) {
        this.repeatedFileStrategy = repeatedFileStrategy;
    }

    void setIgnoreEmptySourcesList(boolean ignoreEmptySourcesList) {
        this.ignoreEmptySourcesList = ignoreEmptySourcesList;
    }
}
