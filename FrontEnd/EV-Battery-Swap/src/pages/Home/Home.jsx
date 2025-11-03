import React, { useRef, useEffect } from 'react';
import 'mapbox-gl/dist/mapbox-gl.css';
import mapboxgl from 'mapbox-gl';
import reactLogo from '../../assets/react.svg';
import ReservationForm from '../../components/ReserveForm/ReservationForm';
import UserDashboard from '../../components/UserDashboard/UserDashboard';

export default function Home() {
  const mapContainer = useRef(null);
  const map = useRef(null);

  // Hard-coded 25 station locations in Ho Chi Minh City
  const stations = [
    { id: 1, name: "Station 1", district: "District 1", coords: [106.660172, 10.762622] },
    { id: 2, name: "Station 2", district: "District 3", coords: [106.700981, 10.776889] },
    { id: 3, name: "Station 3", district: "District 5", coords: [106.682231, 10.762913] },
    { id: 4, name: "Station 4", district: "District 7", coords: [106.629662, 10.823099] },
    { id: 5, name: "Station 5", district: "District 9", coords: [106.800000, 10.870000] },
    { id: 6, name: "Station 6", district: "District 1", coords: [106.660500, 10.770000] },
    { id: 7, name: "Station 7", district: "District 2", coords: [106.715000, 10.780000] },
    { id: 8, name: "Station 8", district: "District 3", coords: [106.680000, 10.765000] },
    { id: 9, name: "Station 9", district: "District 4", coords: [106.690000, 10.770000] },
    { id: 10, name: "Station 10", district: "District 5", coords: [106.695000, 10.775000] },
    { id: 11, name: "Station 11", district: "District 6", coords: [106.700000, 10.780000] },
    { id: 12, name: "Station 12", district: "District 7", coords: [106.705000, 10.785000] },
    { id: 13, name: "Station 13", district: "District 8", coords: [106.710000, 10.790000] },
    { id: 14, name: "Station 14", district: "District 9", coords: [106.715000, 10.795000] },
    { id: 15, name: "Station 15", district: "District 10", coords: [106.720000, 10.800000] },
    { id: 16, name: "Station 16", district: "District 11", coords: [106.725000, 10.805000] },
    { id: 17, name: "Station 17", district: "District 12", coords: [106.730000, 10.810000] },
    { id: 18, name: "Station 18", district: "Binh Thanh", coords: [106.735000, 10.815000] },
    { id: 19, name: "Station 19", district: "Phu Nhuan", coords: [106.740000, 10.820000] },
    { id: 20, name: "Station 20", district: "Go Vap", coords: [106.745000, 10.825000] },
    { id: 21, name: "Station 21", district: "Tan Binh", coords: [106.750000, 10.830000] },
    { id: 22, name: "Station 22", district: "Tan Phu", coords: [106.755000, 10.835000] },
    { id: 23, name: "Station 23", district: "Binh Tan", coords: [106.760000, 10.840000] },
    { id: 24, name: "Station 24", district: "Thu Duc", coords: [106.765000, 10.845000] },
    { id: 25, name: "Station 25", district: "District 1", coords: [106.770000, 10.850000] }
  ];

  useEffect(() => {
    mapboxgl.accessToken = 'pk.eyJ1Ijoia2hvaXZ1engiLCJhIjoiY21nNHcyZXZ4MHg5ZTJtcGtrNm9hbmVpciJ9.N3prC7rC3ycR6DV5giMUfg'; // TODO: Replace with your token
    if (map.current) return;
    map.current = new mapboxgl.Map({
      container: mapContainer.current,
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [106.660172, 10.762622], // [lng, lat] (Ho Chi Minh City)
      zoom: 12
    });

    stations.forEach((station) => {
      const el = document.createElement('div');
      el.className = 'custom-marker';
      el.style.backgroundImage = `url(${reactLogo})`;
      el.style.width = '36px';
      el.style.height = '36px';
      el.style.backgroundSize = '100%';
      el.style.borderRadius = '50%';
      el.style.boxShadow = '0 2px 8px rgba(0,0,0,0.15)';
      el.style.cursor = 'pointer';
      const popup = new mapboxgl.Popup({ offset: 25 })
        .setHTML(`
          <div style="color: #111; font-size: 1.1em; font-weight: 600;">${station.name}</div>
          <div style="margin-top: 6px; color: #333; font-size: 0.95em;">
            Available battery: <span id="battery-${station.id}">Loading...</span>
          </div>
        `);
      // Optionally, fetch battery info from backend here and update the span
      // Example:
      // fetch(`/api/station/${station.id}/battery`).then(res => res.json()).then(data => {
      //   const batterySpan = document.getElementById(`battery-${station.id}`);
      //   if (batterySpan) batterySpan.textContent = data.available;
      // });
      new mapboxgl.Marker(el)
        .setLngLat(station.coords)
        .setPopup(popup)
        .addTo(map.current);
    });
  }, []);

  return (
    <main style={{ padding: 0, margin: 0 }}>
      <video
        autoPlay
        loop
        muted
        playsInline
        controls={false}
        style={{
          position: 'relative',
          width: '100%',
          height: '100%',
          objectFit: 'cover',
          display: 'block',
          background: '#000'
        }}
      >
        <source src="/promo.mp4" type="video/mp4" />
      </video>

      <div
        ref={mapContainer}
        style={{
          width: '100vw',
          height: '80vh',
          maxWidth: '100%',
          margin: '0 auto',
          marginTop: 24,
          borderRadius: 12,
          overflow: 'hidden',
          boxShadow: '0 4px 24px rgba(0,0,0,0.15)'
        }}
      />

      {/* User Dashboard - Role-based content */}
      <UserDashboard />

      {/* Reservation Form */}
      <ReservationForm stations={stations} />
    </main>
  );
}
