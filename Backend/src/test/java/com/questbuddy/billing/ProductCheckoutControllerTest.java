package com.questbuddy.billing;

import com.questbuddy.user.model.User;
import com.questbuddy.user.repository.UserRepository;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ProductCheckoutController.class)
public class ProductCheckoutControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private StripeService stripeService;

    @Test
    public void testSuccessEndpoint_userBecomesPremium() throws Exception {
        User u = new User();
        u.setId(5L);
        u.setPremiumUser(false);

        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        when(userRepository.save(any())).thenReturn(u);

        mvc.perform(MockMvcRequestBuilders.get("/api/v15/payments/success/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"success\"}"));

        verify(userRepository).save(u);
    }

    @Test
    public void testSuccessEndpoint_userAlreadyPremium() throws Exception {
        User u = new User();
        u.setId(5L);
        u.setPremiumUser(true);

        when(userRepository.findById(5L)).thenReturn(Optional.of(u));

        mvc.perform(MockMvcRequestBuilders.get("/api/v15/payments/success/5"))
                .andExpect(status().isOk());

        verify(userRepository, never()).save(any());
    }

    @Test
    public void testCancelEndpoint() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/v15/payments/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"payment_canceled\"}"));
    }

    @Test
    public void testCheckoutProducts_success() throws Exception {
        StripeResponse mockResp = new StripeResponse("success", "ok", "sess123", "http://url");
        when(stripeService.checkout(any(), eq(5L))).thenReturn(mockResp);

        mvc.perform(MockMvcRequestBuilders.post("/api/v15/payments/checkout/premium/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":500,\"quantity\":1,\"productName\":\"Test\",\"currency\":\"usd\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.sessionId").value("sess123"));
    }
}

