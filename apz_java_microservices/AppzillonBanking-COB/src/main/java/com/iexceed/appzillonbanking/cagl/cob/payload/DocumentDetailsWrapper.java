package com.iexceed.appzillonbanking.cagl.cob.payload;

public record DocumentDetailsWrapper(DocumentEntryDto documentDetails) {
    public static DocumentDetailsWrapper of(DocumentEntryDto documentDetails) {
        return new DocumentDetailsWrapper(documentDetails);
    }
}