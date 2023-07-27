package org.estefan.dao;

import org.estefan.dao.annotations.Column;
import org.estefan.dao.annotations.PrimaryKey;
import org.estefan.dao.annotations.Table;

@Table(name = "tempPrograms")
public class TempProgrammingRecord {
    @Column(name = "id")
    @PrimaryKey(columnIdentifier = "id")
    private Integer id;
    @Column(name = "deviceId")
    private Integer deviceId;
    @Column(name = "datetime")
    private String datetime;
    @Column(name = "temp")
    private String temp;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    @Override
    public String toString() {
        return "TempProgrammingRecord{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                ", datetime='" + datetime + '\'' +
                ", temp='" + temp + '\'' +
                '}';
    }
}
