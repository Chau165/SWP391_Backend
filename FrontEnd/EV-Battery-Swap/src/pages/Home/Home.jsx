import React from 'react';

export default function Home() {
  return (
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
          height: '100%',
          objectFit: 'cover',
          display: 'block',
          background: '#000'
        }}
      >
        <source src="/promo.mp4" type="video/mp4" />
      </video>
      {/* ...other main content... */}
    </main>
  );
}
