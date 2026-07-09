package com.claimsplatform.aiservice.service;

import com.claimsplatform.aiservice.dto.ChatRequest;
import com.claimsplatform.aiservice.dto.ChatResponse;
import com.claimsplatform.aiservice.entity.ChatHistory;
import com.claimsplatform.aiservice.repository.ChatHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock private ChatClient.Builder chatClientBuilder;
    @Mock private ChatHistoryRepository chatHistoryRepository;

    @InjectMocks
    private RagService ragService;

    @BeforeEach
    void setUp() {
        // No API key = demo mode
        ReflectionTestUtils.setField(ragService, "apiKey", "");
    }

    @Test
    @DisplayName("chat - demo mode returns fraud response for fraud keyword")
    void chat_demoMode_fraudKeyword_returnsFraudResponse() {
        ChatRequest request = new ChatRequest();
        request.setMessage("Show me fraud analysis for suspicious claims");

        ChatResponse response = ragService.chat(request, "adjuster@desjardins.com");

        assertThat(response.getResponse()).containsIgnoringCase("fraud");
        assertThat(response.getSourceDocs()).anyMatch(s -> s.contains("Demo Mode"));
        assertThat(response.getSessionId()).isNotBlank();
    }

    @Test
    @DisplayName("chat - demo mode returns coverage info for coverage keyword")
    void chat_demoMode_coverageKeyword_returnsCoverageResponse() {
        ChatRequest request = new ChatRequest();
        request.setMessage("What are the coverage options?");

        ChatResponse response = ragService.chat(request, "user@example.com");

        assertThat(response.getResponse()).containsIgnoringCase("coverage");
    }

    @Test
    @DisplayName("chat - demo mode returns summary for summary keyword")
    void chat_demoMode_summaryKeyword_returnsSummaryResponse() {
        ChatRequest request = new ChatRequest();
        request.setMessage("Give me a summary of recent claims");

        ChatResponse response = ragService.chat(request, "user@example.com");

        assertThat(response.getResponse()).containsIgnoringCase("summary");
    }

    @Test
    @DisplayName("chat - demo mode returns process info for how keyword")
    void chat_demoMode_processKeyword_returnsProcessResponse() {
        ChatRequest request = new ChatRequest();
        request.setMessage("How does claims processing work?");

        ChatResponse response = ragService.chat(request, "user@example.com");

        assertThat(response.getResponse()).containsIgnoringCase("submission").or()
                .containsIgnoringCase("process");
    }

    @Test
    @DisplayName("chat - reuses provided sessionId")
    void chat_reusesProvidedSessionId() {
        ChatRequest request = new ChatRequest();
        request.setMessage("Hello");
        request.setSessionId("existing-session-123");

        ChatResponse response = ragService.chat(request, "user@example.com");

        assertThat(response.getSessionId()).isEqualTo("existing-session-123");
    }

    @Test
    @DisplayName("chat - generates sessionId when not provided")
    void chat_generatesSessionId_whenNotProvided() {
        ChatRequest request = new ChatRequest();
        request.setMessage("Hello");

        ChatResponse response = ragService.chat(request, "user@example.com");

        assertThat(response.getSessionId()).isNotBlank();
    }

    @Test
    @DisplayName("chat - persists chat history to repository")
    void chat_persistsChatHistory() {
        ChatRequest request = new ChatRequest();
        request.setMessage("Any water damage claims?");
        request.setClaimId(5L);

        ragService.chat(request, "adjuster@intact.ca");

        ArgumentCaptor<ChatHistory> captor = ArgumentCaptor.forClass(ChatHistory.class);
        verify(chatHistoryRepository).save(captor.capture());
        ChatHistory saved = captor.getValue();
        assertThat(saved.getUserEmail()).isEqualTo("adjuster@intact.ca");
        assertThat(saved.getUserMessage()).isEqualTo("Any water damage claims?");
        assertThat(saved.getClaimId()).isEqualTo(5L);
    }
}
