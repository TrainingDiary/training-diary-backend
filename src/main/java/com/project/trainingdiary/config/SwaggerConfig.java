package com.project.trainingdiary.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
    info = @Info(
        title = "트레이닝 다이어리",
        version = "1.0",
        description = "트레이너의 일정을 예약하고, 운동을 기록합니다."
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Local Server"
        ),
        @Server(
            url = "https://trainingdiaryapi.marcel-dev.com",
            description = "Stage Server"
        )
    }
)
public class SwaggerConfig {

}
