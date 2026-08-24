package com.iexceed.appzillonbanking.cagl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInsuranceNotification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    
    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "kendra_name")
    private String kendraName;

    @Column(name = "kendra_id")
    private String kendraId;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "branch_id")
    private String branchId;

    @Column(name = "category_of_notification")
    private String categoryOfNotification;

    @Column(name = "message")
    private String message;

}