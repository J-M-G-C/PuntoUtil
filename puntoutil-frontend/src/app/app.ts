import { Component, inject } from '@angular/core';

import {
  Router,
  NavigationEnd,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';

import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  private router = inject(Router);

  esLogin = false;

  constructor() {

    this.esLogin = this.router.url === '/login';

    this.router.events
      .pipe(
        filter(evento => evento instanceof NavigationEnd)
      )
      .subscribe(evento => {
        this.esLogin =
          evento.urlAfterRedirects.split('?')[0] === '/login';
      });

  }

}