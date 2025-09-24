import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'

function App() {
  

  return (
    <>
      <Header />
      <main className="app-main">
        {/* ...nội dung trang chính... */}
        <div style={{ padding: 24 }}>
          <h1>Welcome</h1>
          <p>Click the button to increment:</p>
          <button onClick={() => setCount(c => c + 1)}>count is {count}</button>
        </div>
      </main>
    </>
  )
}

export default App
