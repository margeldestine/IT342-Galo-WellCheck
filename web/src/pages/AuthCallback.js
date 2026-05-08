import { useEffect } from 'react';

function AuthCallback() {
  useEffect(() => {
    const run = async () => {
      const hash = window.location.href;
      const urlParams = new URL(hash);
      const token = urlParams.searchParams.get('token');
      const error = urlParams.searchParams.get('error');
      const email = urlParams.searchParams.get('email');
      const firstName = urlParams.searchParams.get('firstName');
      const lastName = urlParams.searchParams.get('lastName');
      const isNewUser = urlParams.searchParams.get('isNewUser');
      const role = urlParams.searchParams.get('role') || 'STUDENT';
      const status = urlParams.searchParams.get('status') || 'ACTIVE';

      console.log('Token:', token);
      console.log('Role:', role);
      console.log('Status:', status);
      console.log('Is New User:', isNewUser);

      if (!token) {
        console.error('Google login failed:', error);
        window.location.href = '/login';
        return;
      }

      let user = { token, email, firstName, lastName, role, status };
      localStorage.setItem('token', token);
      localStorage.setItem('role', role);
      localStorage.setItem('user', JSON.stringify(user));

      if (status === 'PENDING') {
        window.location.href = '/pending';
        return;
      }

      
      try {
        const res = await fetch(`${process.env.REACT_APP_API_URL}/auth/me`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        console.log('ME status:', res.status);
        if (res.ok) {
          const fullProfile = await res.json();
          console.log('ME response:', fullProfile);
          user = { ...user, ...fullProfile, token };
          console.log('Final user saved:', user);
          localStorage.setItem('user', JSON.stringify(user));
        }
      } catch (e) {
        console.warn('Could not fetch full profile:', e);
      }

      if (isNewUser === 'true') {
        if (role === 'COUNSELOR') {
          window.location.href = '/complete-counselor-profile';
        } else {
          window.location.href = '/complete-profile';
        }
      } else {
        if (role === 'COUNSELOR') {
          window.location.href = '/counselor/dashboard';
        } else {
          window.location.href = '/dashboard';
        }
      }
    };

    run();
  }, []);

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <p>Logging you in with Google...</p>
    </div>
  );
}

export default AuthCallback;