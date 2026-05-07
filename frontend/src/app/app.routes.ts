import { Routes } from '@angular/router';
import { AddTripComponent } from './components/add-trip/add-trip';

export const routes: Routes = [
  { path: 'add-trip', component: AddTripComponent },
  { path: '', redirectTo: 'add-trip', pathMatch: 'full' }  // optional
];
