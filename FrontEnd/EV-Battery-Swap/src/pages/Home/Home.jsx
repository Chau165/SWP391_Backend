import React, { useRef, useEffect } from 'react';
import 'mapbox-gl/dist/mapbox-gl.css';
import mapboxgl from 'mapbox-gl';
import reactLogo from '../../assets/react.svg';

export default function Home() {
  const mapContainer = useRef(null);
  const map = useRef(null);

  useEffect(() => {
    mapboxgl.accessToken = 'pk.eyJ1Ijoia2hvaXZ1engiLCJhIjoiY21nNHcyZXZ4MHg5ZTJtcGtrNm9hbmVpciJ9.N3prC7rC3ycR6DV5giMUfg'; // TODO: Replace with your token
    if (map.current) return;
    map.current = new mapboxgl.Map({
      container: mapContainer.current,
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [106.660172, 10.762622], // [lng, lat] (Ho Chi Minh City)
      zoom: 12
    });

    // Example station locations in Ho Chi Minh City
    const stations = [
      [106.660172, 10.762622],
      [106.700981, 10.776889],
      [106.682231, 10.762913],
      [106.629662, 10.823099],
      [106.800000, 10.870000]
    ];

    stations.forEach((coords, idx) => {
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
        .setHTML(`<div>Station #${idx + 1}</div>`);
      new mapboxgl.Marker(el)
        .setLngLat(coords)
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
    </main>
  );
}
