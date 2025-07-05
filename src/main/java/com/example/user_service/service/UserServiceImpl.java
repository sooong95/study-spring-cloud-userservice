package com.example.user_service.service;

import com.example.user_service.client.OrderServiceClient;
import com.example.user_service.dto.UserDto;
import com.example.user_service.entity.UserEntity;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.vo.ResponseOrder;
import feign.FeignException;
import feign.FeignException.FeignClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Environment env;
    private final RestTemplate restTemplate;
    private final OrderServiceClient orderServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(userEntity);

        return mapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        /*if (userEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }*/

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

//        List<ResponseOrder> orders = new ArrayList<>();
        /**
         * RestTemplate 사용 방법
          */
        /*String orderUrl = String.format("http://order-service/order-service/%s/orders", userId);
        ResponseEntity<List<ResponseOrder>> orderListResponse =
                restTemplate.exchange(orderUrl, HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<ResponseOrder>>() {
        });
        List<ResponseOrder> orders = orderListResponse.getBody();*/

        /**
         * feign client 사용 방법
         */
        /*List<ResponseOrder> orders = null;
        try {

            orders = orderServiceClient.getOrders(userId);

        } catch (FeignClientException e) {
            log.error(e.getMessage());
        }*/
        /**
         * ErrorDecoder
         */
        /*List<ResponseOrder> orders = orderServiceClient.getOrders(userId);*/

        /**
         * circuit breaker 사용
         */
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        List<ResponseOrder> orders = circuitbreaker.run(() -> orderServiceClient.getOrders(userId),
                throwable -> new ArrayList<>());

        userDto.setOrders(orders);
        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }
}
