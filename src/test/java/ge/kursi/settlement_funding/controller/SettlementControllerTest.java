package ge.kursi.settlement_funding.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import tools.jackson.databind.json.JsonMapper;

import ge.kursi.settlement_funding.dto.FundingInstructionResponse;
import ge.kursi.settlement_funding.dto.FundingRequestResponse;
import ge.kursi.settlement_funding.service.FundingRequestNotFoundException;
import ge.kursi.settlement_funding.service.FundingRequestService;

@ExtendWith(MockitoExtension.class)
class SettlementControllerTest {

    @Mock
    private FundingRequestService fundingRequestService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SettlementController controller = new SettlementController(fundingRequestService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(JsonMapper.builder().build()))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void fundReturnsCreatedAndLocation() throws Exception {
        UUID requestId = UUID.randomUUID();
        FundingRequestResponse response = new FundingRequestResponse(
                requestId,
                List.of(new FundingInstructionResponse("INS-1", 7_000L, 150L)),
                7_000L,
                150L,
                OffsetDateTime.parse("2026-09-07T09:00:00Z")
        );
        when(fundingRequestService.fund(any())).thenReturn(response);

        String body = """
                {
                  "availableSettlementBalance": 20000,
                  "candidateInstructions": [
                    {
                      "instructionReference": "INS-1",
                      "instructionAmount": 7000,
                      "expectedFee": 150
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/settlement/fund")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/settlement/" + requestId));
    }

    @Test
    void findByIdReturnsOk() throws Exception {
        UUID requestId = UUID.randomUUID();
        when(fundingRequestService.findById(requestId)).thenReturn(
                new FundingRequestResponse(
                        requestId,
                        List.of(),
                        0L,
                        0L,
                        OffsetDateTime.parse("2026-09-07T09:00:00Z")
                ));

        mockMvc.perform(get("/api/v1/settlement/{requestId}", requestId))
                .andExpect(status().isOk());
    }

    @Test
    void findAllReturnsOk() throws Exception {
        when(fundingRequestService.findAll(any())).thenReturn(
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/v1/settlement"))
                .andExpect(status().isOk());
    }

    @Test
    void fundReturnsOkWhenNoInstructionsFit() throws Exception {
        UUID requestId = UUID.randomUUID();
        FundingRequestResponse response = new FundingRequestResponse(
                requestId,
                List.of(),
                0L,
                0L,
                OffsetDateTime.parse("2026-09-07T09:00:00Z")
        );
        when(fundingRequestService.fund(any())).thenReturn(response);

        String body = """
                {
                  "availableSettlementBalance": 100,
                  "candidateInstructions": [
                    {
                      "instructionReference": "INS-1",
                      "instructionAmount": 7000,
                      "expectedFee": 150
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/settlement/fund")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.selectedInstructions").isEmpty())
                .andExpect(jsonPath("$.totalExpectedFee").value(0));
    }

    @Test
    void fundReturnsBadRequestWhenCandidateInstructionsMissing() throws Exception {
        String body = """
                {
                  "availableSettlementBalance": 20000,
                  "candidateInstructions": []
                }
                """;

        mockMvc.perform(post("/api/v1/settlement/fund")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.candidateInstructions").exists());
    }

    @Test
    void fundReturnsBadRequestWhenInstructionAmountIsNotPositive() throws Exception {
        String body = """
                {
                  "availableSettlementBalance": 20000,
                  "candidateInstructions": [
                    {
                      "instructionReference": "INS-1",
                      "instructionAmount": 0,
                      "expectedFee": 150
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/settlement/fund")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByIdReturnsNotFoundWhenMissing() throws Exception {
        UUID requestId = UUID.randomUUID();
        when(fundingRequestService.findById(requestId))
                .thenThrow(new FundingRequestNotFoundException("Funding request not found: " + requestId));

        mockMvc.perform(get("/api/v1/settlement/{requestId}", requestId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
