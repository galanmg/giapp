package com.giapp.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Activa las tareas programadas con @Scheduled (la facturación diaria)
@Configuration
@EnableScheduling
public class ProgramacionConfig {
}
