package com.gridnine.testing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

/*
Если перелеты (Flight) хранятся в реляционной БД, то (т.к. их может очень много) можем ограничить
число записей в SQL-запросе. Если значения хранятся в каком-то файле (бинарник, текстовый), тогда
нужно читать файл по частям.
*/
public class Main {

    public static void main(String[] args) {

        List<Flight> flights = FlightBuilder.createFlights();
        Filtering<Flight> filtering = new FilteringImpl();

        // Фильтруем перелеты до текущей даты.
        filtering.addPredicate(makeBeforeTimePredicate(LocalDateTime.now()));
        flights = filtering.filter(flights);
        System.out.println(flights);

        // Фильтруем перелеты, у которых время прибытия позже времени отправления
        filtering.addPredicate(new ArrivalBeforeDeparturePredicate());
        flights = filtering.filter(flights);
        System.out.println(flights);

        // Фильтруем перелеты, у которых время простоя больше 2 часов.
        filtering.addPredicate(makeIdleTimeMoreThanNHoursPredicate(2));
        flights = filtering.filter(flights);
        System.out.println(flights);
    }

    /*
    Получаем реализацию фильтрации перелетов до определенной даты. (В задаче текущая дата)
    */
    public static Predicate<Flight> makeBeforeTimePredicate(LocalDateTime date) {

        return (Flight flight) -> {
            return flight.getSegments()
                    .get(0)
                    .getDepartureDate()
                    .compareTo(date) > 0;
        };

    }

    /*
    Фильтруем перелеты, у которых имеются сегменты с датой прилета раньше даты вылета.
    */
    static class ArrivalBeforeDeparturePredicate implements Predicate<Flight> {

        @Override
        public boolean test(Flight flight) {
            for (var segment : flight.getSegments()) {
                if (segment.getArrivalDate().compareTo(segment.getDepartureDate()) < 0) {
                    return false;
                }
            }
            return true;
        }

    }

    /*
    Получаем реализацию фильтрации перелетов, у которых общее время, проведенное на земле,
    превышает N часов. (В задаче 2 часа)
    */
    public static Predicate<Flight> makeIdleTimeMoreThanNHoursPredicate(int hours) {
        return (Flight flight) -> {

            if (flight.getSegments().size() == 1) return true;
            int minutes = 0;

            for (int i = 0; i < flight.getSegments().size() - 1; i++) {

                var previousArrivalDate =
                        flight.getSegments().get(i).getArrivalDate();
                var nextDepartureDate =
                        flight.getSegments().get(i + 1).getDepartureDate();

                int minutes1 =
                        previousArrivalDate.getDayOfMonth() * 24 * 60 +
                        previousArrivalDate.getHour() * 60 +
                        previousArrivalDate.getMinute();
                int minutes2 =
                        nextDepartureDate.getDayOfMonth() * 24 * 60 +
                        nextDepartureDate.getHour() * 60 +
                        nextDepartureDate.getMinute();

                minutes += minutes2 - minutes1;
                if (minutes > hours * 60) return false;

            }
            return true;

        };
    }
}
