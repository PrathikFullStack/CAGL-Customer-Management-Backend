package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_asmi_user")
@IdClass(TbAsmiUser.Id.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbAsmiUser {

    @jakarta.persistence.Id
    @Column(name = "user_id", length = 100, nullable = false)
    @JsonProperty("userId")
    private String userId;

    @jakarta.persistence.Id
    @Column(name = "app_id", length = 100, nullable = false)
    @JsonProperty("appId")
    private String appId;

    @Column(name = "pin", length = 300)
    @JsonProperty("pin")
    private String pin;

    @Column(name = "user_name", length = 100)
    @JsonProperty("userName")
    private String userName;

    @Column(name = "last_name", length = 100)
    @JsonProperty("lastName")
    private String lastName;

    @Column(name = "login_status", length = 3, nullable = false)
    @JsonProperty("loginStatus")
    private String loginStatus;

    @Column(name = "fail_count")
    @JsonProperty("failCount")
    private Integer failCount;

    @Column(name = "user_active", length = 3)
    @JsonProperty("userActive")
    private String userActive;

    @Column(name = "user_locked", length = 3)
    @JsonProperty("userLocked")
    private String userLocked;

    @Column(name = "language", length = 2)
    @JsonProperty("language")
    private String language;

    @Column(name = "externalidentifier", length = 30)
    @JsonProperty("externalIdentifier")
    private String externalIdentifier;

    @Column(name = "user_addr1", length = 50)
    @JsonProperty("userAddr1")
    private String userAddr1;

    @Column(name = "user_addr2", length = 50)
    @JsonProperty("userAddr2")
    private String userAddr2;

    @Column(name = "user_addr3", length = 50)
    @JsonProperty("userAddr3")
    private String userAddr3;

    @Column(name = "user_addr4", length = 50)
    @JsonProperty("userAddr4")
    private String userAddr4;

    @Column(name = "user_eml1", length = 255)
    @JsonProperty("userEml1")
    private String userEml1;

    @Column(name = "user_eml2", length = 255)
    @JsonProperty("userEml2")
    private String userEml2;

    @Column(name = "user_phno1", length = 20)
    @JsonProperty("userPhno1")
    private String userPhno1;

    @Column(name = "user_phno2", length = 20)
    @JsonProperty("userPhno2")
    private String userPhno2;

    @Column(name = "user_lvl")
    @JsonProperty("userLvl")
    private Integer userLvl;

    @Column(name = "user_lock_ts")
    @JsonProperty("userLockTs")
    private LocalDateTime userLockTs;

    @Column(name = "pin_change_ts")
    @JsonProperty("pinChangeTs")
    private LocalDateTime pinChangeTs;

    @Column(name = "profile_pic")
    @JsonProperty("profilePic")
    private String profilePic;

    @Column(name = "create_user_id", length = 100)
    @JsonProperty("createUserId")
    private String createUserId;

    @Column(name = "create_ts")
    @JsonProperty("createTs")
    private LocalDateTime createTs;

    @Column(name = "version_no")
    @JsonProperty("versionNo")
    private Integer versionNo;

    @Column(name = "maker_id", length = 45)
    @JsonProperty("makerId")
    private String makerId;

    @Column(name = "maker_ts")
    @JsonProperty("makerTs")
    private LocalDateTime makerTs;

    @Column(name = "checker_id", length = 45)
    @JsonProperty("checkerId")
    private String checkerId;

    @Column(name = "date_of_birth")
    @JsonProperty("dateOfBirth")
    private LocalDate dateOfBirth;

    @Column(name = "checker_ts")
    @JsonProperty("checkerTs")
    private LocalDateTime checkerTs;

    @Column(name = "auth_status", length = 45)
    @JsonProperty("authStatus")
    private String authStatus;

    @Column(name = "add_info1", length = 255)
    @JsonProperty("addInfo1")
    private String addInfo1;

    @Column(name = "add_info2", length = 255)
    @JsonProperty("addInfo2")
    private String addInfo2;

    @Column(name = "add_info3", length = 255)
    @JsonProperty("addInfo3")
    private String addInfo3;

    @Column(name = "add_info4", length = 255)
    @JsonProperty("addInfo4")
    private String addInfo4;

    @Column(name = "add_info5", length = 255)
    @JsonProperty("addInfo5")
    private String addInfo5;

    @Column(name = "user_type", length = 255)
    @JsonProperty("userType")
    private String userType;


    /**
     * Composite primary key:
     * user_id + app_id
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {

        @Column(name = "user_id", length = 100)
        private String userId;

        @Column(name = "app_id", length = 100)
        private String appId;
    }
}