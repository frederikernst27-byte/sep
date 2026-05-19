import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

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
  isToday: boolean;
  trips: Trip[];
}

interface CalendarListItem {
  trip: Trip;
  durationLabel: string;
  statusLabel: string;
}

interface TripStats {
  total: number;
  upcoming: number;
  active: number;
  past: number;
}

type CalendarViewMode = 'month' | 'week' | 'day' | 'list';

@Component({
  selector: 'app-calendar-view',
  standalone: true,
  templateUrl: './calendar-view.html',
  styleUrl: './calendar-view.css'
})
export class CalendarView implements OnInit {
  currentDate = new Date();
  activeView: CalendarViewMode = 'month';
  trips: Trip[] = [];
  selectedTrip: Trip | null = null;
  loading = false;
  deleting = false;
  errorMsg = '';

  readonly weekdays = ['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'];
  readonly viewOptions: { value: CalendarViewMode; label: string }[] = [
    { value: 'month', label: 'Monat' },
    { value: 'week', label: 'Woche' },
    { value: 'day', label: 'Tag' },
    { value: 'list', label: 'Liste' }
  ];

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTrips();
  }

  get monthLabel(): string {
    return this.currentDate.toLocaleDateString('de-DE', {
      month: 'long',
      year: 'numeric'
    });
  }

  get periodLabel(): string {
    if (this.activeView === 'week') {
      const week = this.weekDays;
      return `${this.formatShortDate(week[0].date)} - ${this.formatShortDate(week[6].date)}`;
    }

    if (this.activeView === 'day') {
      return this.currentDate.toLocaleDateString('de-DE', {
        weekday: 'long',
        day: '2-digit',
        month: 'long',
        year: 'numeric'
      });
    }

    if (this.activeView === 'list') {
      return 'Alle Reisen';
    }

    return this.monthLabel;
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
        isToday: dateKey === this.todayKey,
        trips: this.tripsFor(dateKey)
      };
    });
  }

  get weekDays(): CalendarDay[] {
    const startDate = this.startOfWeek(this.currentDate);

    return Array.from({ length: 7 }, (_, index) => {
      const date = this.addDays(startDate, index);
      const dateKey = this.toDateKey(date);

      return {
        date,
        dateKey,
        dayNumber: date.getDate(),
        isCurrentMonth: date.getMonth() === this.currentDate.getMonth(),
        isToday: dateKey === this.todayKey,
        trips: this.tripsFor(dateKey)
      };
    });
  }

  get currentDay(): CalendarDay {
    const dateKey = this.toDateKey(this.currentDate);

    return {
      date: this.currentDate,
      dateKey,
      dayNumber: this.currentDate.getDate(),
      isCurrentMonth: true,
      isToday: dateKey === this.todayKey,
      trips: this.tripsFor(dateKey)
    };
  }

  get sortedTrips(): CalendarListItem[] {
    return [...this.trips]
      .sort((firstTrip, secondTrip) => firstTrip.startDate.localeCompare(secondTrip.startDate))
      .map((trip) => ({
        trip,
        durationLabel: `${this.formatDateKey(trip.startDate)} - ${this.formatDateKey(trip.endDate)}`,
        statusLabel: this.statusFor(trip)
      }));
  }

  get tripStats(): TripStats {
    return this.trips.reduce(
      (stats, trip) => {
        const status = this.statusFor(trip);

        if (status === 'Kommend') {
          stats.upcoming += 1;
        } else if (status === 'Vergangen') {
          stats.past += 1;
        } else {
          stats.active += 1;
        }

        return stats;
      },
      { total: this.trips.length, upcoming: 0, active: 0, past: 0 }
    );
  }

  get todayKey(): string {
    return this.toDateKey(new Date());
  }

  setView(view: CalendarViewMode): void {
    this.activeView = view;
  }

  previousPeriod(): void {
    this.movePeriod(-1);
  }

  nextPeriod(): void {
    this.movePeriod(1);
  }

  previousMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
  }

  nextMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
  }

  resetToToday(): void {
    this.currentDate = new Date();
  }

  navigateToAddTrip(): void {
    this.router.navigate(['/add-trip']);
  }

  formatWeekday(date: Date): string {
    return date.toLocaleDateString('de-DE', { weekday: 'long' });
  }

  formatMonthShort(date: Date): string {
    return date.toLocaleDateString('de-DE', { month: 'short' });
  }

  formatLongDate(date: Date): string {
    return date.toLocaleDateString('de-DE', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
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
    return this.trips
      .filter((trip) => trip.startDate <= dateKey && trip.endDate >= dateKey)
      .sort((firstTrip, secondTrip) => firstTrip.startDate.localeCompare(secondTrip.startDate));
  }

  private movePeriod(direction: -1 | 1): void {
    if (this.activeView === 'day') {
      this.currentDate = this.addDays(this.currentDate, direction);
      return;
    }

    if (this.activeView === 'week') {
      this.currentDate = this.addDays(this.currentDate, direction * 7);
      return;
    }

    this.currentDate = new Date(
      this.currentDate.getFullYear(),
      this.currentDate.getMonth() + direction,
      1
    );
  }

  private toDateKey(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private startOfWeek(date: Date): Date {
    const startDate = new Date(date);
    const startOffset = (startDate.getDay() + 6) % 7;
    startDate.setDate(startDate.getDate() - startOffset);
    return startDate;
  }

  private addDays(date: Date, days: number): Date {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
  }

  private formatShortDate(date: Date): string {
    return date.toLocaleDateString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  private formatDateKey(dateKey: string): string {
    const [year, month, day] = dateKey.split('-');
    return `${day}.${month}.${year}`;
  }

  private statusFor(trip: Trip): string {
    if (trip.startDate > this.todayKey) {
      return 'Kommend';
    }

    if (trip.endDate < this.todayKey) {
      return 'Vergangen';
    }

    return 'Aktuell';
  }
}
