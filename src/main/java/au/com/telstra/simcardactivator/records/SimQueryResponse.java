package au.com.telstra.simcardactivator.records;

public record SimQueryResponse(
        String iccid,
        String customerEmail,
        boolean active
) { }
