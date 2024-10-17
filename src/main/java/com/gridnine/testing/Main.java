package com.gridnine.testing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

/*
Было бы неплохо вынести предикаты в отдельный пакет. Однако тогда для доступа к классу Flight необходимо было бы
выносить его в отедельный файл. Можно ли так делать - я не совсем понял, поэтому оставил, как есть.
*/
public class Main {

    public static void main(String[] args) {

        List<Flight> flights = FlightBuilder.createFlights();
        Filtering<Flight> filtering = new FilteringImpl();
        System.out.println(flights);

        filtering.addPredicate(new BeforeCurrentTimePredicate());
        flights = filtering.filter(flights);
        System.out.println(flights);

        filtering.addPredicate(new ArrivalBeforeDeparturePredicate());
        flights = filtering.filter(flights);
        System.out.println(flights);

        filtering.addPredicate(new IdleTimeMoreThan2HoursPredicate());
        flights = filtering.filter(flights);
        System.out.println(flights);
    }

    /*
    Фильтруем перелеты с вылетами до текущего момента времени.
    */
    static class BeforeCurrentTimePredicate implements Predicate<Flight> {

        @Override
        public boolean test(Flight flight) {
            return flight.getSegments()
                    .get(0)
                    .getDepartureDate()
                    .compareTo(LocalDateTime.now()) > 0;
        }

    }

    /*
    Фильтруем перелеты, у которых имеются сегменты с датой прилета раньше даты вылета.
    */
    static class ArrivalBeforeDeparturePredicate implements Predicate<Flight> {

        @Override
        public boolean test(Flight flight) {
            for (var segment : flight.getSegments()) {
                if (segment.getArrivalDate().compareTo(segment.getDepartureDate()) < 0) return false;
            }
            return true;
        }

    }

    /*
    Фильтруем перелеты, у которых общее время, проведенное на земле, превышает 2 часа.
    */
    static class IdleTimeMoreThan2HoursPredicate implements Predicate<Flight> {

        @Override
        public boolean test(Flight flight) {

            if (flight.getSegments().size() == 1) return true;
            int minutes = 0;

            for (int i = 0; i < flight.getSegments().size() - 1; i++) {

                var previousArrivalDate = flight.getSegments().get(i).getArrivalDate();
                var nextDepartureDate = flight.getSegments().get(i + 1).getDepartureDate();

                if (nextDepartureDate.getDayOfMonth() - previousArrivalDate.getDayOfMonth() > 0) return false;

                int minutes1 = previousArrivalDate.getHour() * 60 + previousArrivalDate.getMinute();
                int minutes2 = nextDepartureDate.getHour() * 60 + nextDepartureDate.getMinute();

                minutes += minutes2 - minutes1;

                if (minutes > 120) return false;

            }
            return true;
        }

    }
}
