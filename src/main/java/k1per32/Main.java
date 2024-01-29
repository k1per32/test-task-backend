package k1per32;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;


public class Main {

    public static void main(String[] args) {
        String pathToJson = "C:\\Users\\deman\\Desktop\\dev\\test-task-backend\\src\\main\\java\\k1per32\\flights_and_forecast.json";

        try {
            String content = new String(Files.readAllBytes(Paths.get(pathToJson)));
            JSONObject jsonObject = new JSONObject(content);
            JSONArray flightsArray = jsonObject.getJSONArray("flights");
            JSONObject forecastObject = jsonObject.getJSONObject("forecast");

            for (int i = 0; i < flightsArray.length(); i++) {
                JSONObject flight = flightsArray.getJSONObject(i);
                String no = flight.getString("no");
                String fromCity = flight.getString("from");
                String toCity = flight.getString("to");
                int timeDeparture = flight.getInt("departure");
                LocalTime departureTime = LocalTime.of(timeDeparture, 0);
                LocalTime arrivalTime = departureTime.plusHours(flight.getInt("duration"));
                int differentTimeBetweenCities = differentBetweenCityes(fromCity, toCity);

                boolean isDepartureWeatherOk = isWeatherFlightworthyDeparture(departureTime, fromCity, forecastObject);
                boolean isArrivalWeatherOk = isWeatherFlightworthyTo(arrivalTime, toCity, forecastObject, differentTimeBetweenCities);

                String status = (isDepartureWeatherOk && isArrivalWeatherOk) ? "по расписанию" : "отменен";
                System.out.println(no + " | " + fromCity + " -> " + toCity + " | " + status);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static boolean isWeatherFlightworthyDeparture(LocalTime time, String city, JSONObject forecastObject) {
        if (forecastObject.has(city)) {
            JSONArray cityForecasts = forecastObject.getJSONArray(city);
            for (int i = 0; i < cityForecasts.length() - 1; i++) {
                JSONObject weatherConditions = cityForecasts.getJSONObject(i);
                if (weatherConditions.getInt("time") == time.getHour()) {
                    int windSpeed = weatherConditions.getInt("wind");
                    int visibility = weatherConditions.getInt("visibility");
                    return windSpeed <= 30 && visibility >= 200;
                }
            }
        }
        return false;
    }

    private static boolean isWeatherFlightworthyTo(LocalTime arrivalTime, String toCity, JSONObject forecastObject, int differentTime) {
        if (forecastObject.has(toCity)) {
            JSONArray cityForecasts = forecastObject.getJSONArray(toCity);
            for (int i = 0; i < cityForecasts.length() - 1; i++) {
                JSONObject weatherConditions = cityForecasts.getJSONObject(i);
                if (weatherConditions.getInt("time")+differentTime == arrivalTime.getHour()) {
                    int windSpeed = weatherConditions.getInt("wind");
                    int visibility = weatherConditions.getInt("visibility");
                    return windSpeed <= 30 && visibility >= 200;
                }
            }
        }
        return false;
    }

    private static int differentBetweenCityes(String fromCity, String toCity) {
        ZonedDateTime fromCityTime;
        ZonedDateTime toCityTime;
        if ("moscow".equals(fromCity)) {
            fromCityTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Europe/" + String.format(StringUtils.capitalize(fromCity))));
        } else
            fromCityTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Asia/" + String.format(StringUtils.capitalize(fromCity))));

        if ("moscow".equals(toCity)) {
            toCityTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Europe/" + String.format(StringUtils.capitalize(toCity))));
        } else
            toCityTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Asia/" + String.format(StringUtils.capitalize(toCity))));

        return fromCityTime.getHour() - toCityTime.getHour();

    }

}