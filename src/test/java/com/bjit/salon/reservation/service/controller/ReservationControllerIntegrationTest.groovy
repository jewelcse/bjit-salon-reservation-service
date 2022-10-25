package com.bjit.salon.reservation.service.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.anyInt
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerIntegrationTest extends Specification {


    @Autowired
    private MockMvc mockMvc;

    def "should create a new reservation"(){

        given:
        String content = "{\n" +
                "    \"staffId\":3,\n" +
                "    \"consumerId\":11,\n" +
                "    \"reservationDate\":\"2025-12-20\",\n" +
                "    \"startTime\":\"14:30:00\",\n" +
                "    \"paymentMethod\":\"CARD\",\n" +
                "    \"services\":[\n" +
                "        {\n" +
                "        \"name\":\"Normal Hair Cut\",\n" +
                "        \"description\":\"Normal Hair Cut\",\n" +
                "        \"approximateTimeForCompletion\":60,\n" +
                "        \"payableAmount\":100.00\n" +
                "        }\n" +
                "    ]\n" +
                "}"


        expect:
        mockMvc.perform(post("/api/v1/reservations")
                .content(content).contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().is2xxSuccessful());

    }

    def "should return all the reservations by staff id"(){
        expect:
        mockMvc.perform(get("/api/v1/reservations/staff/3"))
                .andExpect(status().isOk())
                .andDo(print());

    }


    def "should update the reservation status"(){

        given:
        String content = "{\n" +
                "    \"id\":1,\n" +
                "    \"staffId\":3,\n" +
                "    \"status\":\"ALLOCATED\"\n" +
                "}"


        expect:
        mockMvc.perform(post("/api/v1/reservations/starts")
                .content(content).contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().is2xxSuccessful());

    }


}