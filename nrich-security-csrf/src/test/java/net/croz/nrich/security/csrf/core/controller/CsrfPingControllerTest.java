package net.croz.nrich.security.csrf.core.controller;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CsrfPingControllerTest {

    @CsvSource({ ",/nrich/csrf/ping", "/api/csrf/ping,/api/csrf/ping" })
    @ParameterizedTest
    void shouldReturnPingRequest(String endpointPath, String uri) throws Exception {
        // given
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(CsrfPingController.class).addPlaceholderValue("nrich.security.csrf.endpoint-path", endpointPath).build();

        // when
        ResultActions result = mockMvc.perform(post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }
}
