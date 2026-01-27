import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class MovieApiService {
  constructor(private auth: AuthService) {}

  async list() {
    const res = await this.auth.fetchWithAuth('/api/movies');
    if (!res.ok) return [];
    return res.json();
  }

  async get(id: number) {
    const res = await this.auth.fetchWithAuth(`/api/movies/${id}`);
    if (!res.ok) throw new Error('Failed to load movie');
    return res.json();
  }

  async likeReview(reviewId: number) {
    const res = await this.auth.fetchWithAuth(`/api/reviews/${reviewId}/like`, { method: 'POST' });
    return res.ok;
  }
}
