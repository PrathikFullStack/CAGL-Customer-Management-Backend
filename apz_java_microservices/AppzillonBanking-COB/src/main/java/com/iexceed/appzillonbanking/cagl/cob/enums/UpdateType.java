package com.iexceed.appzillonbanking.cagl.cob.enums;

/**
 * Discriminator for PUT /application/update.
 * Determines which section of the application payload the incoming
 * request mutates, and which {@code UpdateHandler} strategy is invoked.
 *
 * Mapped to onboarding sub-stages 1.1 -> 1.9, plus SUBMIT and BRE_QUEUE
 * (stage 2) per the FSD payload segregation.
 */
public enum UpdateType {
    OTP_CONSENT,        // 1.1
    KYC_DATA,           // 1.2
    PERSONAL_DETAILS,   // 1.3 (religion/caste/nationality/email/incomeSource)
    ADDRESS,            // 1.3 (permanent / communication address)
    FAMILY_MEMBERS,     // 1.4
    INCOME_EXPENSE,     // 1.5
    BANK_DETAILS,       // 1.6
    KENDRA_GROUP,       // 1.7
    PHOTO,              // 1.8 (member / family photo captures)
    DOCUMENTS,          // 1.9 (house / business / other photos)
    SUBMIT,             // end of stage 1 -> pushes to BRE queue
    BRE_QUEUE           // 2.1 BRE response capture
}