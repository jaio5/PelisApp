import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-movie-card',
  imports: [CommonModule],
  template: `
    <article class="card" (click)="open()">
      <img [src]="movie.posterPath || 'assets/placeholder.png'" alt="{{movie.titulo}}" />
      <h4>{{ movie.titulo }}</h4>
    </article>
  `,
  styles: [`.card{cursor:pointer}`]
})
export class MovieCardComponent {
  @Input() movie: any = {};
  constructor(private router: Router) {}
  open() { this.router.navigate([`/movie/${this.movie.id}`]); }
}
