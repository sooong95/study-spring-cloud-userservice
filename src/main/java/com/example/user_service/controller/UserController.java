package com.example.user_service.controller;

import com.example.user_service.dto.UserDto;
import com.example.user_service.entity.UserEntity;
import com.example.user_service.service.UserService;
import com.example.user_service.vo.Greeting;
import com.example.user_service.vo.RequestUser;
import com.example.user_service.vo.ResponseUser;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    private final Environment env;
    private final UserService userService;

    @Autowired
    private Greeting greeting;

    @GetMapping("/health_check")
    @Timed(value = "users.status", longTask = true)
    public String status() {
        return String.format("It's Working in User Service" +
                ", port(local.server.port)= ", env.getProperty("local.server.port") +
                ", port(server.port)= ", env.getProperty("server.port"));
    }

    @GetMapping("/welcome")
    @Timed(value = "users.welcome", longTask = true)
    public String welcome() {
        /*return env.getProperty("greeting.message");*/
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody RequestUser user) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);
        userService.createUser(userDto);

        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);
        return new ResponseEntity<>(responseUser, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        Iterable<UserEntity> userList = userService.getUserByAll();
        List<ResponseUser> result = new ArrayList<>();
        userList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseUser.class));
        });
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable("userId") String userId) {
        UserDto userDto = userService.getUserByUserId(userId);

        return new ResponseEntity<>(new ModelMapper().map(userDto, ResponseUser.class), HttpStatus.OK);
    }
}
