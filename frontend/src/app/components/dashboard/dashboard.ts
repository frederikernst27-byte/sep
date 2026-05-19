import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface Trip {
  id: number;
  name: string;
  destination: string;
  startDate: string;
  endDate: string;
}

interface CalendarEntry {
  id: number;
  name: string;
  dateTime: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {
  trips: Trip[] = [];
  entries: CalendarEntry[] = [];
  loading = false;
  errorMsg = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  get todayKey(): string {
    return this.toDateKey(new Date());
  }

  get activeTripCount(): number {
    return this.trips.filter((trip) => trip.startDate <= this.todayKey && trip.endDate >= this.todayKey).length;
  }

  get upcomingTripCount(): number {
    return this.trips.filter((trip) => trip.startDate > this.todayKey).length;
  }

  loadDashboardData(): void {
    this.loading = true;
    this.errorMsg = '';

    this.http.get<Trip[]>('http://localhost:8080/api/trips').subscribe({
      next: (trips) => {
        this.trips = trips;
        this.loading = false;
      },
      error: () => {
        this.errorMsg = 'Dashboard-Daten konnten nicht geladen werden.';
        this.loading = false;
      }
    });

    this.http.get<CalendarEntry[]>('http://localhost:8080/api/calendar').subscribe({
      next: (entries) => {
        this.entries = entries;
      },
      error: () => {
        this.entries = [];
      }
    });
  }

  private toDateKey(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
