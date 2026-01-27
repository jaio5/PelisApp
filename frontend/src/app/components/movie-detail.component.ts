import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { MovieApiService } from '../services/movie-api.service';
import { StarRatingComponent } from './star-rating.component';

@Component({
  standalone: true,
  selector: 'app-movie-detail',
  imports: [CommonModule, RouterModule, StarRatingComponent],
  template: `
    <div class="detail">
      <button (click)="back()">← Volver</button>
      <div *ngIf="movie" class="movie">
        <img class="poster" [src]="movie.posterPath || 'assets/placeholder.png'" alt="poster" />
        <div class="info">
          <h2>{{ movie.titulo }}</h2>
          <p class="meta">Año: {{movie.anio}} · Duración: {{movie.duracion || '—'}} min</p>
          <p class="descripcion">{{ movie.sinopsis }}</p>
          <p class="rating">Valoración media: {{ avgRating() || '—' }}</p>

          <section class="cast">
            <h3>Reparto</h3>
            <div class="cast-list">
              <div *ngFor="let c of movie.cast"> 
                <img [src]="c.foto_url || 'assets/actor-placeholder.png'" alt="{{c.nombre}}" />
                <div>{{c.nombre}}</div>
              </div>
            </div>
          </section>

          <section class="reviews">
            <h3>Reseñas</h3>
            <div *ngFor="let r of movie.reviews">
              <div class="review-header">
                <strong>{{ r.usuario }}</strong>
                <span class="stars">{{ r.puntuacion }} ★</span>
              </div>
              <p>{{ r.comentario }}</p>
              <div class="review-actions">
                <button (click)="likeReview(r.id)">👍 {{ r.likes || 0 }}</button>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  `,
  styles: [
    `.detail{padding:1rem}.movie{display:flex;gap:1rem}.poster{width:220px;height:330px;object-fit:cover}.cast-list{display:flex;gap:.5rem}.cast-list div{text-align:center;width:80px} .reviews{margin-top:1rem}.review-header{display:flex;justify-content:space-between}`
  ]
})
export class MovieDetailComponent {
  movie: any | null = null;

  constructor(private route: ActivatedRoute, private router: Router, private api: MovieApiService) {
    const id = Number(route.snapshot.paramMap.get('id'));
    if (id) this.load(id);
  }

  async load(id: number) {
    try {
      this.movie = await this.api.get(id);
    } catch (e) {
      console.error(e);
    }
  }

  back() {
    this.router.navigate(['/']);
  }

  avgRating() {
    if (!this.movie?.reviews?.length) return null;
    const sum = this.movie.reviews.reduce((s: number, r: any) => s + (r.puntuacion || 0), 0);
    return (sum / this.movie.reviews.length).toFixed(1);
  }

  async likeReview(id: number) {
    try {
      await this.api.likeReview(id);
      const mid = Number(this.route.snapshot.paramMap.get('id'));
      if (mid) this.load(mid);
    } catch (e) {
      console.error(e);
    }
  }
}
