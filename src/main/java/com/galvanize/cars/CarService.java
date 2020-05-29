package com.galvanize.cars;

import org.springframework.stereotype.Service;

@Service
public class CarService {

    CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Car getCarDetails(String name) {
        Car car = carRepository.findCarByName(name);
        return car;
    }
}
