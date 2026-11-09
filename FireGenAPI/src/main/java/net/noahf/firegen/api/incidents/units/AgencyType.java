package net.noahf.firegen.api.incidents.units;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

/**
 * Represents the type of {@link Agency}
 */
@Getter @AllArgsConstructor
public enum AgencyType implements StringSelectors {

    FIRE("Fire Department", "Provides fire suppression services, rescue operations, hazardous material response, fire prevention, public education, and emergency services for an area."),

    EMS("Emergency Medical Services", "Provides emergency medical care, patient assessments, prehospital medical treatment, and community healthcare services for an area."),

    FIRE_EMS("Fire and EMS Department", "Provides fire suppression services, rescue operations, hazardous material response, fire prevention, public education, emergency services, emergency medical care, patient assessments, prehospital medical treatment, and sometimes community healthcare services for an area. Sometimes, firefighters and EMS personnel are cross-trained in these systems."),

    EMS_BY_AIR("Emergency Medical Services by Air", "Provides critical emergency medical care via transportation by helicopter, usually used in longer ground transportations to higher levels of care"),

    POLICE("Police Department", "Provides law enforcement, crime prevention, criminal investigations, and traffic enforcement for an area."),

    SHERIFF("Sheriff's Office", "Provides law enforcement, crime prevention, criminal investigations, traffic enforcement, civil process service, and court security for an area."),

    SECURITY("Security Company", "Provides protection detail and crime prevention for specific, usually private, establishments"),

    STATE_POLICE("State Police Department", "Provides statewide law enforcement, criminal investigations, highway/traffic enforcement, specialized tactical response, and assistance to local law enforcement agencies in an area."),

    MENTAL_HEALTH_ASSISTANCE("Mental Health Assistance", "Provides mental health services, sometimes in tandem with a police, fire, or EMS emergency response."),

    TRANSIT("Transit Agency / Authority", "Provides public transportation services, fixed-route bus or rail operations, and paratransit for an area."),

    MEDICAL_EXAMINER("Medical Examiner's Office", "Provides death investigations, forensic examinations, autopsies, and determinations of cause and manner of death for an area."),

    PUBLIC_WORKS("Public Works", "Provides road maintenance, infrastructure maintenance, sanitation support, and public facilities maintenance for an area."),

    UTILITY_COMPANY("Utility Company", "Provides electric, natural gas, water, wastewater, telecommunications, internet, or other essential services to customers in an area"),

    NATIONAL_GUARD("National Guard", "Provides military support, disaster response, emergency assistance, and enforces homeland security."),

    FEMA("Federal Emergency Management Agency", "Provides disaster preparedness, emergency response coordination, hazard mitigation, and support to state, local, tribal, and territorial governments."),

    ANIMAL_CONTROL("Animal Control", "Provides animal welfare, stray animal management, animal law enforcement, bite investigations, and public safety services for an area."),

    FIRE_MARSHALL("Fire Marshall's Office", "Provides fire investigations, fire code enforcement, building inspections, fire prevention, public education, and arson investigations for an area."),

    FUNERAL_SERVICE("Funeral Service / Home", "Provides deceased transportation, funeral planning, embalming, cremation, and burial coordination for an area."),

    TOWING("Towing Company", "Provides vehicle recovery, roadside assistance, accident scene clearance, impound service, and often emergency scene response for an area."),

    DISPATCH("Dispatch / 911 Authority", "Provides emergency communications, call-taking, resource coordination, incident tracking, and public access points for an area."),

    /**
     * Represents any service that does not fall into the existing {@link AgencyType agency types}.
     */
    OTHER("Other", "No description provided.");

    private final String fullName;
    private final String description;

    @Override
    public List<String> asStringSelectors() {
        return List.of(name());
    }

}
