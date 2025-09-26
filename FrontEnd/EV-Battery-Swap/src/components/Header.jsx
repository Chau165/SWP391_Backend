import React, { useEffect, useState } from 'react'
import logo from '../assets/react.svg'
import './Header.css'
/* edit khi lăn chuột thì thanh header sẽ hiện*/ 
export default function Header() {
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 10);
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  return (
    <header className={`site-header ${scrolled ? 'scrolled' : ''}`}>
      
      <div className="header-inner">
        <a className="brand" href="/" aria-label="home"> 
          <img src={logo} alt="" className="brand-logo" />
          <span className="brand-title">EV Battery Swap</span>
        </a>

        <div className="nav-wrapper" aria-hidden={false}>
          <nav className="main-nav" aria-label="Primary">
            <a href="/" className="nav-link">Home</a>
            <a href="/battery" className="nav-link">Battery Electric</a>
            <a href="/policies" className="nav-link">Polices</a>
            <a href="/about" className="nav-link">About Us</a>
            <a href="/contact" className="nav-link">Contact</a>
          </nav>
        </div>

        <div className="actions">
          <a className="cta login" href="/login">Login</a>
        </div>
      </div>
    </header>
    
  )
}
