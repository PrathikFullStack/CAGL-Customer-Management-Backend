package com.iexceed.appzillonbanking.cagl.entity;

import java.io.Serializable;
import java.util.Objects;

public class GkNomineeId implements Serializable {
	private static final long serialVersionUID = 1L;
	private String custid;
	private String applicationId;
	private String legaldocId;

	public GkNomineeId() {
	}
	public GkNomineeId(String custid, String applicationId, String legaldocId) {
		this.custid = custid;
		this.applicationId = applicationId;
		this.legaldocId = legaldocId;
	}
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof GkNomineeId))
			return false;
		GkNomineeId that = (GkNomineeId) o;
		return Objects.equals(custid, that.custid) && Objects.equals(applicationId, that.applicationId)
				&& Objects.equals(legaldocId, that.legaldocId);
	}
	@Override
	public int hashCode() {
		return Objects.hash(custid, applicationId, legaldocId);
	}
}
