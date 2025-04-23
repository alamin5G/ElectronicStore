package com.goonok.electronicstore.config; // Or your preferred config package

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean // Exposes this instance as a Spring bean
    public ModelMapper modelMapper() {
        // You can add custom configuration to the mapper here if needed
        // ModelMapper modelMapper = new ModelMapper();
        // modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT); // Example config
        return new ModelMapper();
    }
}