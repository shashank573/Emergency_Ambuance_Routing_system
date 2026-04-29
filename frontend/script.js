const BACKEND_URL = "http://localhost:8080";

const map = L.map("map").setView([28.6, 77.2], 11);
L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "&copy; OpenStreetMap contributors",
}).addTo(map);

let selectedLocation = null;
let selectedAddress = "";
let isAddressVerified = false;
let selectedMarker = null;
let routeLine1 = null;
let routeLine2 = null;
let ambulanceMarkers = new Map();
let hospitalMarkers = new Map();
let currentCase = null;
let isEmergencyActive = false;
let isAddingHospital = false;
let tempHospitalLocation = null;
let draftHospMarker = null;
let isAddingAmbulance = false;
let tempAmbulanceLocation = null;
let tempAmbulanceMarker = null;

let movingVehicles = new Set();

function setSystemLock(locked) {
    const buttons = [
        "confirmAddressBtn",
        "requestBtn",
        "handoverBtn",
        "updateBedsBtn",
        "updateAmbulanceBtn",
    ];
    buttons.forEach((id) => {
        const el = document.getElementById(id);
        if (el) el.disabled = locked;
    });
    const inputs = [
        "patientName",
        "admittedBy",
        "age",
        "hospitalIdInput",
        "bedsInput",
        "ambulanceIdInput",
        "ambulanceStatusInput",
    ];
    inputs.forEach((id) => {
        const el = document.getElementById(id);
        if (el) el.disabled = locked;
    });
}

map.on("click", (e) => {
    if (isEmergencyActive) {
        notify(
            "System is locked during an active emergency! Complete Handover first.",
        );
        return;
    }

    if (isAddingHospital) {
        tempHospitalLocation = e.latlng;
        if (draftHospMarker) map.removeLayer(draftHospMarker);
        draftHospMarker = L.marker(
            [tempHospitalLocation.lat, tempHospitalLocation.lng],
            {
                title: "New Hospital",
                icon: createEmojiMarker("📍"),
            },
        )
            .addTo(map)
            .bindPopup("Selected Hospital Location")
            .openPopup();

        document.getElementById("addHospitalSubmitBtn").disabled = false;
        document.getElementById("addHospitalResult").textContent =
            "Location selected. You can now submit.";
        return;
    }

    if (isAddingAmbulance) {
        tempAmbulanceLocation = e.latlng;
        if (tempAmbulanceMarker) map.removeLayer(tempAmbulanceMarker);
        tempAmbulanceMarker = L.marker(
            [tempAmbulanceLocation.lat, tempAmbulanceLocation.lng],
            {
                title: "New Ambulance",
                icon: createEmojiMarker("🚑"),
            },
        )
            .addTo(map)
            .bindPopup("Selected Ambulance Location")
            .openPopup();

        document.getElementById("addAmbulanceSubmitBtn").disabled = false;
        document.getElementById("addAmbulanceResult").textContent =
            "Location selected. You can now submit.";
        return;
    }

    selectedLocation = e.latlng;
    isAddressVerified = false;
    document.getElementById("requestBtn").disabled = true;
    document.getElementById("selectedLocation").textContent =
        `${selectedLocation.lat.toFixed(5)}, ${selectedLocation.lng.toFixed(5)}`;
    document.getElementById("selectedAddress").textContent =
        "Fetching address...";
    if (selectedMarker) map.removeLayer(selectedMarker);
    selectedMarker = L.marker([selectedLocation.lat, selectedLocation.lng], {
        title: "Patient",
        icon: createEmojiMarker("🧍"),
    })
        .addTo(map)
        .bindPopup("Patient Location")
        .openPopup();
    reverseGeocode(selectedLocation.lat, selectedLocation.lng);
});

async function makeNetworkCall(url, options = {}) {
    const res = await fetch(url, options);
    if (!res.ok) {
        const err = await res.json().catch(() => ({ error: "Request failed" }));
        throw new Error(err.error || "Request failed");
    }
    return res.json();
}

function notify(msg) {
    if ("Notification" in window && Notification.permission === "granted") {
        new Notification(msg);
    }
}

