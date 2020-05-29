package com.galvanize.cars;

import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository {
    Car findCarByName(String name);
}
