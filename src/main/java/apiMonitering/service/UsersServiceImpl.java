package apiMonitering.service;

import apiMonitering.domain.Users;
import apiMonitering.dto.create.CreateUserDTO;
import apiMonitering.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UsersServiceImpl {

    private final UsersRepository usersRepository;

    public Users createUser(CreateUserDTO request) {

        return usersRepository.save()
    }
}
