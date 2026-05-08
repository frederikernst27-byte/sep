import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface Trip {
  id: number;
  name: string;
  destination: string;
  startDate: string;
  endDate: string;
}

interface CalendarDay {
  date: Date;
  dateKey: string;
  dayNumber: number;
  isCurrentMonth: boolean;
  trips: Trip[];
}

@Component({
  selector: 'app-calendar-view',
  standalone: true,
  templateUrl: './calendar-view.html',
  styleUrl: './calendar-view.css'
})
export class CalendarView implements OnInit {
  currentDate = new Date();
  trips: Trip[] = [];
  selectedTrip: Trip | null = null;
  loading = false;
  deleting = false;
  errorMsg = '';

  readonly weekdays = ['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadTrips();
  }

  get monthLabel(): string {
    return this.currentDate.toLocaleDateString('de-DE', {
      month: 'long',
      year: 'numeric'
    });
  }

  get calendarDays(): CalendarDay[] {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    const firstOfMonth = new Date(year, month, 1);
    const startOffset = (firstOfMonth.getDay() + 6) % 7;
    const startDate = new Date(year, month, 1 - startOffset);

    return Array.from({ length: 42 }, (_, index) => {
      const date = new Date(startDate);
      date.setDate(startDate.getDate() + index);
      const dateKey = this.toDateKey(date);

      return {
        date,
        dateKey,
        dayNumber: date.getDate(),
        isCurrentMonth: date.getMonth() === month,
        trips: this.tripsFor(dateKey)
      };
    });
  }

  previousMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
  }

  nextMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
  }

  loadTrips(): void {
    this.loading = true;
    this.errorMsg = '';

    this.http.get<Trip[]>('http://localhost:8080/api/trips').subscribe({
      next: (trips) => {
        this.trips = trips;
        if (this.selectedTrip && !trips.some((trip) => trip.id === this.selectedTrip?.id)) {
          this.selectedTrip = null;
        }
        this.loading = false;
      },
      error: () => {
        this.errorMsg = 'Reisen konnten nicht geladen werden.';
        this.loading = false;
      }
    });
  }

  selectTrip(trip: Trip): void {
    this.selectedTrip = trip;
    this.errorMsg = '';
  }

  closeDetails(): void {
    this.selectedTrip = null;
  }

  deleteSelectedTrip(): void {
    if (!this.selectedTrip) return;

    this.deleting = true;
    this.errorMsg = '';
    const tripId = this.selectedTrip.id;

    this.http.delete(`http://localhost:8080/api/trips/${tripId}`).subscribe({
      next: () => {
        this.trips = this.trips.filter((trip) => trip.id !== tripId);
        this.selectedTrip = null;
        this.deleting = false;
      },
      error: () => {
        this.errorMsg = 'Reise konnte nicht geloescht werden.';
        this.deleting = false;
      }
    });
  }

  private tripsFor(dateKey: string): Trip[] {
    return this.trips.filter((trip) => trip.startDate <= dateKey && trip.endDate >= dateKey);
  }

  private toDateKey(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

}
