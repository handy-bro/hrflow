const apiBase = '/api';

export const environment = {
  production: false,

  authEndpoints: {
    login: `${apiBase}/auth/login`,
    register: `${apiBase}/auth/register`,
    setPassword: `${apiBase}/auth/set-password`,
    resetPassword: `${apiBase}/auth/reset-password`,
    forgotPassword: `${apiBase}/auth/forgot-password`,
    resendActivation: `${apiBase}/auth/resend-activation`,
    validateActivationToken: `${apiBase}/auth/validate-activation-token`,
    createUserByAdmin: `${apiBase}/auth/admin/users`,
  },

  employeeEndpoints: {
    list: `${apiBase}/employees`,
    create: `${apiBase}/employees`,
    update: `${apiBase}/employees/:id`,
    delete: `${apiBase}/employees/:id`,
    restore: `${apiBase}/employees/:id/restore`,
    archive: `${apiBase}/employees/:id/archive`,
    changeDepartment: `${apiBase}/employees/:id/department`,
    uploadProfilePhoto: `${apiBase}/employees/:id/upload-profile-photo`,
    exportPdf: `${apiBase}/employees/export/pdf`,
    exportCsv: `${apiBase}/employees/export/csv`,
  },

  departmentEndpoints: {
    getAll: `${apiBase}/departments`,
    getById: `${apiBase}/departments/:id`,
    create: `${apiBase}/departments`,
    update: `${apiBase}/departments/:id`,
    delete: `${apiBase}/departments/:id`,
    search: `${apiBase}/departments/search`,
  },

  payslipEndpoints: {
    generate: `${apiBase}/payslips/employee/:employeeId/generate`,
    regenerate: `${apiBase}/payslips/employee/:employeeId/regenerate`,
    myHistory: `${apiBase}/payslips/me`,
    employeeHistory: `${apiBase}/payslips/employee/:employeeId`,
    downloadPdf: `${apiBase}/payslips/:id/download`,
  },

  leaveEndpoints: {
    submit: `${apiBase}/leaves`,
    review: `${apiBase}/leaves/:id/review`,
    cancel: `${apiBase}/leaves/:id/cancel`,
    myHistory: `${apiBase}/leaves/me`,
    myBalance: `${apiBase}/leaves/me/balance`,
    employeeHistory: `${apiBase}/leaves/employee/:employeeId`,
    employeeBalance: `${apiBase}/leaves/employee/:employeeId/balance`,
    teamCalendar: `${apiBase}/leaves/calendar`,
  },

  attendanceEndpoints: {
    checkIn: `${apiBase}/attendance/check-in`,
    checkOut: `${apiBase}/attendance/check-out`,
    myReport: `${apiBase}/attendance/me/report`,
    employeeReport: `${apiBase}/attendance/employee/:employeeId/report`,
    departmentToday: `${apiBase}/attendance/department/:departmentId/today`,
  },

  dashboardEndpoints: {
    summary: `${apiBase}/dashboard/summary`,
    workforceTrend: `${apiBase}/dashboard/workforce-trend`,
    newEmployees: `${apiBase}/dashboard/new-employees`,
    departmentDistribution: `${apiBase}/dashboard/department-distribution`,
    attendanceRate: `${apiBase}/dashboard/attendance-rate`,
    alerts: `${apiBase}/dashboard/alerts`,
  }
};