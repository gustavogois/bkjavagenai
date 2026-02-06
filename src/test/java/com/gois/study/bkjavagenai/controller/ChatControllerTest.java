package com.gois.study.bkjavagenai.controller;

import com.gois.study.bkjavagenai.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChatControllerTest {

    @Test
    void shouldReturnContent() throws Exception {
        ChatService chatService = mock(ChatService.class);
        when(chatService.chat("Hello!")).thenReturn("Hello from test!");

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ChatController(chatService)).build();

        mvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userMessage\":\"Hello!\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").value("Hello from test!"));
    }
}
