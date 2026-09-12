package ge.kursi.settlement_funding.service;

public class FundingRequestNotFoundException extends RuntimeException {

    public FundingRequestNotFoundException(String message) {
        super(message);
    }
}