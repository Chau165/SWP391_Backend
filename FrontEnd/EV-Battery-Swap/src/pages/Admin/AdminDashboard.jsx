import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './AdminDashboard.css';

export default function AdminDashboard() {
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function fetchStations() {
      try {
        setLoading(true);
        // Expect backend endpoint at /api/stations
        const res = await axios.get('/api/stations');
        setStations(res.data || []);
      } catch (err) {
        setError(err.message || 'Failed to load stations');
      } finally {
        setLoading(false);
      }
    }

    fetchStations();
  }, []);

  return (
    <div className="admin-dashboard page-container">
      <h1>Admin Dashboard — Station Management</h1>

      <section className="panel">
        <header className="panel-header">
          <h2>Stations</h2>
        </header>

        <div className="panel-body">
          {loading && <div className="muted">Loading stations…</div>}
          {error && <div className="error">Error: {error}</div>}

          {!loading && !error && (
            <table className="stations-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Location</th>
                  <th>Batteries Available</th>
                  <th>Staffs</th>
                </tr>
              </thead>
              <tbody>
                {stations.length === 0 && (
                  <tr><td colSpan={5} className="muted">No stations found</td></tr>
                )}
                {stations.map((s) => (
                  <tr key={s._id || s.id}>
                    <td>{s._id || s.id}</td>
                    <td>{s.name}</td>
                    <td>{s.location || '-'}</td>
                    <td>{(s.batteries && s.batteries.length) || s.batteryCount || 0}</td>
                    <td>{(s.staffs && s.staffs.length) || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>

      <section className="panel">
        <header className="panel-header">
          <h2>Dispatch Requests</h2>
        </header>
        <div className="panel-body muted">Dispatch request list and approval UI will appear here (backend endpoints: GET /api/dispatch-logs, POST /api/dispatch-logs/:id/approve).</div>
      </section>

      <section className="panel">
        <header className="panel-header">
          <h2>Analytics</h2>
        </header>
        <div className="panel-body muted">Basic analytics (station capacity, battery levels, requests) will be added here.</div>
      </section>
    </div>
  );
}
