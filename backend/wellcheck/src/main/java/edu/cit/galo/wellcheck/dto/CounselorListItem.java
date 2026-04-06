package edu.cit.galo.wellcheck.dto;

public class CounselorListItem {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String employeeNumber;
    private String specialization;
    private String status;
    private String bio;
    private int availableSlots;

    // Constructor with bio and availableSlots (for CounselorService)
    public CounselorListItem(Long id, String firstName, String lastName,
                             String email, String employeeNumber,
                             String specialization, String status,
                             String bio, int availableSlots) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.employeeNumber = employeeNumber;
        this.specialization = specialization;
        this.status = status;
        this.bio = bio;
        this.availableSlots = availableSlots;
    }

    // Constructor without bio and availableSlots (for AdminService)
    public CounselorListItem(Long id, String firstName, String lastName,
                             String email, String employeeNumber,
                             String specialization, String status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.employeeNumber = employeeNumber;
        this.specialization = specialization;
        this.status = status;
        this.bio = null;
        this.availableSlots = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }
}