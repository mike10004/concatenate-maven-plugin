/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.mike10004.catmvnplugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

/**
 * Copies of several Guava methods.
 */
@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored", "UnusedReturnValue"})
class Guava {

    private Guava() {}

    public static class Strings {
        private Strings() {}

        public static boolean isNullOrEmpty(String s) {
            return s == null || s.isEmpty();
        }

        public static String nullToEmpty(@Nullable String s) {
            return s == null ? "" : s;
        }
    }

    public static class Files {

        private Files() {}

        /**
         * Creates any necessary but nonexistent parent directories of the specified file. Note that if
         * this operation fails it may have succeeded in creating some (but not all) of the necessary
         * parent directories.
         *
         * @throws IOException if an I/O error occurs, or if any necessary but nonexistent parent
         *     directories of the specified file could not be created.
         * @since 4.0
         */
        public static void createParentDirs(File file) throws IOException {
            requireNonNull(file);
            File parent = file.getCanonicalFile().getParentFile();
            if (parent == null) {
                /*
                 * The given directory is a filesystem root. All zero of its ancestors exist. This doesn't
                 * mean that the root itself exists -- consider x:\ on a Windows machine without such a drive
                 * -- or even that the caller can create it, but this method makes no such guarantees even for
                 * non-root files.
                 */
                return;
            }
            parent.mkdirs();
            if (!parent.isDirectory()) {
                throw new IOException("Unable to create parent directories of " + file);
            }
        }

    }

    public static class ByteStreams {
        private ByteStreams() {}
        /**
         * Copies all bytes from the input stream to the output stream. Does not close or flush either
         * stream.
         *
         * @param from the input stream to read from
         * @param to the output stream to write to
         * @return the number of bytes copied
         * @throws IOException if an I/O error occurs
         */
        public static long copy(InputStream from, OutputStream to) throws IOException {
            requireNonNull(from);
            requireNonNull(to);
            byte[] buf = new byte[8192];
            long total = 0;
            while (true) {
                int r = from.read(buf);
                if (r == -1) {
                    break;
                }
                to.write(buf, 0, r);
                total += r;
            }
            return total;
        }

    }
}
