package model;

public class Passenger {

    // Private variables (Encapsulation)
    private int passengerId;
    private String name;
    private int age;
    private String gender;
    private String phoneNumber;

    // Disability details & Concession verification
    private boolean isDisability;
    private String disabilityType;
    private String disabilityId;

    // Default / Backward-compatible Constructor
    public Passenger(int passengerId, String name, int age, String gender, String phoneNumber) {
        this(passengerId, name, age, gender, phoneNumber, false, null, null);
    }

    // Extended Constructor with Disability Details
    public Passenger(int passengerId, String name, int age, String gender, String phoneNumber,
                     boolean isDisability, String disabilityType, String disabilityId) {
        this.passengerId = passengerId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.isDisability = isDisability;
        this.disabilityType = disabilityType;
        this.disabilityId = disabilityId;
    }

    // Getter methods
    public int getPassengerId() {
        return passengerId;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isDisability() {
        return isDisability;
    }

    public String getDisabilityType() {
        return disabilityType;
    }

    public String getDisabilityId() {
        return disabilityId;
    }

    // Setter methods
    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setDisability(boolean disability) {
        isDisability = disability;
    }

    public void setDisabilityType(String disabilityType) {
        this.disabilityType = disabilityType;
    }

    public void setDisabilityId(String disabilityId) {
        this.disabilityId = disabilityId;
    }

    // Display passenger details
    @Override
    public String toString() {
        return "ID: " + passengerId +
                "\nName: " + name +
                "\nAge: " + age +
                "\nGender: " + gender +
                "\nPhone: " + phoneNumber +
                (isDisability ? ("\nDisability: " + disabilityType + " (Card: " + disabilityId + ")") : "");
    }
}