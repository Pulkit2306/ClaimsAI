import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'auth',
    children: [
      { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
      { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },
    ]
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'claims',
    canActivate: [authGuard],
    children: [
      { path: '', loadComponent: () => import('./features/claims/claim-list/claim-list.component').then(m => m.ClaimListComponent) },
      { path: 'new', loadComponent: () => import('./features/claims/claim-form/claim-form.component').then(m => m.ClaimFormComponent) },
      { path: ':id', loadComponent: () => import('./features/claims/claim-detail/claim-detail.component').then(m => m.ClaimDetailComponent) },
    ]
  },
  {
    path: 'policies',
    canActivate: [authGuard],
    loadComponent: () => import('./features/policies/policy-list/policy-list.component').then(m => m.PolicyListComponent)
  },
  {
    path: 'search',
    canActivate: [authGuard],
    loadComponent: () => import('./features/search/search.component').then(m => m.SearchComponent)
  },
  {
    path: 'ai-chat',
    canActivate: [authGuard],
    loadComponent: () => import('./features/ai-chat/ai-chat.component').then(m => m.AiChatComponent)
  },
  { path: '**', redirectTo: 'dashboard' }
];