if ("Notification" in window && Notification.permission !== "granted") {
    Notification.requestPermission();
}

function createEmojiMarker(emoji) {
    return L.divIcon({
        className: "emoji-icon",
        html: emoji,
        iconSize: [24, 24],
        iconAnchor: [12, 12],
    });
}

function emitSuccessTone() {
    try {
        const actx = new (window.AudioContext || window.webkitAudioContext)();
        const osc = actx.createOscillator();
        const gain = actx.createGain();
        osc.connect(gain);
        gain.connect(actx.destination);
        osc.type = "sine";
        osc.frequency.setValueAtTime(1046.5, actx.currentTime);
        osc.frequency.setValueAtTime(1318.51, actx.currentTime + 0.15);
        gain.gain.setValueAtTime(0.1, actx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.001, actx.currentTime + 0.4);
        osc.start(actx.currentTime);
        osc.stop(actx.currentTime + 0.4);
    } catch (e) {
        console.log(e);
    }
}

function emitWarningBeep() {
    try {
        const actx = new (window.AudioContext || window.webkitAudioContext)();
        const osc = actx.createOscillator();
        const gain = actx.createGain();
        osc.connect(gain);
        gain.connect(actx.destination);
        osc.type = "square";
        osc.frequency.setValueAtTime(600, actx.currentTime);
        gain.gain.setValueAtTime(0.05, actx.currentTime);
        gain.gain.setValueAtTime(0, actx.currentTime + 0.15);
        gain.gain.setValueAtTime(0.05, actx.currentTime + 0.3);
        gain.gain.setValueAtTime(0, actx.currentTime + 0.45);
        osc.start(actx.currentTime);
        osc.stop(actx.currentTime + 0.5);
    } catch (e) {
        console.log(e);
    }
}

function sketchPath(coordinates, color) {
    return L.polyline(
        coordinates.map((c) => [c[1], c[0]]),
        {
            color,
            weight: 5,
            opacity: 0.8,
        },
    ).addTo(map);
}

async function reverseGeocode(lat, lon) {
    try {
        const data = await makeNetworkCall(
            `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`,
        );
        selectedAddress = data.display_name || "Address unavailable";
        document.getElementById("selectedAddress").textContent =
            selectedAddress;
    } catch (e) {
        selectedAddress = "Address unavailable";
        document.getElementById("selectedAddress").textContent =
            selectedAddress;
    }
}

async function fetchRoadRoute(start, end) {
    const url = `https://router.project-osrm.org/route/v1/driving/${start.lng},${start.lat};${end.lng},${end.lat}?alternatives=3&overview=full&geometries=geojson&steps=true`;
    const data = await makeNetworkCall(url);
    if (!data.routes || data.routes.length === 0) {
        throw new Error("No road route found");
    }

    let attempts = 0;
    while (data.routes.length < 3 && attempts < 5) {
        let faked = JSON.parse(
            JSON.stringify(data.routes[data.routes.length - 1]),
        );
        faked.distance += 400 + Math.random() * 800;
        faked.duration += 120 + Math.random() * 180;
        data.routes.push(faked);
        attempts++;
    }

    return data.routes;
}

function pickRoute(routes, preference) {
    const byDuration = [...routes].sort((a, b) => a.duration - b.duration);
    const byDistance = [...routes].sort((a, b) => a.distance - b.distance);
    if (preference === "FASTEST") return byDuration[0];
    if (preference === "SHORTEST") return byDistance[0];
    return routes.length > 1 ? routes[1] : routes[0];
}

