import { Routes } from '@angular/router';
import { AddTripComponent } from './components/add-trip/add-trip';
import { CalendarView } from './components/calendar-view/calendar-view';

export const routes: Routes = [
  { path: 'calendar', component: CalendarView },
  { path: 'add-trip', component: AddTripComponent },
  { path: '', redirectTo: 'calendar', pathMatch: 'full' }  // optional
];
