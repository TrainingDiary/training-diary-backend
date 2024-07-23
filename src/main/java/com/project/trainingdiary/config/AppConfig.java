package com.project.trainingdiary.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
public class AppConfig {

  /**
   * 애플리케이션의 기본 시간대를 Asia/Seoul로 설정합니다. 이 메서드는 빈이 생성된 후 호출됩니다.
   */
  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    log.info("기본 시간대를 Asia/Seoul로 설정했습니다.");
  }

  /**
   * LocalDate 및 LocalDateTime에 대한 커스텀 직렬화기 및 역직렬화기를 설정합니다. 이 모듈은 Jackson ObjectMapper에 등록됩니다.
   */
  @Bean
  public Module jacksonModule() {
    SimpleModule simpleModule = new SimpleModule();

    // LocalDate
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
    simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));

    // LocalDateTime
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    simpleModule.addDeserializer(LocalDateTime.class,
        new LocalDateTimeDeserializer(dateTimeFormatter));
    simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));

    return simpleModule;
  }

  /**
   * 비밀번호 암호화를 위한 Encoder를 Bean으로 등록합니다.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * QueryDSL에서 사용하는 JPAQueryFactory를 Bean으로 등록합니다.
   */
  @Bean
  public JPAQueryFactory jpaQueryFactory(EntityManager em) {
    return new JPAQueryFactory(em);
  }
}