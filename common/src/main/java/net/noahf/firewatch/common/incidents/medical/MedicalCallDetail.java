package net.noahf.firewatch.common.incidents.medical;

public class MedicalCallDetail {

    private MedicalProtocol protocol;
    private MedicalPriority priority;

    public MedicalCallDetail() {
        this.protocol = null;
        this.priority = null;
    }

    public MedicalProtocol medicalProtocol() { return this.protocol; }
    public void medicalProtocol(MedicalProtocol newProtocol) { this.protocol = newProtocol; }

    public MedicalPriority medicalPriority() { return this.priority; }
    public void medicalPriority(MedicalPriority newPriority) { this.priority = newPriority; }

    @Override
    public String toString() {
        return "MedicalCallDetail{" +
                "protocol=" + protocol +
                ", priority=" + priority +
                '}';
    }
}
