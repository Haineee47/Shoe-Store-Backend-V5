package com.shoestore.shared.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @PostMapping("/validation")
    void validateRequest(@Valid @RequestBody TestRequest request) {
    }

    @GetMapping("/method")
    void methodEndpoint() {
    }

    @GetMapping("/unexpected")
    void unexpectedError() {
        throw new IllegalStateException("Sensitive internal implementation detail");
    }

    @GetMapping("/application-error")
    void applicationError() {
        throw new ApplicationException(
                CommonErrorCode.RESOURCE_NOT_FOUND,
                "Test resource was not found"
        );
    }

    public record TestRequest(
            @NotBlank(message = "Name must not be blank")
            String name
    ) {}
}
