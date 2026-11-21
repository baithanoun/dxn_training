import { BrowserRouter, Routes, Route } from "react-router-dom"
import AdminLayout from "@/layouts/AdminLayout"
import Overview from "@/pages/Overview"
import Content from "@/pages/Content"
import ScenarioBuilder from "@/pages/ScenarioBuilder"
import Users from "@/pages/Users"
import Teams from "@/pages/Teams"
import Analytics from "@/pages/Analytics"
import Settings from "@/pages/Settings"

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AdminLayout />}>
          <Route index element={<Overview />} />
          <Route path="content" element={<Content />} />
          <Route path="scenario" element={<ScenarioBuilder />} />
          <Route path="users" element={<Users />} />
          <Route path="teams" element={<Teams />} />
          <Route path="analytics" element={<Analytics />} />
          <Route path="settings" element={<Settings />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
