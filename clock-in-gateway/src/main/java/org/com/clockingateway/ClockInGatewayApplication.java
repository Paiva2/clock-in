package org.com.clockingateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class ClockInGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClockInGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route(route -> route.path("/employee/**").uri("lb://CLOCK-IN-EMPLOYEES"))
            .route(route -> route.path("/time-clock/**").uri("lb://CLOCK-IN-TIME-CLOCK"))
            .build();
    }
}
