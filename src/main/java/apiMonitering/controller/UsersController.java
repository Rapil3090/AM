package apiMonitering.controller;

import apiMonitering.domain.Users;
import apiMonitering.dto.create.CreateUserDTO;
import apiMonitering.service.UsersService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @PostMapping("/create")
    public ResponseEntity<Users> createUsers(
            @Valid @RequestBody CreateUserDTO.Request request
            ) {

        return ResponseEntity.ok(usersService.createUser(request));
    }
}
