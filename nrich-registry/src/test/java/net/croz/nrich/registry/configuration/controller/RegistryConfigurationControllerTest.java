package net.croz.nrich.registry.configuration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import net.croz.nrich.registry.configuration.model.RegistryGroupConfiguration;
import net.croz.nrich.registry.test.BaseWebTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class RegistryConfigurationControllerTest extends BaseWebTest {

    @SneakyThrows
    @Test
    void shouldFetchRegistryConfiguration() {
        // when
        final MockHttpServletResponse response = mockMvc.perform(post("/nrich/registry/configuration/fetch").contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        // and when
        final List<RegistryGroupConfiguration> convertedResponse = objectMapper.readValue(response.getContentAsString(), new TypeReference<List<RegistryGroupConfiguration>>() {});

        // then
        assertThat(convertedResponse).isNotNull();
        assertThat(convertedResponse).hasSize(3);
    }

}