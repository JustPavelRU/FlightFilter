package com.gridnine.testing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;


public class FilteringImplTest {

    private static final LocalDateTime DATE_NOW = LocalDateTime.now();
    private Filtering<Flight> filtering;

    @BeforeEach
    public void setup() {
        filtering = new FilteringImpl();
    }

    /*
    Тест фильтрации перелетов с вылетами до текущего момента времени.
    */
    @Test
    public void testFilteringBeforeCurrentTime() {

        Segment segmentNotCorrect =
                new Segment(DATE_NOW.minusDays(1), DATE_NOW.plusDays(1));
        Segment segmentCorrect =
                new Segment(DATE_NOW.plusDays(1), DATE_NOW.plusDays(2));

        Flight flight1 =
                new Flight(List.of(segmentNotCorrect));
        Flight flight2 =
                new Flight(List.of(segmentCorrect));

        Predicate<Flight> predicate = Main.makeBeforeTimePredicate(DATE_NOW);

        assertFalse(predicate.test(flight1));
        assertTrue(predicate.test(flight2));

        List<Flight> flightList = Arrays.asList(flight1, flight2);

        assertEquals(2, flightList.size());

        filtering.addPredicate(predicate);
        flightList = filtering.filter(flightList);

        assertEquals(1, flightList.size());
        assertEquals(flight2, flightList.get(0));
    }

    /*
    Тест фильтрации перелетов, у которых имеются сегменты с датой прилета раньше даты вылета.
    */
    @Test
    public void testFilteringArrivalBeforeDeparture() {

        Segment segmentNotCorrect =
                new Segment(DATE_NOW.plusDays(2), DATE_NOW.plusDays(1));
        Segment segmentCorrect =
                new Segment(DATE_NOW.plusDays(1), DATE_NOW.plusDays(2));

        Flight flight1 =
                new Flight(Arrays.asList(segmentNotCorrect, segmentCorrect));
        Flight flight2 =
                new Flight(Arrays.asList(segmentCorrect));

        Predicate<Flight> predicate = new Main.ArrivalBeforeDeparturePredicate();

        assertFalse(predicate.test(flight1));
        assertTrue(predicate.test(flight2));

        List<Flight> flightList = Arrays.asList(flight1, flight2);

        assertEquals(2, flightList.size());

        filtering.addPredicate(predicate);
        flightList = filtering.filter(flightList);

        assertEquals(1, flightList.size());
        assertEquals(flight2, flightList.get(0));

    }

    /*
    Тест фильтрации перелетов у которых общее время, проведенное на земле, превышает 2 часа.
    */
    @Test
    public void testFilteringIdleTimeMoreThan2Hours() {

        Segment mainSegment = new Segment(DATE_NOW.plusHours(1), DATE_NOW.plusHours(2));

        // Разница 1:59
        Segment segment1 = new Segment(DATE_NOW.plusHours(3).plusMinutes(59), DATE_NOW.plusHours(4));

        // Разница 2:01
        Segment segment2 = new Segment(DATE_NOW.plusHours(4).plusMinutes(2), DATE_NOW.plusHours(4));

        // Разница 1 день
        Segment segment3 = new Segment(DATE_NOW.plusDays(1), DATE_NOW.plusDays(2));

        // В сумме менее 2 часов
        Flight flight1 = new Flight(Arrays.asList(mainSegment, segment1));

        // В сумме более 2 часов
        Flight flight2 = new Flight(Arrays.asList(mainSegment, segment1, segment2));
        Flight flight3 = new Flight(Arrays.asList(mainSegment, segment2));
        Flight flight4 = new Flight(Arrays.asList(mainSegment, segment3));

        Predicate<Flight> predicate = Main.makeIdleTimeMoreThanNHoursPredicate(2);
        assertTrue(predicate.test(flight1));

        assertFalse(predicate.test(flight2));
        assertFalse(predicate.test(flight3));
        assertFalse(predicate.test(flight4));

        List<Flight> flightList = Arrays.asList(flight1, flight2, flight3, flight4);

        assertEquals(4, flightList.size());

        filtering.addPredicate(predicate);
        flightList = filtering.filter(flightList);

        assertEquals(1, flightList.size());
        assertEquals(flightList.get(0), flight1);

    }

}
