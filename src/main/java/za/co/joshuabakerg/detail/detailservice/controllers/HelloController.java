package za.co.joshuabakerg.detail.detailservice.controllers;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {


    @GetMapping("/me")
    public Object helloWorld(final UsernamePasswordAuthenticationToken principal){
        return principal;
    }

}
