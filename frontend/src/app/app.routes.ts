import { Routes } from '@angular/router';
import { AddTripComponent } from './components/add-trip/add-trip';
import { CalendarView } from './components/calendar-view/calendar-view';
import { DashboardComponent } from './components/dashboard/dashboard';
import {Login} from './components/login/login';

export const routes: Routes = [
  { path: 'dashboard', component: DashboardComponent },
  { path: 'calendar', component: CalendarView },
  { path: 'login', component: Login},
  { path: 'add-trip', component: AddTripComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full' }  // optional
];
