import React, { useEffect, useState } from 'react'
import './LoginModal.css'

export default function LoginModal({ visible, onClose, onSuccess, apiBase = '/api/auth' }) {
  const [mode, setMode] = useState('login') // 'login' | 'register'
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (visible) document.body.style.overflow = 'hidden'
    else document.body.style.overflow = ''
    const onKey = (e) => { if (e.key === 'Escape') onClose() }
    window.addEventListener('keydown', onKey)
    return () => {
      document.body.style.overflow = ''
      window.removeEventListener('keydown', onKey)
    }
  }, [visible, onClose])

  if (!visible) return null

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const endpoint = mode === 'login' ? `${apiBase}/login` : `${apiBase}/register`
      const res = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      })
      const data = await res.json()
      if (!res.ok) {
        setError(data?.message || 'Request failed')
      } else {
        if (onSuccess) onSuccess(data)
      }
    } catch (err) {
      setError('Network error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="lm-overlay" onMouseDown={onClose} role="dialog" aria-modal="true" aria-label="Auth dialog">
      <div className="lm-modal" onMouseDown={(e) => e.stopPropagation()}>
        <button className="lm-close" onClick={onClose} aria-label="Close">×</button>
        <h3>{mode === 'login' ? 'Login' : 'Register'}</h3>

        <form onSubmit={submit} className="lm-form">
          <label>
            Email
            <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
          </label>

          <label>
            Password
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} required minLength={6} />
          </label>

          {error && <div className="lm-error">{error}</div>}

          <button type="submit" className="lm-btn" disabled={loading}>
            {loading ? 'Please wait...' : (mode === 'login' ? 'Sign in' : 'Create account')}
          </button>
        </form>

        <div className="lm-switch">
          {mode === 'login' ? (
            <>
              <span>Don't have an account?</span>
              <button className="lm-link" onClick={() => setMode('register')}>Register</button>
            </>
          ) : (
            <>
              <span>Already have an account?</span>
              <button className="lm-link" onClick={() => setMode('login')}>Login</button>
            </>
          )}
        </div>
      </div>
    </div>
  )
}