package com.bansaiyai.bansaiyai.dto.dashboard;

public class MemberSearchResultDTO {
    private Long memberId;
    private String firstName;
    private String lastName;
    private String thumbnailUrl;
    private String status;

    public MemberSearchResultDTO() {
    }

    public MemberSearchResultDTO(Long memberId, String firstName, String lastName, String thumbnailUrl, String status) {
        this.memberId = memberId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
