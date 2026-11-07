package dev.extramix.web;

import dev.extramix.bank.Bank;
import dev.extramix.bank.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(Bank.class)
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void createAccount() throws Exception {
        OpenAccountRequest payload = new OpenAccountRequest("a1", Currency.USD);
        mockMvc.perform(post("/accounts").contentType(MediaType.APPLICATION_JSON)
                                         .content(jsonMapper.writeValueAsString(payload)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value("a1"))
               .andExpect(jsonPath("$.currency").value("USD"))
               .andExpect(jsonPath("$.balance").value(0));
    }
}