package com.spring.testing.rest;

import com.spring.testing.service.IsolationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/isolation")
public class IsolationController {

    private final IsolationService isolationService;

    @Autowired
    public IsolationController(IsolationService isolationService) {
        this.isolationService = isolationService;
    }

    @PutMapping("/one")
    public String exampleOne(@RequestParam(name = "is-read-committed") boolean isReadCommitted) {
        return isolationService.exampleOne(isReadCommitted);
    }

    @PutMapping("/two")
    public String exampleTwo(@RequestParam(name = "is-repeatable-read") boolean isRepeatableRead) {
        return isolationService.exampleTwo(isRepeatableRead);
    }

    @PutMapping("/three")
    public String exampleThree(
            @RequestParam(name = "is-serializable") boolean isSerializable,
            @RequestParam(name = "is-mysql") boolean isMySQL
    ) {
        return isolationService.exampleThree(isSerializable, isMySQL);
    }

}
