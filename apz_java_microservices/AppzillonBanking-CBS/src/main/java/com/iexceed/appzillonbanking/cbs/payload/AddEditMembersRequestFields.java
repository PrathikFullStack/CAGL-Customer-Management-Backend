package com.iexceed.appzillonbanking.cbs.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddEditMembersRequestFields {

    private List<MemberData> body;

    // ================= MAIN OBJECT =================
    public static class MemberData {
        private String fatherName;
        private String spName;
        private String joiningCbReportLink;
        private String contactDate;
        private String agriLandDry;
        private int groupId;
        private String noOfAdults;
        private String state1;
        private long recordId;
        private String bankACNo;
        private String state;
        private String sector;
        private String bankifsccode;
        private String longitude;
        private String districtId1;
        private String resident;
        private String landHolding;
        private String branchId;
        private String caste;
        private String village1;
        private String noOfChildren;
        private String taluk1;
        private String curAddress1;
        private String customerName;
        private String totalIncome;
        private String nomineeIfscCode;
        private String phone;
        private String dob;
        private String pinCode;
        private String name;
        private String joiningCbSummary;
        private String joiningDateofEnquiry;
        private List<LegalDocument> legalDocument;
        private String maritalStatus;
        private String status;
        private String customerQualify;
        private String gender;
        private String pinCode1;
        private String secondaryMobileNumber;
        private String latitude;
        private List<String> bankName;
        private String mobAppId;
        private String mobileNumberVal;
        private List<String> accountHolderName;
        private List<AddressLine> presentAddress;
        private String cbDate;
        private String custId;
        private Object member;
        private List<Nominee> nominee;
        private String village;
        private String ifscCode;
        private String spouseDob;
        private List<Kyc> spouseKyc;
        private String nominalForm;
        private Object address;
        private int kendraId;
        private Object spouseDocument;
        private String taluk;
        private String commAddrSameAsPresentAddr;
        private String recordType;
        private String branchName;
        private String dateOfBirth;
        private String customerlanguage;
        private String accountNumber;
        private String religion;
        private String currentAddress;
        private String valSource;
        private String districtId;
        private List<CommunicationAddress> communicationAddress;
        private String totalFamilyMembers;
        private String joiningCbStatus;
        private String middleName;
        private String agriLandWet;
        private String firstAcOfficer;
    }

    // ================= NESTED CLASSES =================
    public static class LegalDocument {
        private String date;
        private String legalExpDate;
        private String name;
        private String id;
    }

    public static class AddressLine {
        private String permanentAddressLine;
    }

    public static class CommunicationAddress {
        private String address;
    }

    public static class Nominee {
        private String amount;
        private String taluk;
        private String phone;
        private String district;
        private String postalCode;
        private String relationCode;
        private String name;
        private String state;
        private String upi;
    }

    public static class Kyc {
        private String name;
        private String id;
    }
}
