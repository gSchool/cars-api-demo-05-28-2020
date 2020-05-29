package com.galvanize.cars;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
}
