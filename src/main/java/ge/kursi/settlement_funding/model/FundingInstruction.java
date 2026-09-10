package ge.kursi.settlement_funding.model;

import jakarta.persistence.*;

@Entity 
@Table(
    name = "funding_instructions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_funding_instructions_request_order",
            columnNames = {"request_id", "instruction_order"}
        )
    },
    indexes = {
        @Index(
            name = "idx_funding_instructions_request_selected_order",
            columnList = "request_id, is_selected, instruction_order"
        )
    }
)
public class FundingInstruction {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private FundingRequest request;

    @Column(name = "instruction_reference", nullable = false, length = 255)
    private String instructionReference;

    @Column(name = "instruction_amount", nullable = false)
    private Long instructionAmount;

    @Column(name = "expected_fee", nullable = false)
    private Long expectedFee;

    @Column(name = "is_selected", nullable = false)
    private boolean selected;

    @Column(name = "instruction_order", nullable = false)
    private Integer instructionOrder;

    protected FundingInstruction() {
    }

    public Long getId() {
        return id;
    }

    public FundingRequest getRequest() {
        return request;
    }

    public void setRequest(FundingRequest request) {
        this.request = request;
    }

    public String getInstructionReference() {
        return instructionReference;
    }

    public void setInstructionReference(String instructionReference) {
        this.instructionReference = instructionReference;
    }

    public Long getInstructionAmount() {
        return instructionAmount;
    }

    public void setInstructionAmount(Long instructionAmount) {
        this.instructionAmount = instructionAmount;
    }

    public Long getExpectedFee() {
        return expectedFee;
    }

    public void setExpectedFee(Long expectedFee) {
        this.expectedFee = expectedFee;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Integer getInstructionOrder() {
        return instructionOrder;
    }

    public void setInstructionOrder(Integer instructionOrder) {
        this.instructionOrder = instructionOrder;
    }
}
