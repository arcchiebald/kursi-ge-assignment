package ge.kursi.settlement_funding.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ge.kursi.settlement_funding.dto.FundingRequestAuditResponse;
import ge.kursi.settlement_funding.dto.FundingRequestResponse;
import ge.kursi.settlement_funding.dto.SettlementRequest;
import ge.kursi.settlement_funding.service.FundingRequestService;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController 
@RequestMapping("api/v1/settlement")
public class SettlementController {

    private final FundingRequestService fundingRequestService;

    public SettlementController(FundingRequestService fundingRequestService) {
        this.fundingRequestService = fundingRequestService;
    }

    @PostMapping("/fund")
    public ResponseEntity<FundingRequestResponse> fund(@Valid @RequestBody SettlementRequest request) {
        FundingRequestResponse response = fundingRequestService.fund(request);

        if (response.selectedInstructions().isEmpty()) {
            return ResponseEntity.ok(response);
        }

        URI locUri = URI.create(
                "/api/v1/settlement/" + response.requestId()
        );
        
        return ResponseEntity.created(locUri).body(response);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<FundingRequestResponse> findById(@PathVariable UUID requestId) {
        return ResponseEntity.ok(fundingRequestService.findById(requestId));
    }
    
    @GetMapping()
    public ResponseEntity<Page<FundingRequestAuditResponse>> findAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
            fundingRequestService.findAll(pageable)
        );
    }
    
}
