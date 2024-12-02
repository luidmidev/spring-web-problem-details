package io.github.luidmidev.springframework.web.problemdetails.test;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(properties = {
        "spring.web.problemdetails.all-errors=false",
        "spring.web.problemdetails.log-errors=true",
        "spring.web.problemdetails.send-stack-trace=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TestControllerProblemDetailsTest {

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
    void errorWithRuntimeException() throws Exception {

        var result = mockMvc.perform(get("/bad-request-runtime"))
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

        var result = mockMvc.perform(get("/endpoint-with-param").param("param", "io/github/luidmidev/springframework/web/problemdetails/test"))
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

    @Test
    void requestParts() throws Exception {

        var part1 = new MockPart("part1", null, "Parte 1".getBytes());
        var part2 = new MockPart("part2", null, "[\"item1\", \"item2\"]".getBytes());
        part2.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        var part3 = new MockMultipartFile(
                "part3",
                "test-file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Contenido del archivo".getBytes()
        );

        var result = mockMvc.perform(
                        multipart("/request-parts")
                                .part(part1, part2)
                                .file(part3))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());

    }
}