package io.github.jockerCN;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    @Tool(description = "Get weather information by city name")
    public String getWeather(String cityName) {
        return "good!";
    }
}