package apiMonitering.service;

import apiMonitering.domain.Users;
import apiMonitering.dto.create.CreateUserDTO;

public interface UsersService {

    Users createUser(CreateUserDTO.Request request);
}
