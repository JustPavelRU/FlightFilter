package com.gridnine.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilteringImpl implements Filtering<Flight> {

    private final List<Predicate<Flight>> predicates = new ArrayList<>();

    public void addPredicate(Predicate<Flight> predicate) {
        this.predicates.add(predicate);
    }

    public void addPredicates(List<Predicate<Flight>> predicates) {
        this.predicates.addAll(predicates);
    }

    public List<Flight> filter(List<Flight> flights) {

        if (flights == null || predicates.size() == 0) {
            return null;
        }

        Predicate<Flight> composedPredicate = predicates.get(0);
        for (int i = 1; i < predicates.size(); i++) {
            composedPredicate = composedPredicate.and(predicates.get(i));
        }

        return flights.stream().filter(composedPredicate).collect(Collectors.toList());
    }
}
