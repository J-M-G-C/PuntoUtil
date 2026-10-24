import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Rol } from './modelos';

export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.token()) {
    return router.createUrlTree(['/login'], { queryParams: { retorno: state.url } });
  }

  const roles = route.data['roles'] as Rol[] | undefined;
  return !roles || auth.puede(...roles) ? true : router.createUrlTree(['/productos']);
};
