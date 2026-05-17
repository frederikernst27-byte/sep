import { Routes } from '@angular/router';
import { AddTripComponent } from './components/add-trip/add-trip';
import { CalendarView } from './components/calendar-view/calendar-view';
import {Login} from './components/login/login';

export const routes: Routes = [
  { path: 'calendar', component: CalendarView },
  { path: 'login', component: Login},
  { path: 'add-trip', component: AddTripComponent },
  { path: '', redirectTo: 'calendar', pathMatch: 'full' }  // optional
];
