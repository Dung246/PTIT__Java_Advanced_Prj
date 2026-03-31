package entity;

import java.time.LocalDateTime;



public class Booking {

    private int id;
    private int userId;
    private int roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int participantCount;
    private int serviceId;
    private int equipmentId;
    private int supportStaffId;

    // 🔥 ADD
    private String status;
    private String supportStatus;
    private Integer supportId;

    // ===== GET SET =====

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSupportStatus() { return supportStatus; }
    public void setSupportStatus(String supportStatus) { this.supportStatus = supportStatus; }

    public Integer getSupportId() { return supportId; }
    public void setSupportId(Integer supportId) { this.supportId = supportId; }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public int getSupportStaffId() {
        return supportStaffId;
    }

    public void setSupportStaffId(int supportStaffId) {
        this.supportStaffId = supportStaffId;
    }
}