function updateAssignedHospitalId(hospitalId) {
    document.getElementById("handoverHospitalId").value = hospitalId;
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function executeMovementPhase(route, phaseName, durationSec, marker) {
    document.getElementById("simStatusText").textContent = phaseName;
    document.getElementById("simProgressBar").max = durationSec;
    document.getElementById("simProgressBar").value = 0;

    const totalPoints = route.geometry.coordinates.length;
    const intervalMs = 25;
    const totalSteps = (durationSec * 1000) / intervalMs;

    let currentStep = 0;
    return new Promise((resolve) => {
        const timer = setInterval(() => {
            currentStep++;
            const elapsedSec = (currentStep * intervalMs) / 1000;
            document.getElementById("simProgressBar").value = elapsedSec;
            document.getElementById("simTimeElapsed").textContent =
                Math.floor(elapsedSec);
            document.getElementById("simCountdown").textContent = Math.max(
                0,
                Math.ceil(durationSec - elapsedSec),
            );

            const progressRatio = currentStep / totalSteps;
            const exactIndexFloat = Math.min(
                progressRatio * (totalPoints - 1),
                totalPoints - 1,
            );
            const idx1 = Math.floor(exactIndexFloat);
            const idx2 = Math.min(idx1 + 1, totalPoints - 1);
            const segmentProgress = exactIndexFloat - idx1;

            const p1 = route.geometry.coordinates[idx1];
            const p2 = route.geometry.coordinates[idx2];
            const lat = p1[1] + (p2[1] - p1[1]) * segmentProgress;
            const lng = p1[0] + (p2[0] - p1[0]) * segmentProgress;

            if (marker) {
                marker.setLatLng([lat, lng]);
            }

            if (currentStep >= totalSteps) {
                clearInterval(timer);
                document.getElementById("simProgressBar").value = durationSec;
                document.getElementById("simCountdown").textContent = 0;
                resolve();
            }
        }, intervalMs);
    });
}

function resetSimulationUI() {
    document.getElementById("simulationPanel").style.display = "block";
    document.getElementById("simStatusText").textContent = "Initializing...";
    document.getElementById("simProgressBar").value = 0;
    document.getElementById("simTimeElapsed").textContent = "0";
    document.getElementById("simCountdown").textContent = "-";
}

function promptRouteSelection(routesList) {
    return new Promise((resolve) => {
        const modal = document.getElementById("routeSelectionModal");
        const container = document.getElementById("routeOptionsContainer");

        if (window.routeLinesPhase2) {
            window.routeLinesPhase2.forEach((item) =>
                map.removeLayer(item.line),
            );
        }
        window.routeLinesPhase2 = [];
        container.innerHTML = "";

        const routeSpecs = [
            { color: "#ef4444", class: "route-red", label: "Fastest Route" },
            { color: "#3b82f6", class: "route-blue", label: "Shortest Route" },
            {
                color: "#10b981",
                class: "route-green",
                label: "Alternative Route",
            },
        ];

        const sortedRoutes = [...routesList].sort(
            (a, b) => a.duration - b.duration,
        );
        const routesToShow = sortedRoutes.slice(0, 3);

        routesToShow.forEach((route, idx) => {
            const spec = routeSpecs[idx] || routeSpecs[2];

            const line = sketchPath(route.geometry.coordinates, spec.color);
            window.routeLinesPhase2.push({ line, route });

            const distKm = (route.distance / 1000).toFixed(2);
            const timeMins = Math.ceil(route.duration / 60);

            let trafficRange = [20, 40];
            if (idx === 0) trafficRange = [10, 20];
            else if (idx === 1) trafficRange = [40, 60];
            
            const trafficPercent = Math.floor(Math.random() * (trafficRange[1] - trafficRange[0] + 1)) + trafficRange[0];

            const btn = document.createElement("button");
            btn.className = `route-btn ${spec.class}`;
            btn.innerHTML = `<strong>${spec.label}</strong><small>Distance: ${distKm} km | Time: ${timeMins} mins | Traffic: ${trafficPercent}%</small>`;

            btn.onclick = () => {
                modal.style.display = "none";
                window.routeLinesPhase2.forEach((item) => {
                    if (item.route !== route) map.removeLayer(item.line);
                });
                resolve(route);
            };

            container.appendChild(btn);
        });

        modal.style.display = "flex";
    });
}

async function beginAmbulanceRun(ambulanceId, routeA, routesBList) {
    const marker = ambulanceMarkers.get(ambulanceId);
    if (!marker) return;

    movingVehicles.add(ambulanceId);
    const movingIcon = L.divIcon({
        className: "emoji-icon ambulance-busy",
        html: "🚑",
        iconSize: [24, 24],
        iconAnchor: [14, 14],
    });
    marker.setIcon(movingIcon);
    marker.setZIndexOffset(1000);

    resetSimulationUI();

    const boundsA = L.latLngBounds(routeA.geometry.coordinates.map(c => [c[1], c[0]]));
    map.fitBounds(boundsA, { padding: [50, 50], animate: true, duration: 1 });

    const phase1Duration = 18 + Math.floor(Math.random() * 3);

    await executeMovementPhase(
        routeA,
        "Ambulance is on the way to the patient",
        phase1Duration,
        marker,
    );
    emitWarningBeep();
    document.getElementById("simStatusText").textContent =
        "Ambulance has reached the patient";
    notify("Ambulance reached the patient");

    const selectedRouteB = await promptRouteSelection(routesBList);

    resetSimulationUI();

    const boundsB = L.latLngBounds(selectedRouteB.geometry.coordinates.map(c => [c[1], c[0]]));
    map.fitBounds(boundsB, { padding: [50, 50], animate: true, duration: 1 });

    const sortedRoutesB = [...routesBList].sort((a, b) => a.duration - b.duration);
    const selectedIndex = sortedRoutesB.indexOf(selectedRouteB);
    let phase2Duration;
    if (selectedIndex === 0) {
        phase2Duration = 10 + Math.floor(Math.random() * 3);
    } else if (selectedIndex === 1) {
        phase2Duration = 15 + Math.floor(Math.random() * 4);
    } else {
        phase2Duration = 20 + Math.floor(Math.random() * 3);
    }

    await executeMovementPhase(
        selectedRouteB,
        "Patient is being transported to hospital",
        phase2Duration,
        marker,
    );
    emitSuccessTone();
    const totalSimMins = Math.floor((phase1Duration + phase2Duration) / 60);
    const totalSimSecs = (phase1Duration + phase2Duration) % 60;
    const cost = Math.floor(Math.random() * 500) + 500;
    window.lastRideTotalTime = `${totalSimMins}m ${totalSimSecs}s`;
    window.lastRideCost = cost;

    window.lastRideSource = selectedAddress;
    window.lastRideDestination = currentCase.hospital.name;

    document.getElementById("simStatusText").textContent = "Patient reached the hospital. Please complete Handover.";
    notify("Patient reached the hospital");

    if (selectedMarker) {
        map.removeLayer(selectedMarker);
        selectedMarker = null;
    }

    movingVehicles.delete(ambulanceId);
    loadAmbulances();

    setTimeout(() => {
        document.getElementById("simulationPanel").style.display = "none";
        document.getElementById("handoverModal").style.display = "flex";
    }, 1500);
}

let firstMapLoad = true;

async function loadHospitals() {
    const hospitals = await makeNetworkCall(`${BACKEND_URL}/hospitals`);
    const bounds = [];
    hospitals.forEach((h) => {
        const bedText = h.availableBeds > 0 ? "Beds Available" : "Full";
        const icon = h.type === "GOVERNMENT" ? "🏥" : "🏨";
        const popup = `#${h.id} ${h.name}<br/>Type: ${h.type}<br/>Beds: ${h.availableBeds} (${bedText})<br/>Spec: ${h.specialization}`;

        const className =
            h.type === "GOVERNMENT"
                ? "emoji-icon hospital-govt"
                : "emoji-icon hospital-private";
        const customIcon = L.divIcon({
            className,
            html: icon,
            iconSize: [28, 28],
            iconAnchor: [14, 14],
        });

        if (hospitalMarkers.has(h.id)) {
            hospitalMarkers.get(h.id).setPopupContent(popup);
            hospitalMarkers.get(h.id).setIcon(customIcon);
        } else {
            const marker = L.marker([h.latitude, h.longitude], {
                icon: customIcon,
                title: h.name,
            })
                .addTo(map)
                .bindPopup(popup);
            hospitalMarkers.set(h.id, marker);
        }
        bounds.push([h.latitude, h.longitude]);
    });

    if (firstMapLoad && bounds.length > 0) {
        map.fitBounds(bounds, { padding: [30, 30] });
        firstMapLoad = false;
    }
}

async function loadAmbulances() {
    const ambulances = await makeNetworkCall(`${BACKEND_URL}/ambulances`);
    ambulances.forEach((a) => {
        const popup = `${a.code} (${a.status})`;
        const className =
            a.status === "AVAILABLE"
                ? "emoji-icon ambulance-available"
                : "emoji-icon ambulance-busy";
        const customIcon = L.divIcon({
            className,
            html: "🚑",
            iconSize: [24, 24],
            iconAnchor: [14, 14],
        });

        if (ambulanceMarkers.has(a.id)) {
            if (movingVehicles.has(a.id)) return;
            const marker = ambulanceMarkers.get(a.id);
            marker.setLatLng([a.latitude, a.longitude]);
            marker.setIcon(customIcon);
            marker.setPopupContent(popup);
            marker.setZIndexOffset(0);
            return;
        }
        const marker = L.marker([a.latitude, a.longitude], {
            title: a.code,
            icon: customIcon,
        })
            .addTo(map)
            .bindPopup(popup);
        ambulanceMarkers.set(a.id, marker);
    });
}

async function loadStats() {
    const stats = await makeNetworkCall(`${BACKEND_URL}/admin/stats`);
    document.getElementById("totalHospitals").textContent =
        stats.totalHospitals;
    document.getElementById("hospitalsWithBeds").textContent =
        stats.hospitalsWithBeds;
    document.getElementById("availableAmbulances").textContent =
        stats.availableAmbulances;
}

document.getElementById("confirmAddressBtn").addEventListener("click", () => {
    if (!selectedLocation) {
        document.getElementById("requestResult").textContent =
            "Select a patient location first.";
        return;
    }
    isAddressVerified = true;
    document.getElementById("requestBtn").disabled = false;
    document.getElementById("requestResult").textContent =
        `Address confirmed: ${selectedAddress}`;
    emitSuccessTone();
});

document.getElementById("requestBtn").addEventListener("click", async () => {
    try {
        if (!selectedLocation)
            throw new Error("Select patient location on map first");
        if (!isAddressVerified)
            throw new Error("Confirm patient address before dispatch");
        const payload = {
            latitude: selectedLocation.lat,
            longitude: selectedLocation.lng,
            severity: document.getElementById("severity").value,
            requiredSpecialization:
                document.getElementById("specialization").value,
            hospitalPreference:
                document.getElementById("hospitalPreference").value,
        };
        const routePreference =
            document.getElementById("routePreference").value;

        const data = await makeNetworkCall(`${BACKEND_URL}/emergency/request`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });

        const ambulanceToPatientRoutes = await fetchRoadRoute(
            { lat: data.ambulance.latitude, lng: data.ambulance.longitude },
            { lat: selectedLocation.lat, lng: selectedLocation.lng },
        );
        const patientToHospitalRoutes = await fetchRoadRoute(
            { lat: selectedLocation.lat, lng: selectedLocation.lng },
            { lat: data.hospital.latitude, lng: data.hospital.longitude },
        );
        const selectedLeg1 = pickRoute(
            ambulanceToPatientRoutes,
            routePreference,
        );

        if (routeLine1) map.removeLayer(routeLine1);
        if (routeLine2) map.removeLayer(routeLine2);
        if (window.routeLinesPhase2) {
            window.routeLinesPhase2.forEach((item) =>
                map.removeLayer(item.line),
            );
        }

        routeLine1 = sketchPath(selectedLeg1.geometry.coordinates, "blue");

        currentCase = data;
        isEmergencyActive = true;
        updateAssignedHospitalId(data.hospital.id);

        document.getElementById("requestResult").textContent =
            `Assigned ${data.ambulance.code} -> ${data.hospital.name} [${data.hospital.type}] | ETA to Patient: ${Math.ceil(selectedLeg1.duration / 60)} mins`;

        beginAmbulanceRun(
            data.ambulance.id,
            selectedLeg1,
            patientToHospitalRoutes,
        );

        notify("Ambulance assigned");
        let uiTrafficPercent = Math.floor(Math.random() * 50) + 15;
        notify(`Live Traffic: ${uiTrafficPercent}% Delay`);
        notify(`Arriving in ${data.etaMinutes} mins`);
        emitWarningBeep();
        loadAmbulances();
        loadHospitals();
        loadStats();
    } catch (e) {
        document.getElementById("requestResult").textContent = e.message;
    }
});

