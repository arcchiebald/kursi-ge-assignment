package ge.kursi.settlement_funding.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "funding_requests", indexes = {
        @Index(name = "idx_funding_requests_created_at", columnList = "created_at")
})
public class FundingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "available_settlement_balance", nullable = false)
    private Long availableSettlementBalance;

    @Column(name = "total_settlement_consumed", nullable = false)
    private Long totalSettlementConsumed;

    @Column(name = "total_expected_fee", nullable = false)
    private Long totalExpectedFee;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FundingInstruction> instructions = new ArrayList<>();

    protected FundingRequest() {

    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public Long getAvailableSettlementBalance() {
        return availableSettlementBalance;
    }

    public void setAvailableSettlementBalance(Long availableSettlementBalance) {
        this.availableSettlementBalance = availableSettlementBalance;
    }

    public Long getTotalSettlementConsumed() {
        return totalSettlementConsumed;
    }

    public void setTotalSettlementConsumed(Long totalSettlementConsumed) {
        this.totalSettlementConsumed = totalSettlementConsumed;
    }

    public Long getTotalExpectedFee() {
        return totalExpectedFee;
    }

    public void setTotalExpectedFee(Long totalExpectedFee) {
        this.totalExpectedFee = totalExpectedFee;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<FundingInstruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<FundingInstruction> instructions) {
        this.instructions = instructions;
    }

    public void addInstruction(FundingInstruction instruction) {
        instructions.add(instruction);
        instruction.setRequest(this);
    }

    public FundingRequest(Long availableSettlementBalance,
            OffsetDateTime createdAt) {
        this.availableSettlementBalance = availableSettlementBalance;
        this.totalSettlementConsumed = 0L;
        this.totalExpectedFee = 0L;
        this.createdAt = createdAt;
    }

}
