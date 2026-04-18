/**
 * main.tsx
 * Application entry point — mounts the React root into #root.
 * Wraps the entire tree in an ErrorBoundary so any uncaught render
 * error shows a readable crash screen instead of a blank page.
 */
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import { ErrorBoundary } from './components/ErrorBoundary.tsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </React.StrictMode>,
)
