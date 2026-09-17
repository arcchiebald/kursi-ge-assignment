package ge.kursi.settlement_funding.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ge.kursi.settlement_funding.model.FundingInstruction;

public interface FundingInstructionRepository extends JpaRepository<FundingInstruction, Long> {
    List<FundingInstruction> findBySelected(boolean selected);
}
