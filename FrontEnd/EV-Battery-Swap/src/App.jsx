import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'    
import './App.css'
import Header from './components/Header'

function App() {
  

  return (
    <>
      <Header />
      <main className="app-main" style={{ padding: 0, margin: 0 }}>
        <video
          autoPlay
          loop
          muted
          playsInline
          style={{
            position: 'absolute',
            top: '64px', // height of header
            left: 0,
            width: '100vw',
            height: 'calc(100vh - 64px)',
            objectFit: 'cover',
            display: 'block',
            border: 'none',
            background: '#000',
            zIndex: 0
          }}
        >
          <source src="/promo.mp4" type="video/mp4" />
          Your browser does not support the video tag.
        </video>
        {/* ...other main content... */}
      </main>
    </>
  )
}

export default App
