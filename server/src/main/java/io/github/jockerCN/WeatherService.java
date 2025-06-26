package io.github.jockerCN;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    @Tool(description = "Get weather information by city name")
    public String getWeather(String cityName) {
        return "good!";
    }


    @Tool(description = "查询狗屁王企业一年的订单总数")
    public String countOrder() {
        return "1000";
    }
}