document.getElementById("handoverBtn").addEventListener("click", async () => {
    try {
        const pName = document.getElementById("patientName").value;
        const aBy = document.getElementById("admittedBy").value;
        const aPhone = document.getElementById("admitterPhone").value;
        const pAge = document.getElementById("age").value;

        if (!pName || !aBy || !aPhone || pAge === "") {
            throw new Error(
                "Please properly fill out all handover fields including phone number.",
            );
        }

        const payload = {
            patientName: pName,
            admittedBy: aBy,
            admitterPhone: aPhone,
            age: Number(pAge),
            status: "COMPLETED",
            hospitalId: Number(
                document.getElementById("handoverHospitalId").value,
            ),
            ambulanceId: currentCase.ambulance.id,
            totalTime: window.lastRideTotalTime || "Unknown",
            cost: window.lastRideCost || 500,
            source: selectedAddress
        };
        const res = await makeNetworkCall(`${BACKEND_URL}/handover`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });

        isEmergencyActive = false;
        document.getElementById("handoverModal").style.display = "none";

        document.getElementById("patientName").value = "";
        document.getElementById("admittedBy").value = "";
        document.getElementById("admitterPhone").value = "";
        document.getElementById("age").value = "";
        document.getElementById("handoverResult").textContent = "";

        document.getElementById("selectedLocation").textContent =
            "Not selected";
        document.getElementById("selectedAddress").textContent = "Not fetched";
        if (selectedMarker) map.removeLayer(selectedMarker);
        if (routeLine1) map.removeLayer(routeLine1);
        if (window.routeLinesPhase2) {
            window.routeLinesPhase2.forEach((item) =>
                map.removeLayer(item.line),
            );
        }
        selectedMarker = null;
        selectedLocation = null;
        document.getElementById("requestBtn").disabled = true;

        refreshAll();

        document.getElementById("summaryBookingTime").textContent = new Date().toLocaleString();
        document.getElementById("summarySource").textContent = window.lastRideSource || "Unknown";
        document.getElementById("summaryDestination").textContent = window.lastRideDestination || "Unknown";
        document.getElementById("summaryTotalTime").textContent = window.lastRideTotalTime || "Unknown";
        document.getElementById("summaryRiderName").textContent = "Amit Sharma";
        document.getElementById("summaryCost").textContent = "₹" + (window.lastRideCost || 500);
        document.getElementById("rideSummaryModal").style.display = "flex";
        
        document.getElementById("closeSummaryBtn").onclick = () => {
            document.getElementById("rideSummaryModal").style.display = "none";
        };

        notify(
            "Emergency Phase Concluded! The system is now unlocked for standard operations.",
        );
        emitSuccessTone();
    } catch (e) {
        document.getElementById("handoverResult").textContent = e.message;
    }
});

