package net.noahf.firegen.backend.access.controllers;

import net.noahf.firegen.backend.database.structure.helper.CallerType;
import net.noahf.firegen.backend.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/")
public class ConstantsController {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("incidenttypes")
    public ResponseEntity<?> getIncidents() {
        return ApiResponse.success(List.of(
                "DOMESTIC DISTURBANCE",
                "FIGHT",
                "ROBBERY",
                "DISCHARGED FIREARM",
                "DISABLED VEHICLE",
                "BURGLARY",
                "LARCENY",
                "SHOPLIFTING",
                "LOOKOUT",
                "VANDALISM",
                "TRESPASSING",
                "TRAFFIC STOP",
                "HIGH-RISK TRAFFIC STOP",
                "TRAFFIC HAZARD",
                "TRAFFIC CONTROL",
                "RECKLESS DRIVING",
                "INTOXICATED PERSON",
                "NOISE COMPLAINT",
                "DISORDERLY CONDUCT",
                "LOITERING",
                "SUSPICIOUS PERSON",
                "SUSPICIOUS VEHICLE",
                "SUSPICIOUS ACTIVITY",
                "ARMED SUBJECT",
                "WELFARE CHECK",
                "JUVENILE",
                "CIVIL MATTER",
                "FOLLOW-UP",
                "ASSIST MOTORIST",
                "ANIMAL COMPLAINT",
                "911 HANGUP",
                "LOST PROPERTY",
                "PROPERTY DAMAGE",
                "ACTIVE SHOOTER",
                "ACTIVE GUNFIGHT",
                "OFFICER IN DISTRESS",
                "FIRST RESPONDER IN DISTRESS",
                "BARRICADED SUBJECT",
                "BOMB THREAT",
                "EVACUATION",
                "RESIDENTIAL BURGLAR ALARM",
                "COMMERCIAL BURGLAR ALARM",
                "HARASSMENT / THREATS",
                "TRANSPORT INMATE",
                "ESCORT FUNERAL",
                "ESCORT EMERGENCY VEHICLE",
                "ESCORT CIVILIAN VEHICLE",
                "IMPROPER PARKING",
                "COMMUNITY POLICING",
                "POLICE STANDBY",
                "LOST PET",
                "MISSING PERSON",
                "SECURITY CHECK",
                "DORM CHECK",
                "EXTRA PATROL",
                "LIVESTOCK",
                "ANIMAL AT LARGE",
                "WARRANT SERVICE",

                "EMS SERVICE CALL",
                "FIRE SERVICE CALL",
                "ILLEGAL BURN",
                "RESIDENTIAL STRUCTURE FIRE",
                "RESIDENTIAL FIRE ALARM",
                "RESIDENTIAL CARBON ALARM",
                "RESIDENTIAL GAS LEAK",
                "COMMERCIAL STRUCTURE FIRE",
                "COMMERCIAL FIRE ALARM",
                "COMMERCIAL CARBON ALARM",
                "COMMERCIAL GAS LEAK",
                "VEHICLE FIRE",
                "BRUSH FIRE",
                "DUMPSTER FIRE",
                "CHIMNEY FIRE",
                "ELECTRICAL FIRE",
                "ELECTRICAL HAZARD",
                "SMOKE INVESTIGATION",
                "HAZMAT",
                "LOCKOUT",
                "CHILD LOCKED IN VEHICLE",
                "ELEVATOR ENTRAPMENT",
                "CONTROLLED BURN",
                "FIRE STANDBY",

                "ABDOMINAL PAIN",
                "ALLERGIC REACTION",
                "ANIMAL ATTACK",
                "BACK PAIN",
                "BREATHING PROBLEMS",
                "BURN",
                "INHALATION HAZARD",
                "CARDIAC / RESPIRATORY ARREST",
                "CHEST PAIN",
                "CHOKING",
                "SEIZURE",
                "DIABETIC PROBLEMS",
                "DROWNING",
                "ELECTROCUTION",
                "EYE PROBLEMS",
                "FALLS",
                "HEADACHE",
                "HEART PROBLEMS",
                "HEAT EXPOSURE",
                "COLD EXPOSURE",
                "LACERATIONS",
                "ENTRAPMENT",
                "OVERDOSE / INGESTION",
                "CHILDBIRTH",
                "PSYCHIATRIC",
                "SICK PERSON",
                "GUNSHOT WOUND",
                "STABBING",
                "STROKE",
                "NAUSEA / VOMITING",
                "TRAUMATIC INJURY",
                "UNCONSCIOUS / FAINTING",
                "UNKNOWN MEDICAL PROBLEM",
                "AUTOMATIC CRASH NOTIFICATION",
                "INTER-FACILITY MEDICAL TRANSPORT",
                "LIFT ASSIST",
                "MEDICAL STANDBY",

                "TECHNICAL RESCUE",
                "SEARCH AND RESCUE",

                "MVC W/ PROPERTY DAMAGE",
                "MVC W/ INJURIES",
                "MVC W/ SERIOUS INJURIES",
                "MVC W/ ENTRAPMENT",
                "MVC W/ FIRE",
                "HIT & RUN W/ PROPERTY DAMAGE",
                "HIT & RUN W/ INJURIES",
                "HIT & RUN W/ SERIOUS INJURIES",
                "HIT & RUN W/ ENTRAPMENT",
                "HIT & RUN W/ FIRE",
                "ASSAULT",
                "ASSAULT W/ INJURIES",

                "ASSIST EMS AGENCY",
                "ASSIST FIRE AGENCY",
                "ASSIST POLICE AGENCY",

                "STANDBY"
        ));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("venues")
    public ResponseEntity<?> getVenues() {
        return ApiResponse.success(List.of(
                "TOWN OF CHRISTIANSBURG",
                "TOWN OF BLACKSBURG",
                "VIRGINIA TECH",
                "MONTGOMERY COUNTY",
                "ROANOKE COUNTY",
                "CITY OF ROANOKE",
                "CITY OF RADFORD",
                "CITY OF SALEM",
                "PULASKI COUNTY",
                "FLOYD COUNTY",
                "GILES COUNTY",
                "CRAIG COUNTY",
                "CARROLL COUNTY",
                "WYTHE COUNTY",
                "BLAND COUNTY",
                "PATRICK COUNTRY",
                "FRANKLIN COUNTY",
                "BEDFORD COUNTY",
                "BOTETOURT COUNTY",
                "STATE OF WEST VIRGINIA",
                "STATE OF VIRGINIA",
                "STATE OF NORTH CAROLINA",
                "STATE OF TENNESSEE",
                "STATE OF KENTUCKY"
        ));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("callertypes")
    public ResponseEntity<?> getCallerTypes() {
        return ApiResponse.success(Arrays.stream(CallerType.values()).map(ct -> ct.name().replace("_", " ")).toList());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("priorities")
    public ResponseEntity<?> getPrioritiesFor() {
        return ApiResponse.success(List.of("POLICE 1", "POLICE 2", "POLICE 3", "POLICE HBP", "OMEGA", "ALPHA", "BRAVO", "CHARLIE", "DELTA", "ECHO"));
    }

}
