const Station = require('../models/Station');

// Create Station
exports.createStation = async (req, res) => {
  try {
    const station = new Station(req.body);
    await station.save();
    res.status(201).json(station);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
};

// Get all Stations
exports.getStations = async (req, res) => {
  try {
    const stations = await Station.find().populate('batteries staffs');
    res.json(stations);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

// Get Station by ID
exports.getStationById = async (req, res) => {
  try {
    const station = await Station.findById(req.params.id).populate('batteries staffs');
    if (!station) return res.status(404).json({ error: 'Station not found' });
    res.json(station);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

// Update Station
exports.updateStation = async (req, res) => {
  try {
    const station = await Station.findByIdAndUpdate(req.params.id, req.body, { new: true });
    if (!station) return res.status(404).json({ error: 'Station not found' });
    res.json(station);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
};

// Delete Station
exports.deleteStation = async (req, res) => {
  try {
    const station = await Station.findByIdAndDelete(req.params.id);
    if (!station) return res.status(404).json({ error: 'Station not found' });
    res.json({ message: 'Station deleted' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};
