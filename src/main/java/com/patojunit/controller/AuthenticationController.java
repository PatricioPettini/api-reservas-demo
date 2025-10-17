package com.patojunit.controller;

import com.patojunit.dto.AuthLoginRequestDTO;
import com.patojunit.dto.AuthResponseDTO;
import com.patojunit.service.UserDetailsServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserDetailsServiceImp userDetailsService;

    //Todas estas requests y responses vamos a tratarlas como dto
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthLoginRequestDTO userRequest) {
        return new ResponseEntity<>(this.userDetailsService.loginUser(userRequest), HttpStatus.OK);
    }

    @GetMapping("/hola")
    public String hola() {
        return "Hola Mundo";
    }

}

