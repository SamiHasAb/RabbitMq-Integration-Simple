package org.example.app.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Employee {


  // ===== Primitive Types =====
  private byte idByte;
  private short idShort;
  private int id;
  private long recordNumber;
  private float rating;
  private double salaryGrowthRate;
  private boolean active;
  private char grade;

  // ===== Wrapper Types =====
  private Byte wByte;
  private Short wShort;
  private Integer wInteger;
  private Long wLong;
  private Float wFloat;
  private Double wDouble;
  private Boolean wBoolean;
  private Character wCharacter;

  // ===== Common Object Types =====
  private String name;
  private BigDecimal salary;
  private BigInteger uniqueCode;

  // ===== Date & Time API =====
  private LocalDate joiningDate;
  private LocalDateTime lastModified;

  // ===== Advanced / Collections =====
  private List<String> skills;
  private Set<String> certifications;
  private Map<String, Object> additionalDetails;

  // ===== Enum =====
  private Department department;

  // ===== Nested Subclass =====
  private Address address;
  private JobDetails jobDetails;

  // ====================== ENUM ======================
  public enum Department {
    HR, IT, SALES, MARKETING, FINANCE, OPERATIONS
  }

  // ====================== SUBCLASS: Address ======================
  public static class Address {
    private String street;
    private String city;
    private String state;
    private int zipCode;
    private Country country;

    // Nested Enum inside a subclass
    public enum Country {
      USA, CANADA, INDIA, UK, AUSTRALIA
    }

    // Getters & Setters
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getZipCode() { return zipCode; }
    public void setZipCode(int zipCode) { this.zipCode = zipCode; }

    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
  }

  // ====================== SUBCLASS: JobDetails ======================
  public static class JobDetails {
    private String role;
    private int experienceYears;
    private EmploymentType employmentType;

    private Manager manager;

    // Nested enum
    public enum EmploymentType {
      FULL_TIME, PART_TIME, CONTRACT, INTERN
    }

    // Nested subclass inside subclass
    public static class Manager {
      private String managerName;
      private String email;
      private long managerId;

      public String getManagerName() { return managerName; }
      public void setManagerName(String managerName) { this.managerName = managerName; }

      public String getEmail() { return email; }
      public void setEmail(String email) { this.email = email; }

      public long getManagerId() { return managerId; }
      public void setManagerId(long managerId) { this.managerId = managerId; }
    }

    // Getters & Setters
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }

    public Manager getManager() { return manager; }
    public void setManager(Manager manager) { this.manager = manager; }
  }

  // ====================== Getters & Setters (Main Class) ======================

  public byte getIdByte() { return idByte; }
  public void setIdByte(byte idByte) { this.idByte = idByte; }

  public short getIdShort() { return idShort; }
  public void setIdShort(short idShort) { this.idShort = idShort; }

  public int getId() { return id; }
  public void setId(int id) { this.id = id; }

  public long getRecordNumber() { return recordNumber; }
  public void setRecordNumber(long recordNumber) { this.recordNumber = recordNumber; }

  public float getRating() { return rating; }
  public void setRating(float rating) { this.rating = rating; }

  public double getSalaryGrowthRate() { return salaryGrowthRate; }
  public void setSalaryGrowthRate(double salaryGrowthRate) { this.salaryGrowthRate = salaryGrowthRate; }

  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }

  public char getGrade() { return grade; }
  public void setGrade(char grade) { this.grade = grade; }

  public Byte getwByte() { return wByte; }
  public void setwByte(Byte wByte) { this.wByte = wByte; }

  public Short getwShort() { return wShort; }
  public void setwShort(Short wShort) { this.wShort = wShort; }

  public Integer getwInteger() { return wInteger; }
  public void setwInteger(Integer wInteger) { this.wInteger = wInteger; }

  public Long getwLong() { return wLong; }
  public void setwLong(Long wLong) { this.wLong = wLong; }

  public Float getwFloat() { return wFloat; }
  public void setwFloat(Float wFloat) { this.wFloat = wFloat; }

  public Double getwDouble() { return wDouble; }
  public void setwDouble(Double wDouble) { this.wDouble = wDouble; }

  public Boolean getwBoolean() { return wBoolean; }
  public void setwBoolean(Boolean wBoolean) { this.wBoolean = wBoolean; }

  public Character getwCharacter() { return wCharacter; }
  public void setwCharacter(Character wCharacter) { this.wCharacter = wCharacter; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public BigDecimal getSalary() { return salary; }
  public void setSalary(BigDecimal salary) { this.salary = salary; }

  public BigInteger getUniqueCode() { return uniqueCode; }
  public void setUniqueCode(BigInteger uniqueCode) { this.uniqueCode = uniqueCode; }

  public LocalDate getJoiningDate() { return joiningDate; }
  public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

  public LocalDateTime getLastModified() { return lastModified; }
  public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

  public List<String> getSkills() { return skills; }
  public void setSkills(List<String> skills) { this.skills = skills; }

  public Set<String> getCertifications() { return certifications; }
  public void setCertifications(Set<String> certifications) { this.certifications = certifications; }

  public Map<String, Object> getAdditionalDetails() { return additionalDetails; }
  public void setAdditionalDetails(Map<String, Object> additionalDetails) { this.additionalDetails = additionalDetails; }

  public Department getDepartment() { return department; }
  public void setDepartment(Department department) { this.department = department; }

  public Address getAddress() { return address; }
  public void setAddress(Address address) { this.address = address; }

  public JobDetails getJobDetails() { return jobDetails; }
  public void setJobDetails(JobDetails jobDetails) { this.jobDetails = jobDetails; }
}
