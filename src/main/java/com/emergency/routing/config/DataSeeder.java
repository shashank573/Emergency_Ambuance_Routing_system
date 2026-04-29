package com.emergency.routing.config;

import com.emergency.routing.model.Ambulance;
import com.emergency.routing.model.AmbulanceStatus;
import com.emergency.routing.model.Hospital;
import com.emergency.routing.model.HospitalType;
import com.emergency.routing.model.Specialization;
import com.emergency.routing.repository.AmbulanceRepository;
import com.emergency.routing.repository.HospitalRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final HospitalRepository hospitalRepository;
    private final AmbulanceRepository ambulanceRepository;

    public DataSeeder(
        HospitalRepository hospitalRepository,
        AmbulanceRepository ambulanceRepository
    ) {
        this.hospitalRepository = hospitalRepository;
        this.ambulanceRepository = ambulanceRepository;
    }

    @Override
    public void run(String... args) {
        if (hospitalRepository.count() == 0) {
            hospitalRepository.saveAll(delhiHospitals());
        }
        if (ambulanceRepository.count() == 0) {
            ambulanceRepository.saveAll(
                List.of(
                    new Ambulance(
                        null,
                        "AMB001",
                        28.7041,
                        77.1025,
                        AmbulanceStatus.AVAILABLE
                    ),
                    new Ambulance(
                        null,
                        "AMB002",
                        28.6139,
                        77.2090,
                        AmbulanceStatus.AVAILABLE
                    ),
                    new Ambulance(
                        null,
                        "AMB003",
                        28.5355,
                        77.3910,
                        AmbulanceStatus.AVAILABLE
                    ),
                    new Ambulance(
                        null,
                        "AMB004",
                        28.5672,
                        77.2100,
                        AmbulanceStatus.AVAILABLE
                    )
                )
            );
        }
    }

    private List<Hospital> delhiHospitals() {
        return List.of(
            h(
                "AIIMS Delhi",
                28.5672,
                77.2100,
                12,
                Specialization.HEART,
                HospitalType.GOVERNMENT
            ),
            h(
                "Safdarjung Hospital",
                28.5693,
                77.2058,
                20,
                Specialization.TRAUMA,
                HospitalType.GOVERNMENT
            ),
            h(
                "Ram Manohar Lohia Hospital",
                28.6244,
                77.2006,
                9,
                Specialization.GENERAL,
                HospitalType.GOVERNMENT
            ),
            h(
                "Lady Hardinge Medical College",
                28.6350,
                77.2128,
                7,
                Specialization.PREGNANCY,
                HospitalType.GOVERNMENT
            ),
            h(
                "Lok Nayak Hospital",
                28.6432,
                77.2382,
                15,
                Specialization.TRAUMA,
                HospitalType.GOVERNMENT
            ),
            h(
                "GB Pant Hospital",
                28.6438,
                77.2390,
                10,
                Specialization.HEART,
                HospitalType.GOVERNMENT
            ),
            h(
                "Batra Hospital",
                28.5273,
                77.2507,
                8,
                Specialization.NEURO,
                HospitalType.PRIVATE
            ),
            h(
                "Fortis Escorts",
                28.5602,
                77.2833,
                6,
                Specialization.HEART,
                HospitalType.PRIVATE
            ),
            h(
                "Max Saket",
                28.5255,
                77.2139,
                11,
                Specialization.GENERAL,
                HospitalType.PRIVATE
            ),
            h(
                "Apollo Indraprastha",
                28.5406,
                77.2846,
                5,
                Specialization.NEURO,
                HospitalType.PRIVATE
            ),
            h(
                "BLK-Max",
                28.6445,
                77.1893,
                4,
                Specialization.HEART,
                HospitalType.PRIVATE
            ),
            h(
                "Sir Ganga Ram",
                28.6417,
                77.1890,
                14,
                Specialization.GENERAL,
                HospitalType.PRIVATE
            ),
            h(
                "Moolchand",
                28.5678,
                77.2397,
                3,
                Specialization.PREGNANCY,
                HospitalType.PRIVATE
            ),
            h(
                "Holy Family",
                28.5609,
                77.2742,
                0,
                Specialization.PREGNANCY,
                HospitalType.PRIVATE
            ),
            h(
                "Maharaja Agrasen",
                28.6500,
                77.1420,
                5,
                Specialization.TRAUMA,
                HospitalType.PRIVATE
            ),
            h(
                "Deep Chand Bandhu",
                28.7021,
                77.1557,
                6,
                Specialization.GENERAL,
                HospitalType.GOVERNMENT
            ),
            h(
                "Hindu Rao",
                28.6669,
                77.2135,
                2,
                Specialization.TRAUMA,
                HospitalType.GOVERNMENT
            ),
            h(
                "Rajiv Gandhi Cancer",
                28.7350,
                77.1190,
                13,
                Specialization.NEURO,
                HospitalType.GOVERNMENT
            ),
            h(
                "Dharamshila Narayana",
                28.6261,
                77.3119,
                9,
                Specialization.HEART,
                HospitalType.PRIVATE
            ),
            h(
                "GTB Hospital",
                28.6835,
                77.3159,
                16,
                Specialization.TRAUMA,
                HospitalType.GOVERNMENT
            )
        );
    }

    private long hospitalIdCounter = 1;

    private Hospital h(
        String name,
        double lat,
        double lon,
        int beds,
        Specialization spec,
        HospitalType type
    ) {
        return new Hospital(
            hospitalIdCounter++,
            name,
            lat,
            lon,
            beds,
            spec,
            type
        );
    }
}
