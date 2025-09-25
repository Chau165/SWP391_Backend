import Header from './components/Header'
import Footer from './components/Footer/Footer'

function App() {
  return (
    <>
      <Header />
      <main style={{ padding: 0, margin: 0, overflow: 'hidden' }}>
  <video
    autoPlay
    loop
    muted
    playsInline
    controls={false}
    style={{
      position: 'relative',
      width: '100vw', /* Sửa từ 100% thành 100vw */
      height: '100vh',
      objectFit: 'cover',
      display: 'block',
      background: '#000'
    }}
  >
    <source src="/promo.mp4" type="video/mp4" />
  </video>
  <Footer />
</main>
    </>
  )
}

export default App
