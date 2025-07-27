package net.noahf.firewatch.common.incidents.medical;

public class MedicalPriorityDispatchSystem {

    private final MedicalProtocol protocol;
    private final MedicalPriority priority;

    public MedicalPriorityDispatchSystem(MedicalProtocol protocol, MedicalPriority priority) {
        this.protocol = protocol;
        this.priority = priority;
    }

    public MedicalProtocol medicalProtocol() { return this.protocol; }
    public MedicalPriority medicalPriority() { return this.priority; }

}
