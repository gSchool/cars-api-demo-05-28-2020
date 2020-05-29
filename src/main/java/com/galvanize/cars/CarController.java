package com.galvanize.cars;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class CarController {

    CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/cars/{name}")
    public Car getCarDetails(@PathVariable String name){
        return carService.getCarDetails(name);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void carNotFoundHandler(CarNotFoundException e){}
}
