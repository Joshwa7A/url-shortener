package com.joshwa.urlshortener.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static com.joshwa.urlshortener.utility.UrlUtils.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ShortUrlControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_SHORTURL_JSONREQUEST =
            """
                    {
                      "originalUrl": "animal.com",
                      "expiryDate": null
                    }
            """;
    private static final String INVALID_SHORTURL_JSONREQUEST =
                     """
                    {
                      "originalUrl": ".io*/ls/",
                      "expiryDate": null
                    }
                    """;
    @Container
    static MySQLContainer<?> mysql=
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }




    @Test
    void shouldCreateAndRedirectSuccessfully_endToEndFlow() throws Exception {

         String response= mockMvc.perform(post("/api/v1/short-urls")
                .contentType(MediaType.APPLICATION_JSON).content(VALID_SHORTURL_JSONREQUEST))
                 .andExpect(status().isCreated())
                 .andReturn().getResponse().getContentAsString();
         Assertions.assertNotNull(response);

        String shortCode = JsonPath.read(response, "$.shortCode");

        mockMvc.perform(get("/"+shortCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "https://animal.com"));

        mockMvc.perform(get("/api/v1/short-urls/"+shortCode+"/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clickCount").value(1));
    }
    @Test
    void shouldReturnBadRequest_whenInvalidUrlProvided() throws Exception {
        mockMvc.perform(post("/api/v1/short-urls")
                        .contentType(MediaType.APPLICATION_JSON).content(INVALID_SHORTURL_JSONREQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(INVALID_URL_EXCEPTION_MESSAGE));

    }

    @Test
    void shouldReturnNotFound_whenShortCodeDoesNotExist() throws Exception {
        mockMvc.perform(get("/"+"randomInvalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(SHORT_URL_NOT_FOUND_EXCEPTION_MESSAGE));
    }

}