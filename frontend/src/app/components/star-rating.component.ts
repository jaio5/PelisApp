import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-star-rating',
  imports: [CommonModule],
  template: `
    <span class="stars">
      <ng-container *ngFor="let s of stars; let i = index">
        <span [class.filled]="i < roundedRating">★</span>
      </ng-container>
    </span>
  `,
  styles: [
    `.stars{color:#ddd} .stars .filled{color:#f5b301;font-weight:700;margin-right:2px}`
  ]
})
export class StarRatingComponent {
  @Input() rating = 0;
  stars = new Array(5);
  get roundedRating() {
    return Math.round(this.rating || 0);
  }
}
