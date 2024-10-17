package com.gridnine.testing;

import java.util.List;
import java.util.function.Predicate;

public interface Filtering<T> {

    void addPredicate(Predicate<T> predicate);

    void addPredicates(List<Predicate<T>> predicates);

    List<T> filter(List<T> list);

}
