package com.careround.ai.client;

import com.careround.shared.exception.AiServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AiServiceClientTest {

    private static final String BASE_URL = "http://localhost:8000";

    private AiServiceClient client;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        WebClient webClient = mock(WebClient.class);
        client = new AiServiceClient(builder.build(), webClient);
    }

    @Test
    void isReady_returnsTrue_whenStatusIsReady() {
        mockServer.expect(requestTo(BASE_URL + "/health"))
                .andRespond(withSuccess("{\"status\":\"ready\"}", MediaType.APPLICATION_JSON));

        assertThat(client.isReady()).isTrue();
        mockServer.verify();
    }

    @Test
    void isReady_returnsFalse_whenStatusIsLoading() {
        mockServer.expect(requestTo(BASE_URL + "/health"))
                .andRespond(withSuccess("{\"status\":\"loading\"}", MediaType.APPLICATION_JSON));

        assertThat(client.isReady()).isFalse();
        mockServer.verify();
    }

    @Test
    void isReady_returnsFalse_onConnectionRefused() {
        mockServer.expect(requestTo(BASE_URL + "/health"))
                .andRespond(withException(new IOException("Connection refused")));

        assertThat(client.isReady()).isFalse();
        mockServer.verify();
    }

    @Test
    void streamVoiceNote_throwsAiServiceUnavailable_whenNotReady() {
        mockServer.expect(requestTo(BASE_URL + "/health"))
                .andRespond(withSuccess("{\"status\":\"loading\"}", MediaType.APPLICATION_JSON));

        MockMultipartFile audio = new MockMultipartFile("audio", "voice.m4a",
                "audio/mp4", "audio-bytes".getBytes());

        assertThatThrownBy(() -> client.streamVoiceNote(audio, "patient-1", null, "ward_round"))
                .isInstanceOf(AiServiceUnavailableException.class)
                .hasMessageContaining("not ready");

        mockServer.verify();
    }
}
