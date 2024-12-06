package com.goonok.electronicstore.services;


import com.goonok.electronicstore.dto.EditUserByAdminDto;
import com.goonok.electronicstore.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EditUserByAdminDtoService {

    private final EditUserByAdminDto editUserByAdminDto = new EditUserByAdminDto();

    @Autowired
    private UserService userService;

    //show user information by id from the admin menu
    public EditUserByAdminDto showEditUserInformationByAdminDto(int userId) {
        User user = userService.findById(userId);
        editUserByAdminDto.setId(userId);
        editUserByAdminDto.setEmail(user.getEmail());
        editUserByAdminDto.setPhone(user.getPhone());
        editUserByAdminDto.setEnabled(user.isEnabled());
        return editUserByAdminDto;
    }


}
