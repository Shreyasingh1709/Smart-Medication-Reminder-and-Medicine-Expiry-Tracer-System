import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import "./App.css";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import PatientDashboard from "./pages/PatientDashboard";
import CaregiverDashboard from "./pages/CaregiverDashboard";
import ExpiryTracker from "./pages/ExpiryTracker";
import Reminders from "./pages/Reminders";
import Help from "./pages/Help";
import Layout from "./components/Layout";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route
          path="/patient-dashboard"
          element={
            <Layout>
              <PatientDashboard />
            </Layout>
          }
        />

        <Route
          path="/caregiver-dashboard"
          element={
            <Layout>
              <CaregiverDashboard />
            </Layout>
          }
        />

        <Route
          path="/expiry"
          element={
            <Layout>
              <ExpiryTracker />
            </Layout>
          }
        />

        <Route
          path="/reminders"
          element={
            <Layout>
              <Reminders />
            </Layout>
          }
        />

        <Route
          path="/help"
          element={
            <Layout>
              <Help />
            </Layout>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;