package io.github.luidmidev.springframework.errors.test;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(
        properties = {
                "io.github.luidmidev.springframework.errors.all-errors=true",
                "io.github.luidmidev.springframework.errors.log-errors=true",
                "io.github.luidmidev.springframework.errors.send-stack-trace=true"
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TestControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Test
    void errorWithException() throws Exception {

        var result = mockMvc.perform(get("/bad-request"))
                .andExpect(status().isBadRequest())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    void errorWithExceptionByExtension() throws Exception {

        var result = mockMvc.perform(get("/bad-request-by-extension"))
                .andExpect(status().isBadRequest())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    void endpointWithParam() throws Exception {

        var result = mockMvc.perform(get("/endpoint-with-param").param("param", "test"))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    void endpointWithParamWithoutParam() throws Exception {

        var result = mockMvc.perform(get("/endpoint-with-param")
                        .header("Accept-Language", "es")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value(Matchers.containsString("param")))
                .andReturn();

        System.out.println("Response: " + result.getResponse().getContentAsString());
    }
}