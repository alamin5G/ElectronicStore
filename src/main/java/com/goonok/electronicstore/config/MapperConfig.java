package com.goonok.electronicstore.config; // Or your preferred config package

import com.goonok.electronicstore.dto.ContactMessageDto;
import com.goonok.electronicstore.model.ContactMessage;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Skip 'subject' field when mapping from DTO to entity
        modelMapper.typeMap(ContactMessageDto.class, ContactMessage.class)
                .addMappings(mapper -> mapper.skip(ContactMessage::setSubject));

        // Get the existing typeMap and add all mappings to it
        modelMapper.typeMap(ContactMessage.class, ContactMessageDto.class)
                // Set a default value for 'subject'
                .addMappings(mapper -> mapper.map(src -> "Inquiry #" + src.getId(), ContactMessageDto::setSubject))
                // Add admin mappings to the SAME typeMap
                .addMappings(mapper -> {
                    // Map assignedAdmin.adminId to assignedAdminId
                    mapper.map(src -> src.getAssignedAdmin() != null ?
                                    src.getAssignedAdmin().getAdminId() : null,
                            ContactMessageDto::setAssignedAdminId);

                    // Map assignedAdmin.user.name to assignedAdminName
                    mapper.map(src -> src.getAssignedAdmin() != null &&
                                    src.getAssignedAdmin().getUser() != null ?
                                    src.getAssignedAdmin().getUser().getName() : null,
                            ContactMessageDto::setAssignedAdminName);
                });

        return modelMapper;
    }
}