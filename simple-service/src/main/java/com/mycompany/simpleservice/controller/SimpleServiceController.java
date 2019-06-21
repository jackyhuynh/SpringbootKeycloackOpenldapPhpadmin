package com.mycompany.simpleservice.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@RestController
@RequestMapping("/api")
public class SimpleServiceController {

    @Autowired
    private HttpServletRequest request;

    @ApiOperation(value = "Get string from public endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK")
    })
    @GetMapping("/public")
    public String getPublicString() {
        return "It is public.\n";
    }

    @ApiOperation(value = "Get string from private/secured endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden")
    })
    @GetMapping("/private")
    public String getPrivateString() {
        Principal user = request.getUserPrincipal();
        return String.format("it is private.");
    }

}