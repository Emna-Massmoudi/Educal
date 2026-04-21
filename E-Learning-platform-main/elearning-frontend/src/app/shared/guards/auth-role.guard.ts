import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

type UserRole = 'ADMIN' | 'FORMATEUR' | 'ETUDIANT';

function getRoleHome(role: string | null): string {
  switch (role) {
    case 'ADMIN':
      return '/admin';
    case 'FORMATEUR':
      return '/teacher';
    case 'ETUDIANT':
      return '/student';
    default:
      return '/home';
  }
}

export const authGuard: CanActivateFn = (_, state) => {
  const router = inject(Router);
  const token = localStorage.getItem('token');

  if (token) {
    return true;
  }

  return router.createUrlTree(['/login'], {
    queryParams: { redirectTo: state.url },
  });
};

export function roleGuard(allowedRoles: UserRole[]): CanActivateFn {
  return () => {
    const router = inject(Router);
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    if (!token) {
      return router.createUrlTree(['/login']);
    }

    if (role && allowedRoles.includes(role as UserRole)) {
      return true;
    }

    return router.createUrlTree([getRoleHome(role)]);
  };
}
