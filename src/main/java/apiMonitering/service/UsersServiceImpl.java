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
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;

    public Users createUser(CreateUserDTO.Request request) {

        usersRepository.findByName(request.getName())
                .orElseThrow(() -> new RuntimeException("이름이 이미 있습니다"));

        return usersRepository.save(Users.builder()
                .name(request.getName())
                .build());
    }
}
