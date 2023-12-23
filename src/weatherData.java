import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// Backend Class that finds weather data given the string of location using API open-meteo
public class weatherData {

    // Attempts to connect to API and returns connection object if successful
    private static HttpURLConnection getAPIResponse(String urlString){
        try{
            // Try to create connection with GET request method for API
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return connection;
        }
        catch(IOException e) { e.printStackTrace(); }
        return null; // Failed, return null
    }

    // Gets current time and turns it into string format that can be used by API
    private static String getCurrentTime(){
        DateTimeFormatter apiFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'"); // API Format
        LocalDateTime currentDate = LocalDateTime.now();
        return currentDate.format(apiFormat);
    }

    // Finds location data from given location name
    public static JSONArray getLocationData(String locationName){
        // Replacing  whitespace with +'s due to APIs format and creating URL string with it
        locationName = locationName.replaceAll(" ", "+");
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + locationName + "&count=10&language=en&format=json";
        try{
            HttpURLConnection connection = getAPIResponse(urlString);
            if(connection.getResponseCode() != 200){ //Response that doesn't equal 200 means failure
                System.out.println("Error: Could not connect to API");
                return null;
            }
            else{
                // Storing results using input stream on Scanner
                StringBuilder results = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while(scanner.hasNext()){ // Loop until nothing left
                    results.append(scanner.nextLine());
                }
                scanner.close();
                connection.disconnect();
                // Creating JSON array for location info using JSON object parsed from results string
                JSONParser parser = new JSONParser();
                JSONObject resultsJSONObject = (JSONObject) parser.parse(String.valueOf(results));
                JSONArray locationData = (JSONArray) resultsJSONObject.get("results");
                return locationData;
            }
        }
        catch(Exception e) { e.printStackTrace(); }

        return null; // Couldn't find location, return null
    }

    // Builds JSONObject storing all weather information using data gathered from location
    public static JSONObject getWeatherData(String locationName){
        // Uses location data to create a URL string with appropriate latitude and longitude
        JSONArray locationData = getLocationData(locationName);
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=America%2FLos_Angeles";
        // Same procedure as in getLocationData
        try{
            HttpURLConnection connection = getAPIResponse(urlString);
            if(connection.getResponseCode() != 200){
                System.out.println("Error: Could not connect to API");
                return null;
            }
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(connection.getInputStream());
            while(scanner.hasNext()){
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            connection.disconnect();
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // Getting time Index by going through time data until current time is reached; Index is then used to match other data
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            JSONArray time = (JSONArray) hourly.get("time");
            String currentTime = getCurrentTime();
            int index = 0;
            for(int i = 0; i < time.size(); i++){
                if(((String) time.get(i)).equalsIgnoreCase(currentTime)) index = i;
            }

            // Getting temperature
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            // Getting weather code and convert it using made function
            JSONArray weathercode = (JSONArray) hourly.get("weathercode");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            // Getting humidity
            JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // Getting wind speed
            JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
            double windspeed = (double) windspeedData.get(index);

            // Weather JSONObject that is passed onto Frontend
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);
            return weatherData;
        }
        catch(Exception e) { e.printStackTrace(); }
        return null;
    }

    // Turns weather code into known state and returns that as a string
    private static String convertWeatherCode(long weathercode){
        if(weathercode == 0L) return "Clear";
        else if(weathercode > 0L && weathercode <= 3L) return "Cloudy";
        else if((weathercode >= 51L && weathercode <= 67L)  || (weathercode >= 80L && weathercode <= 99L)) return "Raining";
        else if(weathercode >= 71L && weathercode <= 77L) return "Snowing";
        return null;
    }
}
