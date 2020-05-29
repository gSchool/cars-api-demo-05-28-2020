package com.galvanize.cars;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarController.class)
public class CarControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CarService carService;

    @Test
    void getCarDetails_exists_returnCar() throws Exception {
        when(carService.getCarDetails(anyString()))
                .thenReturn(new Car("prius", "hybrid"));

        mockMvc.perform(get("/cars/prius"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("prius"))
                .andExpect(jsonPath("type").value("hybrid"));
    }

    @Test
    void getCarDetails_notExists_returnNoContent() throws Exception {
        when(carService.getCarDetails(anyString()))
                .thenThrow(new CarNotFoundException());

        mockMvc.perform(get("/cars/nothing"))
                .andExpect(status().isNoContent());
    }
}
