package com.github.mike10004.catmvnplugin;

import org.apache.maven.shared.model.fileset.FileSet;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Comparator;

import static java.util.Objects.requireNonNull;

public class OrderableFileSet extends FileSet {

    private OrderingStrategy orderingStrategy = OrderingStrategy.strict;

    @Nullable
    private SortingStrategy sort;

    private boolean ignoreEmptyIncludedFilesList;

    public OrderableFileSet() {
    }

    public enum OrderingStrategy {
        traditional,
        strict
    }

    public enum SortingStrategy {

        /**
         * Do not sort included files and directories.
         */
        none,

        /**
         * Sort included files and directories by pathname.
         */
        alphabetical;

        public void sort(String[] items) {
            if (this != none) {
                Arrays.sort(items, getComparator());
            }
        }

        private Comparator<String> getComparator() {
            switch (this) {
                case alphabetical:
                    return String::compareTo;
                default:
                    throw new IllegalStateException("BUG: unhandled enum constant: SortingStrategy" + this);
            }
        }
    }

    public OrderingStrategy getOrderingStrategy() {
        return orderingStrategy;
    }

    public void setOrderingStrategy(OrderingStrategy orderingStrategy) {
        this.orderingStrategy = requireNonNull(orderingStrategy);
    }

    public boolean isIgnoreEmptyIncludedFilesList() {
        return ignoreEmptyIncludedFilesList;
    }

    public void setIgnoreEmptyIncludedFilesList(boolean ignoreEmptyIncludedFilesList) {
        this.ignoreEmptyIncludedFilesList = ignoreEmptyIncludedFilesList;
    }

    @Nullable
    public SortingStrategy getSort() {
        return sort;
    }

    public void setSort(@Nullable SortingStrategy sort) {
        this.sort = sort;
    }
}
