import Header from './components/Header'
import Footer from './components/Footer/Footer'

function App() {
  return (
    <>
      <Header />
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
            height: '100vh',
            objectFit: 'cover',
            display: 'block',
            background: '#000'
          }}
        >
          <source src="/promo.mp4" type="video/mp4" />
        </video>

        {/* Footer nằm dưới video */}
        <Footer />
      </main>
    </>
  )
}

export default App
