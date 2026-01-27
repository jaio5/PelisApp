import { Routes } from '@angular/router';
import { MovieListComponent } from './components/movie-list.component';
import { MovieDetailComponent } from './components/movie-detail.component';
import { LoginComponent } from './components/login.component';
import { RegisterComponent } from './components/register.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
	{ path: '', component: MovieListComponent, canActivate: [authGuard] },
	{ path: 'login', component: LoginComponent },
	{ path: 'register', component: RegisterComponent },
	{ path: 'movie/:id', component: MovieDetailComponent, canActivate: [authGuard] },
	{ path: '**', redirectTo: '' }
];
