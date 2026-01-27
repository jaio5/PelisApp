import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { MovieApiService } from '../services/movie-api.service';
import { MovieCardComponent } from './movie-card.component';

@Component({
  standalone: true,
  selector: 'app-movie-list',
  imports: [CommonModule, RouterModule, MovieCardComponent],
  template: `
    <div class="movies-container">
      <header class="topbar">
        <h1>PelisApp — Cartelera</h1>
        <nav>
          <span *ngIf="auth.isLoggedIn()">Hola, {{ auth.getUser() }}</span>
          <button *ngIf="auth.isLoggedIn()" (click)="logout()">Cerrar sesión</button>
          <a *ngIf="!auth.isLoggedIn()" routerLink="/login">Iniciar sesión</a>
        </nav>
      </header>

      <section class="grid">
        <article *ngFor="let m of movies" class="card" (click)="open(m.id)">
          <img [src]="m.posterPath || 'assets/placeholder.png'" alt="{{m.titulo}} poster" />
          <h3>{{ m.titulo }}</h3>
        </article>
      </section>
    </div>
  `,
  styles: [
    `.topbar{display:flex;justify-content:space-between;align-items:center;padding:1rem} .grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(150px,1fr));gap:1rem;padding:1rem} .card{cursor:pointer;border-radius:6px;overflow:hidden;text-align:center;padding:.5rem;border:1px solid #eee} img{width:100%;height:225px;object-fit:cover}`
  ]
})
export class MovieListComponent {
  movies: any[] = [];

  constructor(public auth: AuthService, private api: MovieApiService) {
    this.load();
  }

  async load() {
    try {
      this.movies = await this.api.list();
    } catch (e) {
      console.error(e);
    }
  }

  open(id: number) {
    window.location.href = `/movie/${id}`;
  }

  logout() {
    this.auth.clear();
    window.location.href = '/login';
  }
}
