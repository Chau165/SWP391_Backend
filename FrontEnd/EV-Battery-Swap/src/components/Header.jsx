import reactLogo from '../assets/react.svg'
import viteLogo from '/vite.svg'
import './Header.css'

export default function Header() {
    return (
        <header className="site-header">
            <div className="header-inner">
                <a className="brand" href="/">
                    <img src={viteLogo} alt="Vite" className="brand-logo" />
                    <img src={reactLogo} alt="React" className="brand-logo react" />
                    <span className="brand-title">EV Battery Swap</span>
                </a>
                <nav className="main-nav">
                    <a href="/">Home</a>
                    <a href="/about">About</a>
                    <a href="/contact">Contact</a>
                </nav>
            </div>
        </header>
    )
}