package k1per32;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;

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

                boolean isDepartureWeatherOk = isWeatherFlightworthy(departureTime, fromCity, forecastObject);
                boolean isArrivalWeatherOk = isWeatherFlightworthy(arrivalTime, toCity, forecastObject);

                String status = (isDepartureWeatherOk && isArrivalWeatherOk) ? "по расписанию" : "отменен";
                System.out.println(no + " | " + fromCity + " -> " + toCity + " | " + status);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isWeatherFlightworthy(LocalTime time, String city, JSONObject forecastObject) {
        if (forecastObject.has(city)) {
            JSONArray cityForecasts = forecastObject.getJSONArray(city);
            for (int i = 0; i < cityForecasts.length()-1; i++) {
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
}