document.getElementById("updateBedsBtn").addEventListener("click", async () => {
    const id = Number(document.getElementById("hospitalIdInput").value);
    const beds = Number(document.getElementById("bedsInput").value);
    await makeNetworkCall(`${BACKEND_URL}/hospitals/${id}/beds`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ availableBeds: beds }),
    });
    loadHospitals();
    loadStats();
    emitSuccessTone();
});

document
    .getElementById("updateAmbulanceBtn")
    .addEventListener("click", async () => {
        const code = document.getElementById("ambulanceIdInput").value.trim();
        const status = document.getElementById("ambulanceStatusInput").value;
        try {
            await makeNetworkCall(`${BACKEND_URL}/ambulances/code/${code}/status`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ status }),
            });
            loadAmbulances();
            loadStats();
            emitSuccessTone();
        } catch (e) {
            notify("Error: " + e.message);
        }
    });

document.getElementById("addHospitalMapBtn").addEventListener("click", () => {
    isAddingHospital = true;
    setSystemLock(true);
    document.getElementById("addHospitalResult").textContent =
        "Please select hospital location on the map to continue";
    document.getElementById("addHospitalMapBtn").disabled = true;
});

document
    .getElementById("addHospitalCancelBtn")
    .addEventListener("click", () => {
        isAddingHospital = false;
        setSystemLock(false);
        if (draftHospMarker) {
            map.removeLayer(draftHospMarker);
            draftHospMarker = null;
        }
        tempHospitalLocation = null;
        document.getElementById("newHospitalId").value = "";
        document.getElementById("newHospitalName").value = "";
        document.getElementById("newHospitalBeds").value = "";
        document.getElementById("addHospitalMapBtn").disabled = false;
        document.getElementById("addHospitalSubmitBtn").disabled = true;
        document.getElementById("addHospitalResult").textContent = "";
    });

