import React, { useState } from 'react';

export default function ReservationForm({ stations }) {
  const [selectedStation, setSelectedStation] = useState("");
  const [selectedBattery, setSelectedBattery] = useState("");
  const [msg, setMsg] = useState("");
  const batteryOptions = [
    { id: 'A', label: 'Battery A' },
    { id: 'B', label: 'Battery B' },
    { id: 'C', label: 'Battery C' }
  ];

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!selectedStation || !selectedBattery) {
      setMsg("Please select both station and battery.");
      return;
    }
    setMsg(`Reservation submitted for ${selectedStation} with ${selectedBattery}. (Mock)`);
  };

  return (
    <form
      onSubmit={handleSubmit}
      style={{
        maxWidth: 400,
        margin: '32px auto',
        padding: 24,
        background: '#fff',
        borderRadius: 12,
        boxShadow: '0 2px 12px rgba(0,0,0,0.10)',
        display: 'flex',
        flexDirection: 'column',
        gap: 18
      }}
    >
      <h2 style={{ margin: 0, fontSize: '1.3em', fontWeight: 600 }}>Reserve Battery</h2>
      <label style={{ textAlign: 'left' }}>
        Station:
        <select
          value={selectedStation}
          onChange={e => setSelectedStation(e.target.value)}
          style={{ width: '100%', padding: '8px', marginTop: 6, borderRadius: 6 }}
        >
          <option value="">Select station...</option>
          {stations.map(station => (
            <option key={station.id} value={station.name}>{station.name}</option>
          ))}
        </select>
      </label>
      <label style={{ textAlign: 'left' }}>
        Battery:
        <select
          value={selectedBattery}
          onChange={e => setSelectedBattery(e.target.value)}
          style={{ width: '100%', padding: '8px', marginTop: 6, borderRadius: 6 }}
        >
          <option value="">Select battery...</option>
          {batteryOptions.map(bat => (
            <option key={bat.id} value={bat.label}>{bat.label}</option>
          ))}
        </select>
      </label>
      <button
        type="submit"
        style={{
          padding: '10px 0',
          background: '#1976d2',
          color: '#fff',
          fontWeight: 600,
          border: 'none',
          borderRadius: 8,
          fontSize: '1em',
          cursor: 'pointer'
        }}
      >
        Reserve
      </button>
      {msg && (
        <div style={{ color: '#1976d2', marginTop: 8, fontWeight: 500 }}>{msg}</div>
      )}
    </form>
  );
}
