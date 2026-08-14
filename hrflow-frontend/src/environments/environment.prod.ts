const apiBase = '/api';
const authBaseUrl = `${apiBase}/auth`

export const environment = {
  production: true,
  
  // Auth endpoints
  authEnpoints: {
    login: `${authBaseUrl}/login`,
    register: `${authBaseUrl}/register`,
    forgotPassword: `${authBaseUrl}/forgot-password`,    
    resetPassword: `${authBaseUrl}/reset-password`,     
    verifyEmail: `${authBaseUrl}/verify-email`,       
    resendVerification: `${authBaseUrl}/resend-verification`
  },
};