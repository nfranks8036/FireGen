package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

/**
 * Represents the type of {@link Agency}
 */
public enum AgencyType implements StringSelectors {

    /**
     * Fire Department, provides fire suppression services, rescue operations, hazardous material response, fire
     * prevention, public education, and emergency services for an area.
     */
    FIRE,

    /**
     * EMS Department, provides emergency medical care, patient assessments, prehospital medical treatment, and
     * community healthcare services for an area.
     */
    EMS,

    /**
     * Fire & EMS Department, provides fire suppression services, rescue operations, hazardous material response, fire
     * prevention, public education, emergency services, emergency medical care, patient assessments, prehospital
     * medical treatment, and sometimes community healthcare services for an area. Sometimes, firefighters and EMS
     * personnel are cross-trained in these systems.
     */
    FIRE_EMS,

    /**
     * EMS by Helicopter/Air Agency, provides critical emergency medical care via transportation by helicopter, usually
     * used in longer ground transportations to higher levels of care
     */
    EMS_BY_AIR,

    /**
     * Police Department, provides law enforcement, crime prevention, criminal investigations, and traffic enforcement
     * for an area.
     */
    POLICE,

    /**
     * Sheriff's Office, provides law enforcement, crime prevention, criminal investigations, traffic enforcement, civil
     * process service, and court security for an area.
     */
    SHERIFF,

    /**
     * Security Agency, provides protection detail and crime prevention for specific, usually private, establishments
     */
    SECURITY,

    /**
     * State Police, provides statewide law enforcement, criminal investigations, highway/traffic enforcement,
     * specialized tactical response, and assistance to local law enforcement agencies in an area.
     */
    STATE_POLICE,

    /**
     * Mental Health Assistance Program (or Co-responders or Behavioral Health), provides mental health services,
     * sometimes in tandem with a police, fire, or EMS emergency response.
     */
    MENTAL_HEALTH_ASSISTANCE,

    /**
     * Transit Agency/Authority, provides public transportation services, fixed-route bus or rail operations, and
     * paratransit for an area.
     */
    TRANSIT,

    /**
     * Medical Examiner's Office, provides death investigations, forensic examinations, autopsies, and determinations
     * of cause and manner of death for an area.
     */
    MEDICAL_EXAMINER,

    /**
     * Public Works, provides road maintenance, infrastructure maintenance, sanitation support, and public facilities
     * maintenance for an area.
     */
    PUBLIC_WORKS,

    /**
     * Utility Company, provides electric, natural gas, water, wastewater, telecommunications, internet, or other
     * essential services to customers in an area
     */
    UTILITY_COMPANY,

    /**
     * National Guard, provides military support, disaster response, emergency assistance, and enforces homeland
     * security.
     */
    NATIONAL_GUARD,

    /**
     * FEMA, provides disaster preparedness, emergency response coordination, hazard mitigation, and support to state,
     * local, tribal, and territorial governments.
     */
    FEDERAL_EMERGENCY_MANAGEMENT_AGENCY,

    /**
     * Animal Control, provides animal welfare, stray animal management, animal law enforcement, bite investigations,
     * and public safety services for an area.
     */
    ANIMAL_CONTROL,

    /**
     * Fire Marshall's Office, provides fire investigations, fire code enforcement, building inspections, fire
     * prevention, public education, and arson investigations for an area.
     */
    FIRE_MARSHALL,

    /**
     * Funeral Service/Home, provides deceased transportation, funeral planning, embalming, cremation, and
     * burial coordination for an area.
     */
    FUNERAL_SERVICE,

    /**
     * Towing Service, provides vehicle recovery, roadside assistance, accident scene clearance, impound service, and
     * often emergency scene response for an area.
     */
    TOWING,

    /**
     * Dispatch (911 Authority), provides emergency communications, call-taking, resource coordination, incident
     * tracking, and public access points for an area.
     */
    DISPATCH,

    /**
     * Represents any service that does not fall into the existing {@link AgencyType agency types}.
     */
    OTHER;


    @Override
    public List<String> asStringSelectors() {
        return List.of(name());
    }

}
