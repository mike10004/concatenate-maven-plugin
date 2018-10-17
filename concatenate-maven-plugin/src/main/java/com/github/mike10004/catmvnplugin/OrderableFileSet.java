package com.github.mike10004.catmvnplugin;

import org.apache.maven.shared.model.fileset.FileSet;

import static java.util.Objects.requireNonNull;

public class OrderableFileSet extends FileSet {

    private OrderingStrategy orderingStrategy = OrderingStrategy.strict;
    private boolean ignoreEmptyIncludedFilesList;

    public OrderableFileSet() {
    }

    public enum OrderingStrategy {
        traditional,
        strict
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
}