document
    .getElementById("addHospitalSubmitBtn")
    .addEventListener("click", async () => {
        try {
            const id = document.getElementById("newHospitalId").value;
            const name = document.getElementById("newHospitalName").value;
            const beds = document.getElementById("newHospitalBeds").value;
            const spec = document.getElementById("newHospitalSpec").value;
            const type = document.getElementById("newHospitalType").value;

            if (!id || !name || beds === "") {
                throw new Error("All fields are mandatory.");
            }
            if (Number(beds) < 0) {
                throw new Error("Beds must be a non-negative number.");
            }
            if (!tempHospitalLocation) {
                throw new Error("Please select hospital location on the map.");
            }

            const payload = {
                id: Number(id),
                name: name,
                availableBeds: Number(beds),
                specialization: spec,
                type: type,
                latitude: tempHospitalLocation.lat,
                longitude: tempHospitalLocation.lng,
            };

            await makeNetworkCall(`${BACKEND_URL}/hospitals`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            notify("Hospital added successfully 🏥");
            document.getElementById("addHospitalCancelBtn").click();
            document.getElementById("addHospitalResult").textContent =
                "Hospital added successfully 🏥";

            emitSuccessTone();

            loadHospitals();
            loadStats();
        } catch (e) {
            document.getElementById("addHospitalResult").textContent =
                e.message;
        }
    });

document.getElementById("addAmbulanceMapBtn").addEventListener("click", () => {
    isAddingAmbulance = true;
    setSystemLock(true);
    document.getElementById("addAmbulanceResult").textContent =
        "Please select ambulance location on the map to continue";
    document.getElementById("addAmbulanceMapBtn").disabled = true;
});

document
    .getElementById("addAmbulanceCancelBtn")
    .addEventListener("click", () => {
        isAddingAmbulance = false;
        setSystemLock(false);
        if (tempAmbulanceMarker) {
            map.removeLayer(tempAmbulanceMarker);
            tempAmbulanceMarker = null;
        }
        tempAmbulanceLocation = null;
        document.getElementById("newAmbulanceCode").value = "";
        document.getElementById("addAmbulanceMapBtn").disabled = false;
        document.getElementById("addAmbulanceSubmitBtn").disabled = true;
        document.getElementById("addAmbulanceResult").textContent = "";
    });

document
    .getElementById("addAmbulanceSubmitBtn")
    .addEventListener("click", async () => {
        try {
            const code = document.getElementById("newAmbulanceCode").value;

            if (!code) {
                throw new Error("Ambulance ID / Code is required.");
            }
            if (!tempAmbulanceLocation) {
                throw new Error("Please select ambulance location on the map.");
            }

            const payload = {
                code: code,
                latitude: tempAmbulanceLocation.lat,
                longitude: tempAmbulanceLocation.lng,
            };

            const res = await makeNetworkCall(`${BACKEND_URL}/ambulances`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (res.error) {
                throw new Error(res.error);
            }

            notify("Ambulance added successfully 🚑");
            document.getElementById("addAmbulanceCancelBtn").click();
            document.getElementById("addAmbulanceResult").textContent =
                "Ambulance added successfully and is now Available!";

            emitSuccessTone();

            loadAmbulances();
            loadStats();
        } catch (e) {
            document.getElementById("addAmbulanceResult").textContent =
                e.message || "Failed to add ambulance.";
        }
    });

async function refreshAll() {
    try {
        await Promise.all([loadHospitals(), loadAmbulances(), loadStats()]);
    } catch (e) {
        console.error(e);
    }
}

refreshAll();
setInterval(refreshAll, 5000);
