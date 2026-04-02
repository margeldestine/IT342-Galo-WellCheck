import { useEffect } from 'react';

function AuthCallback() {
  useEffect(() => {
    const hash = window.location.href;
    const urlParams = new URL(hash);
    const token = urlParams.searchParams.get('token');
    const error = urlParams.searchParams.get('error');
    const email = urlParams.searchParams.get('email');
    const firstName = urlParams.searchParams.get('firstName');
    const lastName = urlParams.searchParams.get('lastName');
    const isNewUser = urlParams.searchParams.get('isNewUser');

    console.log('Full URL:', hash);
    console.log('Token:', token);
    console.log('Is New User:', isNewUser);

    if (token) {
      const user = { token, email, firstName, lastName, role: 'STUDENT' };
      localStorage.setItem('token', token);
      localStorage.setItem('role', 'STUDENT');
      localStorage.setItem('user', JSON.stringify(user));

      if (isNewUser === 'true') {
        window.location.href = '/complete-profile';
      } else {
        window.location.href = '/dashboard';
      }
    } else {
      console.error('Google login failed:', error);
      window.location.href = '/login';
    }
  }, []);

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <p>Logging you in with Google...</p>
    </div>
  );
}

export default AuthCallback;