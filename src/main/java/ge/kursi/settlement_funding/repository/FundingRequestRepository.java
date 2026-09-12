package ge.kursi.settlement_funding.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ge.kursi.settlement_funding.model.FundingRequest;

public interface FundingRequestRepository extends JpaRepository<FundingRequest, UUID> {
    Page<FundingRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
}