package com.jccv.tuprivadaapp.service.user;

import com.jccv.tuprivadaapp.dto.user.UserDataToShowDto;
import com.jccv.tuprivadaapp.model.User;

import java.util.List;

public interface UserService {
    User updateUser(Long userId, UserDataToShowDto userDataToShowDto);

    List<UserDataToShowDto> getAllUsersByCondominiumId(Long condominiumId);
}