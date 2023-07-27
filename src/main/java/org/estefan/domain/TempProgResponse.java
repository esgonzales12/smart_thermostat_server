package org.estefan.domain;

public class TempProgResponse {
    Integer recordId;

    public TempProgResponse() {}

    public TempProgResponse(Integer id) {
        this.recordId = id;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }
}
